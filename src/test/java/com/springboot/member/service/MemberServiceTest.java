//package com.springboot.member.service;
//
//import com.nimbusds.oauth2.sdk.token.AccessToken;
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
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MemberServiceTest {
//
//    @InjectMocks
//    private MemberService memberService;            // class under test, with mocks injected
//
//    @Mock
//    private GoogleOAuthService googleOAuthService;
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @MockBean
//    private DeletedMemberRepository deletedMemberRepository;
//
//    @MockBean
//    private CategoryRepository categoryRepository;
//
//    @MockBean
//    private CustomAuthorityUtils authorityUtils;
//
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void createMember_ReturnsAccessToken_WhenUserLogsIn() {
//        // Arrange: define input and expected output
//        String authCode = "test-auth-code";
//        AccessToken mockAccessToken = new AccessToken("mock-token-value", "...");  // dummy token object or use String if appropriate
//
//        // Stub GoogleOAuthService.processUserLogin to prevent actual logic from running
//        when(googleOAuthService.processUserLogin(anyString()))
//                .thenReturn(mockAccessToken);
//        // If we were using a real GoogleOAuthService (not mocked), we would also stub its internal repo call:
//        when(memberRepository.findByEmail(anyString()))
//                .thenReturn(Optional.empty());  // e.g. assume no existing user for simplicity
//
//        // Act: call the service method
//        AccessToken result = memberService.createMember(authCode);
//
//        // Assert: verify the result comes from the mocked GoogleOAuthService
//        assertNotNull(result);
//        assertEquals(mockAccessToken, result);
//        verify(googleOAuthService, times(1)).processUserLogin(authCode);
//        // (No actual interaction with Redis or real DB occurs thanks to mocking)
//    }
//
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
//        when(deletedMemberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//        // 💥 핵심 변경: 2번 호출될 것을 순서대로 설정
//        when(memberRepository.findByEmail(email))
//                .thenReturn(Optional.empty())  // 중복 확인
//                .thenReturn(Optional.of(member));  // processUserLogin 내부
//
//        when(authorityUtils.createRoles(email)).thenReturn(List.of("USER"));
//        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
//        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));
//
//        Map<String, String> mockLoginResponse = Map.of(
//                "accessToken", "mockAccessToken",
//                "refreshToken", "mockRefreshToken"
//        );
//
//        when(googleOAuthService.processUserLogin(any(), eq(refreshToken)))
//                .thenReturn(mockLoginResponse);
//
//        // when
//        Map<String, String> result = memberService.createMember(member);
//
//        // then
//        assertEquals("mockAccessToken", result.get("accessToken"));
//        assertEquals("mockRefreshToken", result.get("refreshToken"));
//    }
//    @DisplayName("validateRejoinableMember - 탈퇴 후 6개월 이내이면 예외")
//    @Test
//    void validateRejoinableMember_within6Months_throwsException() {
//        String email = "deleted@example.com";
//        DeletedMember deletedMember = new DeletedMember();
//        deletedMember.setDeletedAt(LocalDateTime.now().minusMonths(2));
//        when(deletedMemberRepository.findByEmail(email)).thenReturn(Optional.of(deletedMember));
//
//        assertThrows(BusinessLogicException.class, () -> {
//            memberService.validateRejoinableMember(email);
//        });
//    }
//
//    @DisplayName("isMemberAlreadyRegistered - 중복 이메일이면 예외")
//    @Test
//    void isMemberAlreadyRegistered_alreadyExists_throwsException() {
//        String email = "existing@example.com";
//        Member existing = new Member();
//        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existing));
//
//        assertThrows(BusinessLogicException.class, () -> {
//            memberService.isMemberAlreadyRegistered(email);
//        });
//    }
//}
