package com.springboot.member.service;

import com.springboot.auth.service.GoogleOAuthService;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.category.entity.Category;
import com.springboot.category.repository.CategoryRepository;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.TestDataFactory;
import com.springboot.member.entity.DeletedMember;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.DeletedMemberRepository;
import com.springboot.member.repository.MemberRepository;
import com.springboot.oauth.GoogleInfoDto;
import com.springboot.question.entity.Question;
import com.springboot.utils.AuthorizationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private DeletedMemberRepository deletedMemberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private CustomAuthorityUtils customAuthorityUtils;

    @Test
    void createMember_successfullyCreatesMemberAndReturnsTokens() {
        // given
        Member testMember = TestDataFactory.createTestMember(1L);
        testMember.setRefreshToken("mock-refresh-token"); // refreshToken 설정

        Map<String, String> mockTokenMap = Map.of(
                "accessToken", "mock-token",
                "refreshToken", "mock-refresh-token"
        );

        when(customAuthorityUtils.createRoles(anyString()))
                .thenReturn(List.of("USER")); // ✅ roles mock

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(googleOAuthService.processUserLogin(
                any(GoogleInfoDto.class),
                anyString()
        )).thenReturn(mockTokenMap);

        // when
        Map<String, String> result = memberService.createMember(testMember);

        // then
        assertEquals("mock-token", result.get("accessToken"));
        assertEquals("mock-refresh-token", result.get("refreshToken"));
    }

    @Test
    void validateRejoinableMember_successfully() {
        // given
        String email = "test@example.com";
        LocalDateTime deletedAt = LocalDateTime.now().minusMonths(7); // 6개월 넘게 지난 경우
        DeletedMember deletedMember = new DeletedMember();
        deletedMember.setEmail(email);
        deletedMember.setDeletedAt(deletedAt);

        when(deletedMemberRepository.findByEmail(email))
                .thenReturn(Optional.of(deletedMember));

        // when & then
        assertDoesNotThrow(() -> memberService.validateRejoinableMember(email));
    }

    @Test
    void validateRejoinableMember_deleteMember() {
        // given
        String email = "test@example.com";
        LocalDateTime deletedAt = LocalDateTime.now().minusMonths(3); // 아직 6개월 안 됨
        DeletedMember deletedMember = new DeletedMember();
        deletedMember.setEmail(email);
        deletedMember.setDeletedAt(deletedAt);

        when(deletedMemberRepository.findByEmail(email))
                .thenReturn(Optional.of(deletedMember));

        // when & then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () ->
                memberService.validateRejoinableMember(email));

        assertEquals(ExceptionCode.CANCEL_MEMBERSHIP, exception.getExceptionCode());
    }

    @Test
    void validateRejoinableMember_succeeds_whenNoDeletedHistoryExists() {
        // given
        String email = "test@example.com";
        when(deletedMemberRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> memberService.validateRejoinableMember(email));
    }

    @Test
    void updateMember_successfullyUpdatesMember() {
        // given
        Member existingMember = new Member();
        existingMember.setMemberId(1L);
        existingMember.setEmail("test@example.com");
        existingMember.setNickname("oldNick");

        Member updateRequest = new Member();
        updateRequest.setNickname("newNick");

        given(memberRepository.findByMemberId(1L)).willReturn(Optional.of(existingMember));
        given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        Member updated = memberService.updateMember(updateRequest, 1L, "test@example.com");

        // then
        assertEquals("newNick", updated.getNickname());
    }

    @Test
    void updateMember_throwsForbidden_whenEmailMismatch() {
        // given
        Member existingMember = new Member();
        existingMember.setMemberId(1L);
        existingMember.setEmail("owner@example.com");

        Member updateRequest = new Member();
        updateRequest.setNickname("newNick");

        given(memberRepository.findByMemberId(1L)).willReturn(Optional.of(existingMember));

        // when & then
        assertThrows(BusinessLogicException.class, () -> {
            memberService.updateMember(updateRequest, 1L, "other@example.com");
        });
    }

    @Test
    void findVerifiedExistsMember_throwsException_whenNotFound() {
        // given
        given(memberRepository.findByMemberId(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(BusinessLogicException.class, () -> {
            memberService.findVerifiedExistsMember(999L);
        });
    }

    @Test
    void findMember_success_whenOwner() {
        // given
        Member member = new Member();
        member.setMemberId(1L);
        given(memberRepository.findByMemberId(1L)).willReturn(Optional.of(member));

        // AuthorizationUtils의 static 메서드 mocking
        try (MockedStatic<AuthorizationUtils> mocked = Mockito.mockStatic(AuthorizationUtils.class)) {
            mocked.when(() -> AuthorizationUtils.isOwner(1L, 1L)).thenReturn(true); // ✅ 소유자임을 명시
            mocked.when(AuthorizationUtils::isAdmin).thenReturn(false);            // ✅ 관리자는 아님

            // when
            Member result = memberService.findMember(1L, 1L);

            // then
            assertNotNull(result);
            assertEquals(1L, result.getMemberId());
        }
    }

    @Test
    void findMember_throwsException_whenNotOwnerAndNotAdmin() {
        try (MockedStatic<AuthorizationUtils> mockedAuth = Mockito.mockStatic(AuthorizationUtils.class)) {
            mockedAuth.when(() -> AuthorizationUtils.isAdminOrOwner(1L, 2L))
                    .thenCallRealMethod();
            mockedAuth.when(() -> AuthorizationUtils.isAdmin())
                    .thenReturn(false); // 관리자가 아님

            // when & then
            BusinessLogicException exception = assertThrows(
                    BusinessLogicException.class,
                    () -> memberService.findMember(1L, 2L)
            );
            assertEquals(ExceptionCode.UNAUTHORIZED_OPERATION, exception.getExceptionCode());
        }
    }

    @Test
    void findMembers_returnsPagedMembers_sortedByCreatedAtDescByDefault() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = null;  // 기본값: "createdAt"
        String order = null;   // 기본값: DESC

        List<Member> members = List.of(new Member(), new Member());
        Page<Member> mockPage = new PageImpl<>(members);

        when(memberRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // when
        Page<Member> result = memberService.findMembers(page, size, sortBy, order);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        // ✅ 정렬 조건까지 검증
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(memberRepository).findAll(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertEquals(page, captured.getPageNumber());
        assertEquals(size, captured.getPageSize());
        assertEquals(Sort.Direction.DESC, captured.getSort().getOrderFor("createdAt").getDirection());
    }

    @Test
    void findMembers_withSortAndAscOrder() {
        // given
        int page = 1;
        int size = 5;
        String sortBy = "email";
        String order = "asc";

        List<Member> members = List.of(new Member());
        Page<Member> mockPage = new PageImpl<>(members);

        when(memberRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // when
        Page<Member> result = memberService.findMembers(page, size, sortBy, order);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(memberRepository).findAll(pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertEquals(page, captured.getPageNumber());
        assertEquals(size, captured.getPageSize());
        assertEquals(Sort.Direction.ASC, captured.getSort().getOrderFor("email").getDirection());
    }

    @Test
    void findMembersToList_returnsAllMembers() {
        // given
        Member member1 = new Member();
        member1.setMemberId(1L);
        Member member2 = new Member();
        member2.setMemberId(2L);

        List<Member> mockMembers = List.of(member1, member2);
        when(memberRepository.findAll()).thenReturn(mockMembers);

        // when
        List<Member> result = memberService.findMembersToList(999L); // memberId는 현재 사용되지 않음

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(member1));
        assertTrue(result.contains(member2));

        // verify 호출 여부
        verify(memberRepository, times(1)).findAll();
    }


    @Test
    void findFilterMembers_filtersByEmailAndNameAndBirthAndStatusAndRegion() {
        // given
        Member member1 = new Member();
        member1.setEmail("test@example.com");
        member1.setName("Alice");
        member1.setBirth("1970-04-01");
        member1.setRegion("Seoul");
        member1.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);

        Member member2 = new Member();
        member2.setEmail("user@example.com");
        member2.setName("Bob");
        member2.setBirth("1980-01-01");
        member2.setRegion("Busan");
        member2.setMemberStatus(Member.MemberStatus.MEMBER_SLEEP);

        List<Member> members = List.of(member1, member2);

        Map<String, String> filters = new HashMap<>();
        filters.put("birth", "197");
        filters.put("memberStatus", "MEMBER_ACTIVE");

        // when
        List<Member> result = memberService.findFilterMembers(members, filters, "test", "Alice", "Seoul");

        // then
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
    }

    @Test
    void findFilterMembers_filtersByRegionOnly() {
        // given
        Member member1 = new Member();
        member1.setEmail("a@example.com");
        member1.setRegion("Seoul");

        Member member2 = new Member();
        member2.setEmail("b@example.com");
        member2.setRegion("Busan");

        List<Member> members = List.of(member1, member2);
        Map<String, String> filters = new HashMap<>();

        // when
        List<Member> result = memberService.findFilterMembers(members, filters, null, null, "Busan");

        // then
        assertEquals(1, result.size());
        assertEquals("b@example.com", result.get(0).getEmail());
    }

    @Test
    void findFilterMembers_noFilter_returnsAll() {
        // given
        Member member1 = new Member();
        member1.setEmail("x@example.com");

        Member member2 = new Member();
        member2.setEmail("y@example.com");

        List<Member> members = List.of(member1, member2);
        Map<String, String> filters = new HashMap<>();

        // when
        List<Member> result = memberService.findFilterMembers(members, filters, null, null, null);

        // then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("deleteMember - 본인 요청으로 회원 탈퇴 성공")
    void deleteMember_success_bySelf() throws Exception {
        // given
        Member member = TestDataFactory.createTestMember(1L);
        Question question = TestDataFactory.createTestQuestion(member);
        member.setQuestions(List.of(question));

        // 🛠️ adminEmail 설정
        Field field = MemberService.class.getDeclaredField("adminEmail");
        field.setAccessible(true);
        field.set(memberService, "admin@example.com");

        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));
        given(memberRepository.save(any(Member.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(deletedMemberRepository.save(any(DeletedMember.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        memberService.deleteMember(member.getEmail(), "탈퇴 요청");

        // then
        assertEquals(Member.MemberStatus.MEMBER_DELETEED, member.getMemberStatus());
        assertEquals(Question.QuestionStatus.QUESTION_DELETED, member.getQuestions().get(0).getQuestionStatus());
    }
}
