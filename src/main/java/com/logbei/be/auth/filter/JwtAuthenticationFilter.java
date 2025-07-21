package com.logbei.be.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logbei.be.auth.dto.LoginDto;
import com.logbei.be.auth.jwt.JwtTokenizer;
import com.logbei.be.auth.utils.MemberDetails;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.repository.MemberRepository;
import com.logbei.be.oauth.OAuthAuthenticationToken;
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
        // ✅ OPTIONS 요청은 인증 처리하지 않고 넘김
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return null; // 또는 throw new AuthenticationServiceException("OPTIONS bypass"); 등
        }
        // ObjectMapper = 자바 객체를 JSON 으로 변환하거나, JSON 을 자바 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
            if (loginDto.getPassword().isEmpty()) {
                OAuthAuthenticationToken token = new OAuthAuthenticationToken(loginDto.getUsername());
                return authenticationManager.authenticate(token);
            } else {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
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
        MemberDetails memberDetails = (MemberDetails) authResult.getPrincipal();
        Optional<Member> findMember = memberRepository.findByEmail(memberDetails.getEmail());
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
