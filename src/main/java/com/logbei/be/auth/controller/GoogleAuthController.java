package com.logbei.be.auth.controller;

<<<<<<< HEAD:src/main/java/com/springboot/auth/controller/GoogleAuthController.java
import com.springboot.auth.dto.GoogleTokenResponse;
import com.springboot.auth.service.GoogleOAuthService;
import com.springboot.member.dto.MemberResponseDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.oauth.GoogleInfoDto;
import com.springboot.oauth.OAuthService;
import com.springboot.swagger.SwaggerErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
=======
import com.logbei.be.auth.service.GoogleOAuthService;
import com.logbei.be.member.dto.MemberResponseDto;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.mapper.MemberMapper;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.oauth.GoogleInfoDto;
import com.logbei.be.oauth.OAuthService;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/auth/controller/GoogleAuthController.java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private final OAuthService oAuthService;
    private final MemberService memberService;
    private final GoogleOAuthService googleOAuthService;
    private final MemberMapper memberMapper;

    public GoogleAuthController(OAuthService oAuthService,
                                MemberService memberService,
                                GoogleOAuthService googleOAuthService, MemberMapper memberMapper) {
        this.oAuthService = oAuthService;
        this.memberService = memberService;
        this.googleOAuthService = googleOAuthService;
        this.memberMapper = memberMapper;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "구글 로그인 코드로 토큰 요청",
        description = "Google OAuth 인증 코드(code)를 받아 accessToken과 유저 정보를 반환합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "구글 인증 코드 예시",
                    value = "{ \"code\": \"4/0AY0e-g7xxx_example_code\" }"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "성공적으로 로그인 처리됨"),
            @ApiResponse(responseCode = "400", description = "code 값이 없거나 비어있음"),
            @ApiResponse(responseCode = "500", description = "서버 오류"),
                @ApiResponse(responseCode = "500", description = "서버 오류",
                        content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                examples = @ExampleObject(value = "{\"error\": \"INVALID_SERVER_ERROR\", \"message\": \"INVALID_SERVER_ERROR\"}"))),
                @ApiResponse(responseCode = "400", description = "code 값이 없거나 비어있음",
                        content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                examples = @ExampleObject(value = "{\"error\": \"BAD_REQUEST\", \"message\": \"잘못된 요청 형태 접근\"}"))),
        }
    )
    @PostMapping("/google/code")
    public ResponseEntity<?> loginWithGoogleCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("code is required");
        }

        try {
            // 1. code 로 3 tokens 받기
            Map<String, String> googleTokens = googleOAuthService.getTokensFromCode(code);
            String idToken = googleTokens.get("id_token");
            String accessToken = googleTokens.get("access_token");
            String refreshToken = googleTokens.get("refresh_token");

            // 2. id_token 을 파싱해 유저 정보(email, name) 가져오기
            GoogleInfoDto userInfo = oAuthService.authenticate(idToken);

            // Redis에 Google AccessToken 저장
            googleOAuthService.saveAccessTokenToRedis(userInfo.getEmail(), accessToken);

            // 3. DB에 유저 존재 여부 확인
            boolean isExiting = memberService.googleOAuthValidateMember(userInfo.getEmail());

            if (isExiting) {
                // 4. 유저 로그인 처리 및 우리 토큰 발급
                Map<String, String> tokens = googleOAuthService.processUserLogin(userInfo, refreshToken);

                Member member = memberService.findMemberByEmail(userInfo.getEmail());
                MemberResponseDto memberResponseDto = memberMapper.memberToMemberResponseDto(member);

                return ResponseEntity.ok(Map.of(
                        "status", "login",
                        "token", tokens.get("accessToken"),
                        "user", memberResponseDto
                ));
            } else {
                // 6. 회원가입 필요 시 -> 이메일과 이름만 리턴, GoogleRefreshToken은 redis에 임시 저장
                googleOAuthService.saveTempRefreshToken(userInfo.getEmail(), refreshToken);
                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "status", "register",
                        "user", userInfo
                ));
            }
        } catch (HttpClientErrorException e) {
            System.out.println("💥 Google Token 요청 실패");
            System.out.println("🔸 상태 코드: " + e.getStatusCode());
            System.out.println("🔸 응답 본문: " + e.getResponseBodyAsString());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("구글 인증 실패 : " + e.getMessage());
        }
    }

    //test용 controller method
    @Hidden
    @PostMapping("/test")
    public ResponseEntity<?> testLogin() {
        GoogleInfoDto testUser = new GoogleInfoDto("taekho1225@gmail.com", "남택호");
        try {

            // 3. DB에 유저 존재 여부 확인
            Member member = memberService.findMemberByEmail(testUser.getEmail());

            Map<String, String> tokens = googleOAuthService.processUserLogin(testUser, member.getRefreshToken());

            MemberResponseDto memberResponseDto = memberMapper.memberToMemberResponseDto(member);

            return ResponseEntity.ok(Map.of(
                    "status", "login",
                    "token", tokens.get("accessToken"),
                    "user", memberResponseDto
            ));

        } catch (HttpClientErrorException e) {
            System.out.println("💥 Google Token 요청 실패");
            System.out.println("🔸 상태 코드: " + e.getStatusCode());
            System.out.println("🔸 응답 본문: " + e.getResponseBodyAsString());
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("구글 인증 실패 : " + e.getMessage());
        }
    }

//    @PostMapping("/google")
//    // idToken 으로 email 과 name 파싱 및 존재하는 회원인지 검증
//    public ResponseEntity<?> loginWithGoogle(@RequestHeader("Authorization") String authHeader) {
//        String token = authHeader.replace("Bearer ", "");
//        GoogleInfoDto authenticate = oAuthService.authenticate(token);
//
//        // 존재하는 유저라면 true 존재하지 않는다면 false 리턴
//        boolean isExisting = memberService.googleOAuthValidateMember(authenticate.getEmail());
//
//        if (isExisting) {
//            // 찾았는데 없으면 에러 터짐 근데 무조건 true 여서 안터짐 그냥 optional 이어서 어쩔수 없이한거임
//            Map<String, String> tokens = googleOAuthService.processUserLogin(authenticate);
//            return ResponseEntity.ok(tokens);
//        } else {
//            // 구글 인증으로 얻은 email 과 name 을 반환한다
//            Map<String, String> payload = Map.of(
//                    "email", authenticate.getEmail(),
//                    "name", authenticate.getName()
//            );
//            // 유저 정보와 상태코드 반환
//            return new ResponseEntity<>(payload, HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @PostMapping("/google/code")
//    public ResponseEntity<?> loginWithGoogleCode(@RequestBody Map<String, String> body) {
//        String code = body.get("code");
//        if (code == null || code.isBlank()) {
//            return ResponseEntity.badRequest().body("code is required");
//        }
//
//        try {
//            // 1. code -> access_token 교환
//            String accessToken = googleOAuthService.getAccessTokenFromCode(code);
//
//            // 2. access_token -> 사용자 정보 요청
//            GoogleInfoDto userInfo = oAuthService.authenticate(accessToken);
//
//            // 3. 기존 회원인지 확인
//            boolean isExisting = memberService.googleOAuthValidateMember(userInfo.getEmail());
//
//            if (isExisting) {
//                Map<String, String> tokens = googleOAuthService.processUserLogin(userInfo);
//                return ResponseEntity.ok(tokens);
//            } else {
//                return new ResponseEntity<>(
//                        Map.of("email", userInfo.getEmail(), "name", userInfo.getName()),
//                        HttpStatus.NOT_FOUND
//                );
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("구글 인증 실패: " + e.getMessage());
//        }
//    }
}
