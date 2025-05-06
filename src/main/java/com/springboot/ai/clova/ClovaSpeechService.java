package com.springboot.ai.clova;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClovaSpeechService {

    @Value("${clova.api.key}")
    private String CLOVA_API_SECRET;
    @Value("${clova.api.id}")
    private String CLOVA_CLIENT_ID;

//    private final LogStorageService logStorageService;

    String logName = "Clova";
    // 클로바 STT API 에 오디오 파일을 전송하고, 텍스트로 변환된 결과를 받아오는 메서드
    public String recognizeSpeech(File voiceFile) throws IOException {
        // 언어 설정
        String language = "Kor";
        // 클로바 STT API 요청 URL 설정
        String apiURL = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt?lang=" + language;
        // API 응답 결과 (JSON 문자열) 담는 객체 생성
        StringBuilder response = new StringBuilder();

        // HTTP 연결 준비
        try {
            // 클로바 STT API 요청 URL 객체 생성
            URL url = new URL(apiURL);
            // URL 객체로부터 HTTP 연결 객체 생성
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 캐시 사용 여부 설정 (false 로 설정해 항상 서버에 fresh 데이터 요청
            conn.setUseCaches(false);
            // 출력 가능 설정 (파일을 전송을 위해 true 로 설정)
            conn.setDoOutput(true);
            // 입력 가능 설정 (응답을 읽기 위해 true 로 설정)
            conn.setDoInput(true);
            // HTTP 요청 방식 설정 -> 클로바 API 는 POST 방식만 지원
            conn.setRequestMethod("POST"); // ✅ 꼭 있어야 함
            // 요청 본문의 데이터 타입 설정 -> 바이너리 데이터 전송 (음성 파일)
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            // 인증용 헤더 작성
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", CLOVA_CLIENT_ID);
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", CLOVA_API_SECRET);
            // 설정한 내용을 바탕으로 실제 서버 연결 시작
            conn.connect();

            // 서버로 데이터를 보낼 스트림
            try (OutputStream outputStream = conn.getOutputStream();
                 // 전송할 음성 파일을 읽기 위한 스트림
                 FileInputStream inputStream = new FileInputStream(voiceFile)) {
                // 한번에 전송할 데이터 크기를 지정한 버퍼 생성 (4KB)
                byte[] buffer = new byte[4096];
                // 읽어들인 바이트 수를 저장할 변수
                int bytesRead;
                // 음성 파일에서 데이터를 끝까지 읽을때까지 반복
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // 읽은 데이터를 서버로 전송 (버퍼의 0번부터 bytesRead 길이만큼)
                    outputStream.write(buffer, 0, bytesRead);
                }
                // 아직 출력 스트림에 남아잇는 데이터가 있다면 모두 전송
                outputStream.flush();
            }

            // 서버로부터 응답 상태 코드 받아옴
            int responseCode = conn.getResponseCode();
            // 200 이라면 InputStream 할당, 아니라면 ErrorStream 할당
            InputStream inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

            // BufferReader 로 스트림을 한 줄씩 읽는다.
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String inputLine;
                // 더이상 읽을 스트림이 없을때 true
                while ((inputLine = br.readLine()) != null) {
                    // response.append 를 통해 결과 문자열을 하나로 이어붙인다.
                    response.append(inputLine);
                }
            }

        } catch (Exception e) {
//            e.printStackTrace();
//            logStorageService.logAndStoreWithError("Clova STT request failed: {}", logName, e.getMessage(), e);
            throw new RuntimeException("Clova STT 요청 중 오류 발생", e);
        }
        // 최종 문자열을 반환
        // 이걸 GPT 한테 넘겨줘야함
        return response.toString();
    }

    // 음성데이터를 text 로 변환
    public String voiceToText (MultipartFile audioFile) throws IOException {
        // CLOVA 에서 허용하는 음성데이터 확장자 목록
        List<String> allowedExtensions = List.of("mp3", "acc", "ac3", "ogg", "flac", "wav", "m4a");

        // 파일 이름에서 확장자 추출
        String originalFilename = audioFile.getOriginalFilename();
        // 확장자명을 담을 문자열 객체 생성
        String extension = "";
        // 확장자명이 비어있지 않고 . 을 포함하고있다면
        if(originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename
                    // 파일 이름에서 .의 인덱스 번호에 +1 을 더해 순수한 확장자 이름만 찾는다
                    .substring(originalFilename.lastIndexOf(".") + 1)
                    // 보통 소문자로 이뤄지지만 대문자가 섞일 수 있으니 소문자로 변경
                    .toLowerCase(); // 컴퓨터는 확장자명의 대소문자 구분을 못함 ex) MP3 == mp3 => true
        }

        // 임시 파일 생성 (확장자 포함)
        // 업로드된 MultipartFile(현재 로직에서는 음성데이터) 을 저장할 임시 파일 객체 생성
        File tempFile = File.createTempFile("clova_", "." + extension);
        // 사용자가 업로드한 오디오 파일 데이터를 임시 파일에 저장
        audioFile.transferTo(tempFile); // 이렇게 담아줘야 file 객체로 API 에 보낼 수 있다.

        // 네이버 클로바 음성인식 서버에 요청 보낼 때 사용할 헤더 설정
        // header 객체 생성
        HttpHeaders headers = new HttpHeaders();
        // Content-Type 은 바이너리 데이터임으로 application/octet-stream
        // 음성 파일은 사람이 직접 읽을 수 없는 0과 1의 데이터로 저장되기 때문이다.
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // Accept = 응답 __ 응답을 JSON 으로 받겠다는 의미
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // api 요청을 위한 key 설정
        headers.set("X-NCP-APIGW-API-KEY-ID", CLOVA_CLIENT_ID);  // 네이버 콘솔 Client ID
        headers.set("X-NCP-APIGW-API-KEY", CLOVA_API_SECRET);       // 네이버 콘솔 Secret Key

        // tempFile 을 CLOVA 에 전송해서 음성 -> 텍스트 변환 결과 받아오기
        String result = recognizeSpeech(tempFile);
        log.info("CLOVA: " + result);
        // CLOVA 전송을 위해 임시저장해둔 파일 삭제
        tempFile.delete();

        return result;
    }

}
