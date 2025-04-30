package com.springboot.ai.googleTTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.log.LogStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GoogleTextToSpeechService {

    @Value("${google.api.key}")
    private String apiKey;

//    private LogStorageService logStorageService;
    String logName = "Google_TTS";
    // toggle
    // text -> voice 로 변환할 글, outputFilePath -> 파일 저장할 경로 ( 저장될 파일 이름만 작성하면 경로는 자동으로 현 파일 위치로 잡는다)
    public void synthesizeText(String text, String outputFilePath) throws Exception {
        // url -> 구글 TTS 요청 url + apiKey;
        URL url = new URL("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + apiKey);
        // 구글 TTS 에 연결해주는 객체 생성
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // true: 서버에 뭔가 보낸다는 의미, false: (데이터 안보냄)
        conn.setDoOutput(true);
        // POST 요청으로 설정
        conn.setRequestMethod("POST");
        // 돌려받을 content type 설정
        conn.setRequestProperty("Content-Type", "application/json");

        // 요청 JSON 구성
        Map<String, Object> payload = new HashMap<>();
        // "text": text (본문)
        payload.put("input", Map.of("text", text));
        // voice 를 ko-KR 언어로 설정,  "name" : 모델 이름
        payload.put("voice", Map.of("languageCode", "ko-KR", "name", "ko-KR-Chirp3-HD-Charon"));
        // audio 설정은 MP3 형태로 받는다
        payload.put("audioConfig", Map.of("audioEncoding", "MP3"));

        // JSON 직렬화
        ObjectMapper mapper = new ObjectMapper();
        // 구글 TTS 모델 및 기본 설정을 담은 payload 를 Json 으로 만들기
        String jsonRequest = mapper.writeValueAsString(payload);

        // 서버로 데이터를 보낼 통로 (OutputStream) 생성 -> 해당 통로로 데이터를 보내야 구글 서버가 내가 보낸 데이터 인식이 가능합
        // conn.getOutputStream() 여기에 데이터 흘려보낼 수 있다.
        try (OutputStream os = conn.getOutputStream()) {
            // 최종 요청인 jsonRequest 의 인코딩폼을 UTF_8 로 설정하여 OutputStream 에 알맞는 형태로 전송 가능하게 만들어줌
            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
        }

        // 응답 확인
        int status = conn.getResponseCode();
        // 실패했다면
        if (status != 200) {
            //
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                String error = br.lines().reduce("", (acc, line) -> acc + line);
                // TTS 요청 실패시 로그 남기기
//                logStorageService.logAndStoreWithError("TTS API Request Failed: {}", error, logName);
                throw new RuntimeException("TTS API Request Failed: " + error);
            }
        }

        // 응답 수신
        // 서버 응답 데이터 변수 선언
        String jsonResponse;
        // BufferReader -> 한 줄씩, 빠르고 효율적으로 텍스트 데이터를 읽는 도구
        // getInputStream(): 서버로부터 받은 성공 응답 데이터를 가져옴
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            //
            jsonResponse = br.lines().reduce("", (acc, line) -> acc + line);
        }

        // Base64 디코딩 & 파일 저장
        // jsonResponse 구글 TTS 응답 JSON 안에 base64로 인코딩된 오디오 데이터를 담고 있는 필드야.
        String audioContent = (String)mapper.readValue(jsonResponse, Map.class).get("audioContent");
        // audioBytes = 디코딩된  audioContent 를 디코딩하여 audioBytes 에 담기
        byte[] audioBytes = Base64.getDecoder().decode(audioContent);
        // Ensure output directory exists
        File outputFile = new File("/app/audio/" + outputFilePath);
        outputFile.getParentFile().mkdirs(); // 폴더가 없으면 생성
        // 기존 코드 변경
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(audioBytes);
        }

//        logStorageService.logAndStoreWithError("Audio saved to: {}", outputFilePath, logName);
    }
}