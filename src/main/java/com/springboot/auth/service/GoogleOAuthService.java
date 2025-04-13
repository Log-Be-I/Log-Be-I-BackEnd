package com.springboot.auth.service;

import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.oauth.GoogleInfoDto;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleOAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenizer jwtTokenizer;

    public GoogleOAuthService(MemberRepository memberRepository, JwtTokenizer jwtTokenizer) {
        this.memberRepository = memberRepository;
        this.jwtTokenizer = jwtTokenizer;
    }

    // 로그인 처리
    public Map<String, String> processUserLogin(GoogleInfoDto googleInfoDto) {
        Member member = memberRepository.findByEmail(googleInfoDto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        return generateAuthTokens(member);
    }

    // JWT 생성만 담당
    private Map<String, String> generateAuthTokens(Member member) {
        Map<String, Object> claims = Map.of(
                "memberId", member.getMemberId(),
                "roles", member.getRoles()
        );

        String subject = member.getEmail();

        Date accessTokenExp = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
        Date refreshTokenExp = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String accessToken = jwtTokenizer.generateAccessToken(claims, subject,
                accessTokenExp, jwtTokenizer.getSecretKey());
        String refreshToken = jwtTokenizer.generateRefreshToken(subject,refreshTokenExp, jwtTokenizer.getSecretKey(), accessToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
}