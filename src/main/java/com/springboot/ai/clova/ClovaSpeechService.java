package com.springboot.ai.clova;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClovaSpeechService {

    @Value("${clova.api.id}")
    private String clientId;

    @Value("${clova.api.key}")
    private String clientSecret;

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
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
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
            e.printStackTrace();
            throw new RuntimeException("Clova STT 요청 중 오류 발생", e);
        }
        // 최종 문자열을 반환
        // 이걸 GPT 한테 넘겨줘야함
        return response.toString();

    }
}
