package com.springboot.auth.controller;

import com.springboot.auth.service.GoogleOAuthService;
import com.springboot.member.service.MemberService;
import com.springboot.oauth.GoogleInfoDto;
import com.springboot.oauth.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> loginWithGoogle(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        GoogleInfoDto authenticate = oAuthService.authenticate(token);

        boolean isExisting = memberService.googleOAuthValidateMember(authenticate.getEmail());

        if (isExisting) {
            Map<String, String> tokens = googleOAuthService.processUserLogin(authenticate);
            return ResponseEntity.ok(tokens);
        } else {
            // 유저 정보와 상태코드 반환
            Map<String, String> payload = Map.of(
                    "email", authenticate.getEmail(),
                    "name", authenticate.getName()
            );
            return new ResponseEntity<>(payload, HttpStatus.NOT_FOUND);
        }
    }
}
