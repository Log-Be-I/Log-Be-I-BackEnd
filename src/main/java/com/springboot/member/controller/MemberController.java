package com.springboot.member.controller;

import com.springboot.auth.utils.MemberDetails;
import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.dto.MemberPostDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    // 회원가입
    @PostMapping
    public ResponseEntity postMember(@Valid @RequestBody MemberPostDto memberPostDto) {
        Member member = memberMapper.memberPostDtoToMember(memberPostDto);
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

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PatchMapping("/notification/{member-id}")
    //앱 푸쉬 알림 수신동의 여부 저장
    public ResponseEntity setNotificationConsent(@PathVariable("member-id") @Positive long memberId,
                                                 @Valid  @RequestParam boolean notification){
        memberService.updateNotificationConsent(memberId, notification);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 회원 수정
    @PatchMapping("/{memberId}")
    public ResponseEntity patchMember(@Valid @RequestBody MemberPatchDto requestBody,
                                      @PathVariable("memberId") int memberId,
                                      @AuthenticationPrincipal MemberDetails memberDetails) {
        Member member = memberMapper.memberPatchDtoToMember(requestBody);

        memberService.updateMember(member, memberId, memberDetails);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 회원 단일 조회
    @GetMapping("/{memberId}")
    public ResponseEntity getMember(@Valid @PathVariable("memberId") int memberId,
                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = memberService.findMember(memberId, memberDetails);
        return new ResponseEntity<>(
                new SingleResponseDto<>(memberMapper.memberToMemberResponseDto(member)), HttpStatus.OK
        );
    }

    // 회원 전체 조회
    @GetMapping
    public ResponseEntity getMembers(@Positive @RequestParam(value = "page") int page,
                                     @Positive @RequestParam(value = "size") int size,
                                     @RequestParam(value = "sortBy") String sortBy,
                                     @RequestParam(value = "order") String order,
                                     @RequestParam(value = "member_Status", required = false) String memberStatus,
                                     @RequestParam(value = "birth", required = false) String birth,
                                     @RequestParam(value = "email", required = false) String email,
                                     @RequestParam(value =  "name", required = false) String name) {
        Map<String, String> filters = new HashMap<>();
        if (birth != null) filters.put("birth", birth);
        if (email != null) filters.put("email", email);
        if (name != null) filters.put("name", name);
        if (memberStatus != null) filters.put("memberStatus", memberStatus);

        Page<Member> pageMember = memberService.findMembers(page-1, size, sortBy, order, filters);

        List<Member> members = pageMember.getContent();

        return new ResponseEntity<>(
                new MultiResponseDto<>(memberMapper.membersToMemberResponseDtos(members), pageMember), HttpStatus.OK
        );
    }

    // 회원 삭제
    @DeleteMapping("/{memberId}")
    public ResponseEntity deleteMember(@Valid @PathVariable("memberId") int memberId,
                                       @AuthenticationPrincipal MemberDetails memberDetails,
                                       @RequestBody String request) {
        memberService.deleteMember(memberId, memberDetails, request);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
