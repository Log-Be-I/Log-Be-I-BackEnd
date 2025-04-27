package com.springboot.member.service;

import com.springboot.auth.service.GoogleOAuthService;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.category.entity.Category;
import com.springboot.category.repository.CategoryRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.DeletedMember;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.DeletedMemberRepository;
import com.springboot.member.repository.MemberRepository;
import com.springboot.oauth.GoogleInfoDto;
import com.springboot.question.entity.Question;
import com.springboot.record.entity.Record;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CustomAuthorityUtils authorityUtils;
    private final DeletedMemberRepository deletedMemberRepository;
    private final CategoryRepository categoryRepository;
    private final GoogleOAuthService googleOAuthService;

//    @Value("${mail.address.admin}")
    private String adminEmail;

    public Map<String, String> createMember(Member member) {
        // 탈퇴한 내역이 있는지 있다면 재가입 가능한지 검증
        validateRejoinableMember(member);
        // 중복된 회원인지 이메일로 검증
        isMemberAlreadyRegistered(member.getEmail());
        // 중복된 닉네임인지 검증
        isNicknameAlreadyUsed(member);

        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);
        List<Record> records = new ArrayList<>();
        List<String> categoryNames = List.of("일상", "소비", "건강", "할 일", "기타");
        List<Category> categoryList = categoryNames.stream()
                .map(categoryName -> new Category(categoryName, "url", member, true))
                .collect(Collectors.toList());
        categoryList.stream().map(category -> categoryRepository.save(category));

        member.setCategories(categoryList);
        memberRepository.save(member);

        return googleOAuthService.processUserLogin(new GoogleInfoDto(member.getEmail(), member.getName()), member.getRefreshToken());

    }

    public Member updateMember(Member member, long memberId, String email) {
        // MemberId 로 존재하는 회원인지 검증
        Member findMember = validateExistingMember(memberId);
        validateMemberStatus(findMember);

        // uri 에 있는 memberId 의 owner email 추출
        String isOwnerEmail = findMember.getEmail();

        // 만약 요청한 사용자의 이메일과 변경하고자하는 유저정보의 owner 의 이메일이 같다면 변경 실행
        if (Objects.equals(email, isOwnerEmail)) {
            findMember.setNickname(
                    Optional.ofNullable(member.getNickname())
                            .orElse(findMember.getNickname()));
            findMember.setProfile(
                    Optional.ofNullable(member.getProfile())
                            .orElse(findMember.getProfile()));
            findMember.setRegion(
                    Optional.ofNullable(member.getRegion())
                            .orElse(findMember.getRegion()));
            findMember.setBirth(
                    Optional.ofNullable(member.getBirth())
                            .orElse(findMember.getBirth()));
            findMember.setNotification(
                    // Lombok 은 boolean 타입을 get 이 아닌 Is로 호출한다
                    Optional.ofNullable(member.isNotification())
                            .orElse(findMember.isNotification()));
            findMember.setMemberStatus(
                    Optional.ofNullable(findMember.getMemberStatus())
                            .orElse(findMember.getMemberStatus()));
        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }
        return memberRepository.save(findMember);
    }

    // 유저 단일 조회는 유저 본인과 관리자만 허용
    public Member findMember(long memberId, long currentMemberId) {
        // 유저 존재 확인
        AuthorizationUtils.isAdminOrOwner(memberId, currentMemberId);
        return validateExistingMember(memberId);

//        // 유저 정보 owner 의 email 과 관리자 email 을 담은 리스트
//        List<String> authentication = List.of(findMember.getEmail(), adminEmail);
//
//        // 요청한 유저의 이메일과 비교하여 리스트에 동일한 이메일이 있는지 true / false
//        boolean valuer = authentication.stream()
//                .anyMatch(email -> Objects.equals(email, memberDetails.getEmail()));
//
//        // 요청한 유저가 조회하고자 하는 유저 정보의 owner 와 동일 인물인지 또는 관리자인지 권한에 따른 접근 제한
//        if(!valuer) {
//            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
//        } else {
//            return findMember;
//        }
    }

    // 전체 조회는 관리자만 가능하다.
    public Page<Member> findMembers(int page, int size, String sortBy, String order) {
        // 정렬 조건 (삼항연산자로 내림, 오름차순 선택)
        Sort.Direction direction = order != null ? Sort.Direction.fromString(order) : Sort.Direction.DESC;

        // 페이지네이션 양식 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy == null ? "createdAt" : sortBy));

        Page<Member> members = memberRepository.findAll(pageable);

        return members;
    }

    // 조건에 맞춘 회원 검색 결과
    public List<Member> findFilterMembers (List<Member> members, Map<String, String> filters, String email, String name ) {
        List<Member> filteredMember = new ArrayList<>(members);
        // 검색 조건
        // email 과 name 전부 들어왔다면
        if(email != null && name != null) {
            filteredMember = filteredMember.stream().filter(member -> {
                Objects.equals(member.getEmail(), email);
                Objects.equals(member.getName(), name);
                return true;
            }).collect(Collectors.toList());

            // email 만 들어왔을 때
        } else if (email != null){
            filteredMember = filteredMember.stream().filter(member ->
                            Objects.equals(member.getEmail(), email))
                    .collect(Collectors.toList());

            // name 만 들어왔을 때
        } else if (name != null) {
            filteredMember = filteredMember.stream().filter(member ->
                            Objects.equals(member.getName(), name))
                    .collect(Collectors.toList());
        }
        // "birth" 조건이 있을때만 필터링 ex) 1970 ~ 1979
        if(filters.get("birth") != null){
            filteredMember = filteredMember.stream().filter(member ->
                            member.getBirth().substring(0, 3).equals(filters.get("birth")))
                    .collect(Collectors.toList());
        }
        // memberStatus 로 필터링
        if(filters.get("memberStatus") != null) {
            filteredMember = filteredMember.stream().filter(member ->
                            member.getMemberStatus().equals(filters.get("memberStatus")))
                    .collect(Collectors.toList());
        }
        return filteredMember;
    }

    // 회원 삭제는 관리자와 유저 본인만 가능
    @Transactional
    public void deleteMember(long memberId, long loginMemberId, String memberEmail, String response) {
        // 존재하는 회원인지 검증
        Member member = validateExistingMember(memberId);
        AuthorizationUtils.isAdminOrOwner(memberId, loginMemberId);
        //memberId와 email이 같은 회원의 정보인지 확인
        checkMemberIdentityByIdAndEmail(member, memberEmail);
        // 회원 상태가 활동중인지 검증
        // 활동중인 경우에만 삭제 가능 ( 보통 휴면계정 또한 휴면을 풀어야 삭제든 뭐든 가능)
        validateMemberStatus(member);

//        // uri 에 있는 memberId 의 owner email 추출
//        String isOwnerEmail = member.getEmail();

        List<String> authentication = List.of(memberEmail, adminEmail);

        // 만약 요청한 사용자의 이메일과 변경하고자하는 유저 정보의 owner 의 이메일이 동일하거나 admin 일 경우 변경 실행
        boolean value = authentication.stream().anyMatch(email -> Objects.equals(memberEmail, email));
        //탈퇴 요청을 보낸 회원이 사용자 본인 또는 관리자라면 true
        if (value) {
            // 회원 상태 변경
            member.setMemberStatus(Member.MemberStatus.MEMBER_DELETEED);
            // 회원이 작성한 질문글 상태 변경
            member.getQuestions().stream()
                    .forEach(question -> question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED));
            memberRepository.save(member);
            // DeletedMember 에 post
            DeletedMember deletedMember = new DeletedMember();
            deletedMember.setMemberId(member.getMemberId());
            deletedMember.setEmail(memberEmail);
            deletedMember.setReason(response);
            deletedMemberRepository.save(deletedMember);


        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }


    }

    // 이미 가입된 회원인지 중복 가입 예외처리
    public void isMemberAlreadyRegistered(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.EXISTING_MEMBER);
        }
    }

    // 이미 사용중인 닉네임인지 닉네임 예외처리
    public void isNicknameAlreadyUsed(Member member) {
        Optional<Member> findMember = memberRepository.findByNickname(member.getNickname());
        if (findMember.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.NICKNAME_ALREADY_USED);
        }
    }

    // 가입된 회원인지 검증(id)
    public Member validateExistingMember(long memberId) {
        Optional<Member> findMember = memberRepository.findByMemberId(memberId);
        return findMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

    }

    // 가입된 회원인지 검증(email)
    public Member validateExistingMemberUsedEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        Member member = findMember.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return member;
    }

    //memberId로 찾은 회원과 email로 찾은 회원이 서로 같지않다면 예외발생
    public void checkMemberIdentityByIdAndEmail(Member member, String memberEmail) {
        Member wishDeleteMember = validateExistingMemberUsedEmail(memberEmail);
        if (!member.getEmail().equals(wishDeleteMember.getEmail())) {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }
    }

    // 회원 상태 검증
    public void validateMemberStatus(Member member) {
        if (member.getMemberStatus() == Member.MemberStatus.MEMBER_DELETEED) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_DELETED);

        } else if (member.getMemberStatus() != Member.MemberStatus.MEMBER_ACTIVE) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_DEACTIVATED);
        }

    }

    // 탈퇴 회원 재가입 가능한지 검증
    public void validateRejoinableMember(Member member) {
        // 회원 id 로 탈퇴 내역있는지 조회
        Optional<DeletedMember> deletedMember = deletedMemberRepository.findByEmail(member.getEmail());
        // 탈퇴한 내역이 있다면
        if (deletedMember.isPresent()) {
            //탈퇴 후 6개월이 지나지 않았다면 회원가입 불가
            if (LocalDateTime.now().isBefore(deletedMember.get().getDeletedAt().plusMonths(6))) {
                throw new BusinessLogicException(ExceptionCode.CANCEL_MEMBERSHIP);

            }
        }
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND)
        );
    }

    // 구글 인증 정보로 유저 유무 확인
    public boolean googleOAuthValidateMember(String email) {
        // 이메일로 존재하는 회원인지 찾기
        Optional<Member> findMember = memberRepository.findByEmail(email);
        // 존재하는 유저라면 true 존재하지 않는다면 false 리턴
        return findMember.isPresent();
    }

    //검증 로직 : 회원가입 직후에 사용자에게 앱 푸쉬알림 허용 여부 받기
    public void updateNotificationConsent(long memberId, boolean notification) {
        Member findMember = validateExistingMember(memberId);

        findMember.setNotification(notification);
        //저장
        memberRepository.save(findMember);
    }
}
