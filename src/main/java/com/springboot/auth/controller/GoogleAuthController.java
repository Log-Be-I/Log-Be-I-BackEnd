package com.springboot.auth.controller;

import com.springboot.auth.service.GoogleOAuthService;
import com.springboot.member.service.MemberService;
import com.springboot.oauth.GoogleInfoDto;
import com.springboot.oauth.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private final OAuthService oAuthService;
    private final MemberService memberService;
    private final GoogleOAuthService googleOAuthService;

    public GoogleAuthController(OAuthService oAuthService,
                                MemberService memberService,
                                GoogleOAuthService googleOAuthService) {
        this.oAuthService = oAuthService;
        this.memberService = memberService;
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping("/google")
    // idToken 으로 email 과 name 파싱 및 존재하는 회원인지 검증
    public ResponseEntity<?> loginWithGoogle(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        GoogleInfoDto authenticate = oAuthService.authenticate(token);

        // 존재하는 유저라면 true 존재하지 않는다면 false 리턴
        boolean isExisting = memberService.googleOAuthValidateMember(authenticate.getEmail());

        if (isExisting) {
            // 찾았는데 없으면 에러 터짐 근데 무조건 true 여서 안터짐 그냥 optional 이어서 어쩔수 없이한거임
            Map<String, String> tokens = googleOAuthService.processUserLogin(authenticate);
            return ResponseEntity.ok(tokens);
        } else {
            // 구글 인증으로 얻은 email 과 name 을 반환한다
            Map<String, String> payload = Map.of(
                    "email", authenticate.getEmail(),
                    "name", authenticate.getName()
            );
            // 유저 정보와 상태코드 반환
            return new ResponseEntity<>(payload, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/google/code")
    public ResponseEntity<?> loginWithGoogleCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("code is required");
        }

        try {
            // 1. code -> access_token 교환
            String accessToken = googleOAuthService.getAccessTokenFromCode(code);

            // 2. access_token -> 사용자 정보 요청
            GoogleInfoDto userInfo = oAuthService.authenticate(accessToken);

            // 3. 기존 회원인지 확인
            boolean isExisting = memberService.googleOAuthValidateMember(userInfo.getEmail());

            if (isExisting) {
                Map<String, String> tokens = googleOAuthService.processUserLogin(userInfo);
                return ResponseEntity.ok(tokens);
            } else {
                return new ResponseEntity<>(
                        Map.of("email", userInfo.getEmail(), "name", userInfo.getName()),
                        HttpStatus.NOT_FOUND
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("구글 인증 실패: " + e.getMessage());
        }
    }
}
