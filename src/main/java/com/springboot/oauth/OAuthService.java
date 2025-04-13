package com.springboot.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@Slf4j
public class OAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String clientId;

    // Google OAuth 토큰을 검증하고 사용자 정보를 반환합니다.
    public GoogleInfoDto authenticate(String token) {
        return extractUserInfoFromToken(token);
    }

    // 토큰에서 Google 사용자 정보를 추출합니다.
    private GoogleInfoDto extractUserInfoFromToken(String token) {
        try {
            log.info("token : {}", token);
            GoogleIdTokenVerifier verifier = createGoogleIdTokenVerifier();
            // 토큰 검증
            GoogleIdToken idToken = verifier.verify(token);
            Payload payload = idToken.getPayload();
            // Payload로부터 사용자 정보 추출
            return convertPayloadToGoogleInfoDto(payload);

        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessLogicException(ExceptionCode.SECURITY_EXCEPTION);
        }
    }

    // Payload를 GoogleInfoDto로 변환합니다.
    private GoogleInfoDto convertPayloadToGoogleInfoDto(Payload payload) {
        String email = payload.getEmail();
        String name = payload.get("name").toString();
        return new GoogleInfoDto(email, name);
    }

    // Google ID 토큰 검증기를 생성합니다.
    private GoogleIdTokenVerifier createGoogleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }
}
