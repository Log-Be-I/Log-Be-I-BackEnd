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
}
