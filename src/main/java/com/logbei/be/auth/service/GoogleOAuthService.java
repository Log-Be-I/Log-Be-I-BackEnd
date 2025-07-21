package com.logbei.be.auth.service;

// Google OAuth 관련 로직을 처리하는 서비스 클래스

import com.logbei.be.auth.dto.GoogleTokenResponse;
import com.logbei.be.auth.jwt.JwtTokenizer;
import com.logbei.be.exception.exception.BusinessLogicException;
import com.logbei.be.exception.exception.ExceptionCode;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.repository.MemberRepository;
import com.logbei.be.oauth.GoogleInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final JwtTokenizer jwtTokenizer;


    // application.yml에서 Google OAuth client ID를 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    // application.yml에서 Google OAuth client secret을 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // redirect uri
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    // 프론트에서 전달받은 code로 Google에 토큰 요청
    public Map<String, String> getTokensFromCode(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        System.out.println(params);
        System.out.println(headers);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token", request, GoogleTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            GoogleTokenResponse body = response.getBody();
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", body.getAccessToken());
//            tokens.put("refresh_token", body.getRefreshToken());
            tokens.put("id_token", body.getIdToken());

            // refreshToken이 null 일 수도 있음
            if (body.getRefreshToken() != null) {
                tokens.put("refresh_token", body.getRefreshToken());
            } else {
                log.warn("💢💢💢💢💢 Google did not return a refresh_token.");
            }
            return tokens;
        }
        throw new RuntimeException("Google 토큰 발급 실패");
    }

    public void saveTempRefreshToken(String email, String refreshToken) {
        if (refreshToken != null) {
            redisTemplate.opsForValue().set("temp:refreshToken:" + email, refreshToken, 10, TimeUnit.MINUTES);
        } else {
            log.warn("⛔⛔⛔⛔ Tried to store null refresh_token for email : {}", email);
        }

    }

    // 구글 사용자 정보로 로그인 처리: DB에서 사용자 찾고 토큰 발급
    public Map<String, String> processUserLogin(GoogleInfoDto googleInfoDto, String refreshToken) {
        // 이메일 기준으로 DB에서 사용자 조회 (없으면 예외 발생)
        Member member = memberRepository.findByEmail(googleInfoDto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        String existingRefreshToken = member.getRefreshToken();
        if (!refreshToken.equals(existingRefreshToken)) {
            member.setRefreshToken(refreshToken);
            memberRepository.save(member);
        }

        redisTemplate.opsForValue().set("google:" + member.getEmail(), refreshToken,
                jwtTokenizer.getRefreshTokenExpirationMinutes(), TimeUnit.MINUTES);

        return generateAuthTokens(member);
    }

    // JWT 액세스 토큰과 리프레시 토큰을 생성하여 반환
    private Map<String, String> generateAuthTokens(Member member) {
        // JWT claims에 포함할 정보 설정 (사용자 ID와 역할)
        Map<String, Object> claims = Map.of(
                "memberId", member.getMemberId(),
                "roles", member.getRoles()
        );

        // subject는 이메일로 설정
        String subject = member.getEmail();

        // 액세스 토큰 및 리프레시 토큰 만료 시간 계산
        Date accessTokenExp = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        Date refreshTokenExp = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        // JWT 액세스 토큰 생성
        String accessToken = jwtTokenizer.generateAccessToken(claims, subject,
                // base64 로 인코딩 완료
                accessTokenExp, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));
        // JWT 리프레시 토큰 생성
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, refreshTokenExp, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()), accessToken);

        // 토큰을 Map 형태로 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    // Google AccessToken을 Redis에 저장하는 메서드
    public void saveAccessTokenToRedis(String email, String accessToken){
        redisTemplate.opsForValue().set(("google:" + email), accessToken);
        if(redisTemplate.opsForValue().get("google:" + email) == null){
            throw new BusinessLogicException(ExceptionCode.TOKEN_NOT_EXIST);
        }
    }
}