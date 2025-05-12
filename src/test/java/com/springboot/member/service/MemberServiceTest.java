//package com.springboot.member.service;
//
//import com.springboot.auth.service.GoogleOAuthService;
//import com.springboot.auth.utils.CustomAuthorityUtils;
//import com.springboot.category.entity.Category;
//import com.springboot.category.repository.CategoryRepository;
//import com.springboot.exception.BusinessLogicException;
//import com.springboot.exception.ExceptionCode;
//import com.springboot.member.TestDataFactory;
//import com.springboot.member.entity.DeletedMember;
//import com.springboot.member.entity.Member;
//import com.springboot.member.repository.DeletedMemberRepository;
//import com.springboot.member.repository.MemberRepository;
//import com.springboot.oauth.GoogleInfoDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
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
//    private CustomAuthorityUtils customAuthorityUtils;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void createMember_successfullyCreatesMemberAndReturnsTokens() {
//        // given
//        Member testMember = TestDataFactory.createTestMember(1L);
////        testMember.setRoles(List.of("USER")); // ✅ 여기서 직접 설정
//        when(customAuthorityUtils.createRoles(anyString()))
//                .thenReturn(List.of("USER")); // ✅ roles mock
//
//        when(categoryRepository.save(any(Category.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        when(memberRepository.save(any(Member.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        when(googleOAuthService.processUserLogin(
//                any(GoogleInfoDto.class),
//                anyString()
//        )).thenReturn(Map.of("accessToken", "mock-token", "refreshToken", "mock-refresh"));
//        // when
//        Map<String, String> result = memberService.createMember(testMember);
//
//        // then
//        assertEquals("mock-token", result.get("accessToken"));
//    }
//
//    @Test
//    void validateRejoinableMember_successfully() {
//        // given
//        String email = "test@example.com";
//        LocalDateTime deletedAt = LocalDateTime.now().minusMonths(7); // 6개월 넘게 지난 경우
//        DeletedMember deletedMember = new DeletedMember();
//        deletedMember.setEmail(email);
//        deletedMember.setDeletedAt(deletedAt);
//
//        when(deletedMemberRepository.findByEmail(email))
//                .thenReturn(Optional.of(deletedMember));
//
//        // when & then
//        assertDoesNotThrow(() -> memberService.validateRejoinableMember(email));
//    }
//
//    @Test
//    void validateRejoinableMember_deleteMember () {
//        // given
//        String email = "test@example.com";
//        LocalDateTime deletedAt = LocalDateTime.now().minusMonths(3); // 아직 6개월 안 됨
//        DeletedMember deletedMember = new DeletedMember();
//        deletedMember.setEmail(email);
//        deletedMember.setDeletedAt(deletedAt);
//
//        when(deletedMemberRepository.findByEmail(email))
//                .thenReturn(Optional.of(deletedMember));
//
//        // when & then
//        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () ->
//                memberService.validateRejoinableMember(email));
//
//        assertEquals(ExceptionCode.CANCEL_MEMBERSHIP, exception.getExceptionCode());
//    }
//
//    @Test
//    void validateRejoinableMember_succeeds_whenNoDeletedHistoryExists() {
//        // given
//        String email = "test@example.com";
//        when(deletedMemberRepository.findByEmail(email))
//                .thenReturn(Optional.empty());
//
//        // when & then
//        assertDoesNotThrow(() -> memberService.validateRejoinableMember(email));
//    }
//}
