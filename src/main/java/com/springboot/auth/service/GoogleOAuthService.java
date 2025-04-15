package com.springboot.auth.service;

// Google OAuth 관련 로직을 처리하는 서비스 클래스
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.oauth.GoogleInfoDto;
import lombok.RequiredArgsConstructor;
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
public class GoogleOAuthService {

    private final RedisTemplate redisTemplate;


    // application.yml에서 Google OAuth client ID를 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    // application.yml에서 Google OAuth client secret을 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // DB에서 회원 정보를 조회하기 위한 리포지토리
    private final MemberRepository memberRepository;
    // JWT 토큰을 생성하기 위한 유틸리티 클래스
    private final JwtTokenizer jwtTokenizer;

    // 생성자를 통해 의존성 주입
    public GoogleOAuthService(RedisTemplate redisTemplate, MemberRepository memberRepository, JwtTokenizer jwtTokenizer) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
        this.jwtTokenizer = jwtTokenizer;
    }

    // 구글 사용자 정보로 로그인 처리: DB에서 사용자 찾고 토큰 발급
    public Map<String, String> processUserLogin(GoogleInfoDto googleInfoDto) {
        // 이메일 기준으로 DB에서 사용자 조회 (없으면 예외 발생)
        Member member = memberRepository.findByEmail(googleInfoDto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

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
                accessTokenExp, jwtTokenizer.getSecretKey());
        // JWT 리프레시 토큰 생성
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, refreshTokenExp, jwtTokenizer.getSecretKey(), accessToken);

        // 토큰을 Map 형태로 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    // 프론트에서 전달된 인가 코드로 구글에 액세스 토큰 요청
    public String getAccessTokenFromCode(String code) {
        // RestTemplate 인스턴스 생성
        RestTemplate restTemplate = new RestTemplate();

        // HTTP 헤더 설정 (Content-Type: application/x-www-form-urlencoded)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디에 필요한 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", "http://localhost:3000/"); // 프론트의 redirect URI
        params.add("grant_type", "authorization_code");

        // 헤더와 바디를 함께 담은 HttpEntity 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 구글 토큰 엔드포인트에 POST 요청 보내기
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token", request, Map.class
        );

        // 응답에서 액세스 토큰 추출
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String accessToken = response.getBody().get("access_token").toString();
            String refreshToken = response.getBody().get("refresh_token").toString();

            // 사용자 정보 조회 요청
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );
            if (userInfoResponse.getStatusCode() == HttpStatus.OK && userInfoResponse.getBody() != null) {
                String email = userInfoResponse.getBody().get("email").toString();

                // 회원 조회
                Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                // refreshToken 저장
                member.setRefreshToken(refreshToken);
                memberRepository.save(member);

                // Redis 저장 예시 (실제 RedisTemplate 주입 필요)
                redisTemplate.opsForValue().set("google:" + email, accessToken,
                    jwtTokenizer.getAccessTokenExpirationMinutes(), TimeUnit.MINUTES);
            }
        }

        // 예외 발생: 토큰 요청 실패
        throw new RuntimeException("Failed to get access token from Google");
    }
}