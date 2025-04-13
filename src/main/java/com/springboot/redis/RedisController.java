package com.springboot.redis;

import com.springboot.auth.jwt.JwtTokenizer;
import com.springboot.auth.utils.MemberDetailService;
import com.springboot.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/v11/auth")
@RequiredArgsConstructor
public class RedisController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenizer jwtTokenizer;
    private final RedisService redisService;
    private final MemberDetailService memberDetailService;

    @PostMapping("/logout")
    public ResponseEntity postLogout(Authentication authentication, HttpServletResponse response) {
        String username = authentication.getName(); // 현재 인증된 사용자의 사용자명을 가져옵니다.

        if (redisService.logout(username)) {
            SecurityContextHolder.clearContext();
            // 🔹 RefreshToken 쿠키 삭제 (만료 시간 0 설정)
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .domain("localhost") // 프론트엔드 도메인에 맞게 변경
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(0) // 즉시 만료
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // ✅ 🔹 RefreshToken을 이용해 새로운 AccessToken & RefreshToken 발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("🔄 RefreshToken 검증 및 AccessToken 재발급 요청");

        // 1️⃣ 쿠키에서 RefreshToken 가져오기
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("RefreshToken이 존재하지 않습니다.");
        }

        try {
            // 2️⃣ RefreshToken 검증
            jwtTokenizer.verifySignature(refreshToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()));
            String username = jwtTokenizer.getClaims(refreshToken, jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey())).getBody().getSubject();

            // 3️⃣ 사용자 정보 가져오기
            Member member = (Member) memberDetailService.loadUserByUsername(username);

            // 4️⃣ 새로운 AccessToken 생성
            String newAccessToken = jwtTokenizer.generateAccessToken(
                    Map.of("username", member.getEmail(), "roles", member.getRoles()),
                    member.getEmail(),
                    jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes()),
                    jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey())
            );

            // 5️⃣ 새로운 RefreshToken 생성
            String newRefreshToken = jwtTokenizer.generateRefreshToken(
                    member.getEmail(),
                    jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes()),
                    jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey()),
                    newAccessToken
            );

            // 6️⃣ Redis에 기존 토큰 삭제 후 새 AccessToken 저장
            redisTemplate.delete(member.getEmail());
            redisTemplate.opsForValue().set(member.getEmail(), newAccessToken, jwtTokenizer.getAccessTokenExpirationMinutes(), TimeUnit.MINUTES);

            // 7️⃣ 새로운 RefreshToken을 쿠키에 저장
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .domain("localhost")
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            // 8️⃣ 새로운 AccessToken을 응답으로 반환
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {

            return ResponseEntity.status(401).body("RefreshToken이 유효하지 않습니다.");
        }
    }

    // ✅ RefreshToken 쿠키에서 가져오는 메서드
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

