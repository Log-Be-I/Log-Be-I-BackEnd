package com.springboot.ai.googleTTS;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void synthesizeText(String text, String outputFilePath) throws Exception {
        URL url = new URL("https://texttospeech.googleapis.com/v1/text:synthesize?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // 요청 JSON 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("input", Map.of("text", text));
        payload.put("voice", Map.of("languageCode", "ko-KR", "name", "ko-KR-Chirp3-HD-Charon"));
        payload.put("audioConfig", Map.of("audioEncoding", "MP3"));

        // JSON 직렬화
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = mapper.writeValueAsString(payload);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
        }

        // 응답 확인
        int status = conn.getResponseCode();
        if (status != 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                String error = br.lines().reduce("", (acc, line) -> acc + line);
                throw new RuntimeException("TTS API 요청 실패: " + error);
            }
        }

        // 응답 수신
        String jsonResponse;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            jsonResponse = br.lines().reduce("", (acc, line) -> acc + line);
        }

        // Base64 디코딩 & 파일 저장
        String audioContent = (String) mapper.readValue(jsonResponse, Map.class).get("audioContent");
        byte[] audioBytes = Base64.getDecoder().decode(audioContent);
        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(audioBytes);
        }

        log.info("Audio saved to: {}", outputFilePath);
    }
}