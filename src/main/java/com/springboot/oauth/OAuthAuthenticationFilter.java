package com.springboot.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class OAuthAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🔥 AntPathRequestMatcher 로 명시적 경로 + POST 설정
    public OAuthAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(defaultFilterProcessesUrl, "POST"));  // 👈 POST 요청만 매치됨
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        log.info("🔥 OAuthAuthenticationFilter 진입함"); // 👈 반드시 찍혀야 함

        // JSON -> OAuthLoginRequest
        OAuthLoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), OAuthLoginRequest.class);
        log.info("📨 요청받은 username = {}", loginRequest.getUsername());

        // username 기반 OAuthAuthenticationToken 생성
        OAuthAuthenticationToken authRequest = new OAuthAuthenticationToken(loginRequest.getUsername());

        // AuthenticationManager 통해 인증 시도
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) {
        log.info("✅ OAuth 로그인 성공");
        // 이후 후처리 로직 필요 시 여기에 추가
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        log.warn("❌ OAuth 로그인 실패: {}", failed.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth Login Failed");
    }
}
