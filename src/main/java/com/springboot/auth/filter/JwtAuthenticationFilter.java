package com.springboot.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.auth.dto.LoginDto;
import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.oauth.OAuthAuthenticationToken;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberRepository memberRepository;

    @Value("${jwt.refresh-token-expiration-minutes}")
    private long refreshTokenExpiration;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer, RedisTemplate<String, Object> redisTemplate, MemberRepository memberRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    // Checked Exception 을 자동으로 처리해주는 역할
    // try-catch / throws 키워드 추가 안해줘도 됨
    @SneakyThrows
    @Override
    // 사용자의 입력 정보를 받아 인증을 거친 토큰을 생성하는 역할
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // ObjectMapper = 자바 객체를 JSON 으로 변환하거나, JSON 을 자바 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // request 의 입력 스트림을 가져와서 바디(JSON 데이터) 를 읽어온다.
            // 읽어온 데이터를 loginDto 에 할당
            LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
            // 여기서 password가 ""라면 OAuth 방식으로 토큰 생성 시도
            if (loginDto.getPassword().isEmpty()) {
                // 커스텀 토큰 사용 (email 만 있는 토큰)
                OAuthAuthenticationToken token = new OAuthAuthenticationToken(loginDto.getUsername());
                return authenticationManager.authenticate(token);
            } else {
                // 받아온 데이터에서 username, password 를 사용해 토큰을 생성한다
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
                // 만들어진 토큰을 검증하여 성공하면 인증된 Authentication 객체를 반환하고 실패하면 예외를 발생시킨다.
                return authenticationManager.authenticate(authenticationToken);
            }

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws ServletException, IOException {
        // .getPrincipal() = 인증된 사용자 정보를 가져와서 member 에 할당
        // 이때 UserDetails 타입으로 반환하는데 이때 member 클래스로 다운캐스팅 해야한다.

        Optional<Member> findMember = memberRepository.findByEmail((String) authResult.getPrincipal());
        Member member = findMember.orElse(null);

        // accessToken 생성
        String accessToken = delegateAccessToken(member);
        // refreshToken 생성
        String refreshToken = delegateRefreshToken(member, accessToken);
        //  Redis에 액세스 토큰 저장 (Key: "TOKEN:사용자이메일", Value: accessToken)
        redisTemplate.opsForValue().set(member.getEmail(), accessToken,
                jwtTokenizer.getAccessTokenExpirationMinutes(), TimeUnit.MINUTES);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                // javaScript 에서 접근 불가능
                .httpOnly(true)
                // HTTPS 에서만 전송
                .secure(false)
                .domain("localhost")
                // 모든 도메인 접근 허용
                .path("/")
                .sameSite("Lax")
                // refreshToken 수명
                .maxAge(7 * 24 * 60 * 60)
                .build();

        // 응답 헤더에 access 토큰을 Bearer 토큰 형식으로 추가
        response.setHeader("Authorization", "Bearer " + accessToken);
        // 응답 헤더에 refresh 토큰 추가
//        response.setHeader("Refresh", refreshToken);
//        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);

        // 쿠키로 refreshToken 전달
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    }

    // member 의 정보로 access 토큰 생성
    private String delegateAccessToken(Member member) {
        // payload 에 들어갈 정보 입력
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", member.getEmail());
        claims.put("roles", member.getRoles());

        // subject 에 member email 할당
        String subject = member.getEmail();
        // 만료기한에 토큰의 만료기한 할당
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        // 문자열 형태의 시크릿키를 인코딩
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        // accessToken 생성
        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        System.out.println("🔍 원본 Secret Key: " + jwtTokenizer.getSecretKey());
        System.out.println("🔍 Base64 Secret Key: " + jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));

        return accessToken;
    }

    // member 의 정보로 refresh 토큰 생성
    private String delegateRefreshToken(Member member, String accessToken) {
        // subject 에 member email 할당
        String subject = member.getEmail();
        // 토큰의 만료기한 설정
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        // 인코딩된 시크릿키 생성
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        // refresh 토큰 생성
        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey, accessToken);

        return refreshToken;
    }

}
