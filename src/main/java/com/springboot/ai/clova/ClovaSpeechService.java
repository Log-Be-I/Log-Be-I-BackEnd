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

    public String recognizeSpeech(File voiceFile) throws IOException {
        String language = "Kor";
        String apiURL = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt?lang=" + language;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST"); // ✅ 꼭 있어야 함
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
            conn.connect();

            try (OutputStream outputStream = conn.getOutputStream();
                 FileInputStream inputStream = new FileInputStream(voiceFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream inputStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Clova STT 요청 중 오류 발생", e);
        }

        return response.toString();
    }
}
