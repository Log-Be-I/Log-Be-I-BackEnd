package com.springboot.auth.filter;

import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.auth.utils.MemberDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberDetailService memberDetailService;
    public JwtVerificationFilter(JwtTokenizer jwtTokenizer,
                                 CustomAuthorityUtils authorityUtils,

                                 RedisTemplate<String, Object> redisTemplate, MemberDetailService memberDetailService) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;

        this.redisTemplate = redisTemplate;
        this.memberDetailService = memberDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                Map<String, Object> claims = verifyJws(request);
                // Redis 에서 토큰 검증
                isTokenValidInRedis(claims);
                setAuthenticationToContext(claims);


            } catch (Exception e) {
                log.warn("🚨 JWT 인증 실패: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        chain.doFilter(request, response);
    }



    @Override
    // JWT 가 인증 헤더에 포함되지 않았다면 자격증명이 필요하지 않은 리소스에 대한 요청이라고 판단후
    // 다음 필터로 처리를 넘겨줌
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // request 의 Authorization header 의 값을 얻는다
        String authorization = request.getHeader("Authorization");
        String path = request.getRequestURI();
        // Authorization header 의 값이 null 이거나 Authorization header 의 값이
        // "Bearer"로 시작하지 않는다면 해당 Filter 의 동작을 수행하지 않도록 정의

        return authorization == null ||
                !authorization.startsWith("Bearer") ||
                // /api/auth/google/ 경로로 들어오는 요청은 필터 무시 (구글 토큰은 따로 검증해서 그냥 패스시켜야함)
                path.equals("/api/auth/google");
    }

    // JWT 를 검증하는데 사용되는 메서드
    private Map<String, Object> verifyJws(HttpServletRequest request) {
        // HTTP 요청에서 Authorization 헤더 값을 가져온다.
        // 이때 Bearer(공백포함) 을 제거한 순수한 JWT 값만 추출한다
        String jws = request.getHeader("Authorization").replace("Bearer ","");
        // 시크릿키를 인코딩해서 가져온다
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        // JWT 를 검증하고 payload(Claims) 데이터를 Map 형태로 추출
        // getClaims() 는 JWT 의 서명을 검증한 후, Payload 부분을 파싱하여 반환
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
        return claims;
    }

    // 시큐리티 context 에 있는 인증 정보를 변경
    private void setAuthenticationToContext(Map<String, Object> claims) {
        // payload 에서 username 가져오는데 String 으로 형변환 해줘야함
        String username = (String)claims.get("username");
        UserDetails userDetails = memberDetailService.loadUserByUsername(username);

        // payload 에서 권한 목록 가져와서 권한 생성후 리스트화
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List)claims.get("roles"));
        // username 과 password 가 들어간 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        // 시큐리티 context 에 있는 인증 정보를 현재 생성한 인증 정보로 교체
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // redis 에서 토큰을 검증하는 메서드 추가
    private void isTokenValidInRedis(Map<String, Object> claims) {
        String username = Optional.ofNullable((String)claims.get("username"))
                .orElseThrow(() -> new NullPointerException("Username is Null"));

        Boolean hasKey = redisTemplate.hasKey(username);

        if(Boolean.FALSE.equals(hasKey)) {
            throw new IllegalStateException("Redis Key Does Not Exist for username: " + username);
        }
    }
}
