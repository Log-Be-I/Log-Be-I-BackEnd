package com.springboot.member.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import com.springboot.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member createMember(Member member) {
        // 중복된 회원인지 이메일로 검증
        isMemberAlreadyRegistered(member.getEmail());
        // 중복된 닉네임인지 검증
        isNicknameAlreadyUsed(member);

        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);

        return memberRepository.save(member);
    }


    public Member updateMember(Member member, int memberId, MemberDetails memberDetails) {
        // MemberId 로 존재하는 회원인지 검증
        Member findMember = validateExistingMember(memberId);
        validateMemberStatus(findMember);

        // uri 에 있는 memberId 의 owner email 추출
        String isOwnerEmail = findMember.getEmail();

        // 만약 요청한 사용자의 이메일과 변경하고자하는 유저정보의 owner 의 이메일이 같다면 변경 실행
        if(Objects.equals(memberDetails.getEmail(), isOwnerEmail)){
            findMember.setNickname(
                    Optional.ofNullable(member.getNickname())
                            .orElse(findMember.getNickname()));
            findMember.setName(
                    Optional.ofNullable(member.getName())
                            .orElse(findMember.getName()));
            findMember.setMemberStatus(
                    Optional.ofNullable(member.getMemberStatus())
                            .orElse(findMember.getMemberStatus()));

        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }
        return memberRepository.save(findMember);
    }

    // 유저 단일 조회는 유저 본인과 관리자만 허용
    public Member findMember(int memberId, MemberDetails memberDetails) {
        // 유저 존재 확인
        Member findMember = validateExistingMember(memberId);

        // 유저 정보 owner 의 email 과 관리자 email 을 담은 리스트
        List<String> authentication = List.of(findMember.getEmail(), adminEmail);

        // 요청한 유저의 이메일과 비교하여 리스트에 동일한 이메일이 있는지 true / false
        boolean valuer = authentication.stream()
                .anyMatch(email -> Objects.equals(email, memberDetails.getEmail()));

        // 요청한 유저가 조회하고자 하는 유저 정보의 owner 와 동일 인물인지 또는 관리자인지 권한에 따른 접근 제한
        if(!valuer) {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        } else {
            return findMember;
        }
    }

    // 전체 조회는 관리자만 가능하다.
    public Page<Member> findMembers(int page, int size) {
        Page<Member> members = memberRepository.findAll(PageRequest.of(page, size, Sort.by("MemberId").ascending()));

        return members;
    }

    // 회원 삭제는 관리자와 유저 본인만 가능
    public void deleteMember(int memberId, MemberDetails memberDetails) {
        // 존재하는 회원인지 검증
        Member member = validateExistingMember(memberId);

        // 회원 상태가 활동중인지 검증
        // 활동중인 경우에만 삭제 가능 ( 보통 휴면계정 또한 휴면을 풀어야 삭제든 뭐든 가능)
        if(member.getMemberStatus() != Member.MemberStatus.ACTIVE_MEMBER){
            throw new BusinessLogicException(ExceptionCode.MEMBER_DEACTIVATED);
        }

        // uri 에 있는 memberId 의 owner email 추출
        String isOwnerEmail = member.getEmail();

        List<String> authentication = List.of(isOwnerEmail, adminEmail);

        // 만약 요청한 사용자의 이메일과 변경하고자하는 유저 정보의 owner 의 이메일이 동일하거나 admin 일 경우 변경 실행
        boolean value = authentication.stream().anyMatch(email -> Objects.equals(memberDetails.getEmail(), email));

        if(value){
            // 회원 상태 변경
            member.setMemberStatus(Member.MemberStatus.DEACTIVATED_MEMBER);
            // 회원이 작성한 질문글 상태 변경
            member.getQuestions().stream()
                    .forEach(question -> question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED));
        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }

        memberRepository.save(member);
    }

    // 이미 가입된 회원인지 중복 가입 예외처리
    public void isMemberAlreadyRegistered(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if(findMember.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.EXISTING_MEMBER);
        }
    }

    // 이미 사용중인 닉네임인지 닉네임 예외처리
    public void isNicknameAlreadyUsed(Member member) {
        Optional<Member> findMember = memberRepository.findByNickname(member.getNickname());
        if(findMember.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.NICKNAME_ALREADY_USED);
        }
    }

    // 가입된 회원인지 검증(id)
    public Member validateExistingMember(long memberId) {
        Optional<Member> findMember = memberRepository.findByMemberId(memberId);
        Member member = findMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return member;
    }

    // 가입된 회원인지 검증(email)
    public Member validateExistingMemberUsedEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        Member member = findMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return member;
    }

    // 회원 상태 검증
    public void validateMemberStatus(Member member) {
        if (member.getMemberStatus() == Member.MemberStatus.DORMANT_MEMBER) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_DORMANT);
        } else if(member.getMemberStatus() == Member.MemberStatus.DEACTIVATED_MEMBER) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_DEACTIVATED);
        }
    }

    // 구글 인증 정보로 유저 유무 확인
    public boolean googleOAuthValidateMember(String email) {
        // 이메일로 존재하는 회원인지 찾기
        Optional<Member> findMember = memberRepository.findByEmail(email);
        // 존재하는 유저라면 true 존재하지 않는다면 false 리턴
        return findMember.isPresent();
    }
}
