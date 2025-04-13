package com.springboot.member.controller;

import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.dto.MemberPostDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    // 회원가입
    @PostMapping
    public ResponseEntity postMember(@Valid @RequestBody MemberPostDto requestBody) {
        Member member = memberMapper.memberPostDtoToMember(requestBody);
        memberService.createMember(member);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
    public ResponseEntity getMembers(@Positive @RequestParam("page") int page,
                                     @Positive @RequestParam("size") int size) {

        Page<Member> pageMember = memberService.findMembers(page-1, size);

        List<Member> members = pageMember.getContent();

        return new ResponseEntity<>(
                new MultiResponseDto<>(memberMapper.membersToMemberResponseDtos(members), pageMember), HttpStatus.OK
        );
    }

    // 회원 삭제
    @DeleteMapping("/{memberId}")
    public ResponseEntity deleteMember(@Valid @PathVariable("memberId") int memberId,
                                       @AuthenticationPrincipal MemberDetails memberDetails) {
        memberService.deleteMember(memberId, memberDetails);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
