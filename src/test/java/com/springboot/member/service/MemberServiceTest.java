//package com.springboot.member.service;
//
//import com.springboot.auth.service.GoogleOAuthService;
//import com.springboot.auth.utils.CustomAuthorityUtils;
//import com.springboot.category.entity.Category;
//import com.springboot.category.repository.CategoryRepository;
//import com.springboot.exception.BusinessLogicException;
//import com.springboot.member.entity.DeletedMember;
//import com.springboot.member.entity.Member;
//import com.springboot.member.repository.DeletedMemberRepository;
//import com.springboot.member.repository.MemberRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MemberServiceTest {
//
//    @InjectMocks
//    private MemberService memberService;
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @Mock
//    private DeletedMemberRepository deletedMemberRepository;
//
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    @Mock
//    private GoogleOAuthService googleOAuthService;
//
//    @Mock
//    private CustomAuthorityUtils authorityUtils;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
//    }
//
//    @DisplayName("createMember - 신규 회원 가입 성공")
//    @Test
//    void createMember_success() {
//        // given
//        String email = "test@example.com";
//        String name = "테스트유저";
//        String refreshToken = "mockRefreshToken";
//
//        Member member = new Member();
//        member.setEmail(email);
//        member.setName(name);
//        member.setRefreshToken(refreshToken);
//
//        // 1. 탈퇴 회원 여부 확인 → 없음
//        when(deletedMemberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());
//
//        // ✅ 모든 findByEmail 호출 → 무조건 존재하는 member 반환
////        when(memberRepository.findByEmail(eq(member.getEmail())))
////                .thenReturn(Optional.of(member));
//        when(memberRepository.findByEmail(any()))
//                .thenAnswer(invocation -> {
//                    String arg = invocation.getArgument(0);
//                    System.out.println("💬 findByEmail() called with: " + arg);
//                    return Optional.of(member);
//                });
//
//        // 2. 권한 생성
//        when(authorityUtils.createRoles(member.getEmail())).thenReturn(List.of("USER"));
//
//        // 3. 카테고리 저장 mock
//        when(categoryRepository.save(any(Category.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // 4. member 저장
//        when(memberRepository.save(any(Member.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // 5. 구글 OAuth 로그인 처리
//        Map<String, String> mockLoginResponse = Map.of(
//                "accessToken", "mockAccessToken",
//                "refreshToken", "mockRefreshToken"
//        );
//        when(googleOAuthService.processUserLogin(any(), eq(refreshToken)))
//                .thenReturn(mockLoginResponse);
//
//        // when
//        Map<String, String> result = memberService.createMember(member);
//
//        // then
//        assertEquals("mockAccessToken", result.get("accessToken"));
//        assertEquals("mockRefreshToken", result.get("refreshToken"));
//
//        verify(deletedMemberRepository).findByEmail(email);
//        verify(memberRepository, atLeastOnce()).findByEmail(anyString());
//        verify(authorityUtils).createRoles(email);
//        verify(categoryRepository, times(5)).save(any(Category.class));
//        verify(memberRepository).save(any(Member.class));
//        verify(googleOAuthService).processUserLogin(any(), eq(refreshToken));
//    }
//
//    @DisplayName("isMemberAlreadyRegistered - 이미 가입된 이메일이면 예외 발생")
//    @Test
//    void isMemberAlreadyRegistered_alreadyExists_throwsException() {
//        // given
//        String email = "test@example.com";
//        Member existing = new Member();
//        existing.setEmail(email);
//
//        when(memberRepository.findByEmail(anyString()))
//                .thenAnswer(invocation -> {
//                    String arg = invocation.getArgument(0);
//                    System.out.println("💬 isMemberAlreadyRegistered()에서 findByEmail 호출됨: " + arg);
//                    return Optional.of(new Member());
//                });
//
//        // when & then
//        BusinessLogicException ex = assertThrows(BusinessLogicException.class, () -> {
//            memberService.isMemberAlreadyRegistered(email);
//        });
//        System.out.println("🔥 예외 발생 확인됨: " + ex.getMessage());
//    }
//
//    @DisplayName("validateRejoinableMember - 탈퇴 후 6개월 이내면 예외 발생")
//    @Test
//    void validateRejoinableMember_within6Months_throwsException() {
//        // given
//        String email = "test@example.com";
//        DeletedMember deletedMember = new DeletedMember();
//        deletedMember.setDeletedAt(LocalDateTime.now().minusMonths(2)); // 6개월 안 됨
//
//        when(deletedMemberRepository.findByEmail(email)).thenReturn(Optional.of(deletedMember));
//
//        // when & then
//        assertThrows(BusinessLogicException.class, () -> {
//            memberService.validateRejoinableMember(email);
//        });
//    }
//}