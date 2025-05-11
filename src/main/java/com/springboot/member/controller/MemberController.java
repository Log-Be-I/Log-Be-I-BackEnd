package com.springboot.member.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.member.dto.AdminPostDto;
import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.dto.MemberPostDto;
import com.springboot.member.dto.MemberResponseDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.schedule.dto.ScheduleResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Validated
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final StringRedisTemplate stringRedisTemplate;

    //swagger API - 등록
    @Operation(summary = "회원 등록", description = "회원가입과 로그인 동시 진행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 회원 등록"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "400", description = "입력 형식의 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"잘못된 입력 형태입니다.\"}")))
    })

    // 회원가입
    @PostMapping
    public ResponseEntity postMember(@Valid @RequestBody MemberPostDto memberPostDto) {
        Member member = memberMapper.memberPostDtoToMember(memberPostDto);

        // redis에서 임시 저장한 구글 refreshToken 꺼내기
        String googleRefreshToken = stringRedisTemplate.opsForValue()
                .get("temp:refreshToken:" + member.getEmail());

        // refreshToken 유효성검사
        if(googleRefreshToken ==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("구글 인증 정보가 만료되었거나 존재하지 않습니다..");
        }
        // refreshToken 설정
        member.setRefreshToken(googleRefreshToken);

        Map<String, String> tokens = memberService.createMember(member);
        // accessToken을 헤더에 추가
        HttpHeaders headers = new HttpHeaders();
        // refreshToken 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
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
        headers.set("Authorization", "Bearer " + tokens.get("accessToken"));
        headers.set(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        MemberResponseDto memberResponseDto = memberMapper.memberToMemberResponseDto(member);

        return new ResponseEntity<>(new SingleResponseDto<>(memberResponseDto), headers, HttpStatus.CREATED);
    }

    //swagger API - 앱 푸쉬 알림 수신동의
    @Operation(summary = "알림 수신동의", description = "회원의 푸쉬 알림 수신 동의 내역")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원의 푸쉬 알림 수신 변경"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })

    @PatchMapping("/{member-id}/notification")
    //앱 푸쉬 알림 수신동의 여부 저장
    public ResponseEntity setNotificationConsent(@PathVariable("member-id") @Positive long memberId,
                                                 @Valid  @RequestParam("notification") boolean notification){
        memberService.updateNotificationConsent(memberId, notification);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //swagger API - 수정
    @Operation(summary = "회원 수정", description = "회원정보 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 회원 정보 수정"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "400", description = "입력 형식의 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"잘못된 입력 형태입니다.\"}")))
    })

    // 회원 수정
    @PatchMapping("/{member-id}")
    public ResponseEntity patchMember(@Valid @RequestBody MemberPatchDto patchDto,
                                      @Parameter(description = "수정할 멤버의 ID", example = "1")
                                      @PathVariable("member-id") long memberId,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Member uodateMember = memberService.updateMember(memberMapper.memberPatchDtoToMember(patchDto), memberId, customPrincipal.getEmail());
        return new ResponseEntity<>(new SingleResponseDto<>(
                memberMapper.memberToMemberResponseDto(uodateMember)),HttpStatus.OK);
    }

    //swagger API - 조회
    @Operation(summary = "회원 단일 조회", description = "회원정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "404", description = "조회 가능한 회원이 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"조회하신 회원이 없습니다.\"}")))
    })

    // 회원 단일 조회
    @GetMapping("/{member-id}")
    public ResponseEntity getMember(@Parameter(description = "조회할 멤버의 ID", example = "1")
                                        @Valid @PathVariable("member-id") long memberId,
                                    @Parameter(hidden = true)
                                    @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Member member = memberService.findMember(memberId, customPrincipal.getMemberId());
        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.memberToMemberResponseDto(member)), HttpStatus.OK
        );
    }

    //swagger API - 조회
    @Operation(summary = "회원 전체 조회", description = "회원정보 전체 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MemberResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "404", description = "조회 가능한 회원이 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"조회하신 회원이 없습니다.\"}")))
    })
    // 회원 전체 조회
    @GetMapping
    public ResponseEntity getMembers(@Parameter(description = "page", example = "1")
                                         @Positive @RequestParam(value = "page") int page,
                                     @Parameter(description = "size", example = "5")
                                     @Positive @RequestParam(value = "size") int size,
                                     @Parameter(description = "정렬 조건", example = "ASC")
                                     @RequestParam(value = "sortBy", required = false) String sortBy,
                                     @Parameter(description = "필터 조건", example = "birth")
                                     @RequestParam(value = "order", required = false) String order,
                                     @RequestParam(value = "memberStatus", required = false) String memberStatus,
                                     @RequestParam(value = "birth", required = false) String birth,
                                     @RequestParam(value = "email", required = false) String email,
                                     @RequestParam(value =  "name", required = false) String name,
                                     @RequestParam(value = "region", required = false) String region) {

        Map<String, String> filters = new HashMap<>();
        // 출생년도 조건이 들어왔을때
        if (birth != null) {
            String year = birth.substring(0, 3);
            filters.put("birth", year);
        }
        // 상태 조건이 들어왔을때
        if (memberStatus != null) {
            filters.put("memberStatus", memberStatus);
        }

        Page<Member> pageMember = memberService.findMembers(page-1, size, sortBy, order);
        List<Member> members = memberService.findFilterMembers(pageMember.getContent(), filters, email, name, region);

        return new ResponseEntity<>(
                new MultiResponseDto<>(memberMapper.membersToMemberResponseDtos(members), pageMember), HttpStatus.OK
        );
    }
    //swagger API - 삭제
    @Operation(summary = "회원 삭제", description = "회원 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제되었습니다"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}"))),
            @ApiResponse(responseCode = "404", description = "이미 탈퇴한 회원입니다.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Bad Request\", \"message\": \"가입중인 회원이 아닙니다.\"}")))
    })
    // 회원 삭제
    @DeleteMapping("/{member-id}")
    public ResponseEntity deleteMember(@Parameter(description = "삭제할 회원 ID", example = "1")
                                           @Valid @PathVariable("member-id") long memberId,
                                       @Parameter(hidden = true)
                                       @AuthenticationPrincipal CustomPrincipal customPrincipal,
                                       @RequestBody String request) {
        memberService.deleteMember(customPrincipal.getEmail(), request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
