package com.logbei.be.answer.service;

import com.logbei.be.answer.entity.Answer;
import com.logbei.be.answer.repository.AnswerRepository;
import com.logbei.be.exception.exception.BusinessLogicException;
import com.logbei.be.exception.exception.ExceptionCode;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.question.entity.Question;
import com.logbei.be.question.service.QuestionService;
import com.logbei.be.utils.AuthorizationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private AnswerService answerService;

    @Test
    void createAnswer_success() {
        // given
        Long adminId = 1L;
        Long questionId = 2L;

        Member mockAdmin = new Member();
        mockAdmin.setMemberId(adminId);

        Question mockQuestion = new Question();
        mockQuestion.setQuestionId(questionId);
        mockQuestion.setAnswer(null); // 아직 답변 없음

        Answer answer = new Answer();
        Question innerQuestion = new Question();
        innerQuestion.setQuestionId(questionId);
        answer.setQuestion(innerQuestion);

        // ✅ 실제 스프링 인증 구조와 유사한 객체로 SecurityContext 구성
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockAdmin", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContext mockContext = mock(SecurityContext.class);
        when(mockContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            // static 메서드 verifyAdmin()은 실제 로직 호출
            try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
                utilities.when(AuthorizationUtils::verifyAdmin).thenCallRealMethod();

                // stub: 의존 서비스 동작
                given(memberService.findVerifiedExistsMember(adminId)).willReturn(mockAdmin);
                given(questionService.findVerifiedExistsQuestion(questionId)).willReturn(mockQuestion);
                given(answerRepository.save(any(Answer.class))).willAnswer(invocation -> invocation.getArgument(0));

                // when
                Answer result = answerService.createAnswer(answer, adminId);

                // then
                assertEquals(mockAdmin, result.getMember());
                assertEquals(mockQuestion, result.getQuestion());
                assertEquals(Question.QuestionAnswerStatus.DONE_ANSWER, mockQuestion.getQuestionAnswerStatus());

                verify(memberService).findVerifiedExistsMember(adminId);
                verify(questionService).findVerifiedExistsQuestion(questionId);
                verify(answerRepository).save(any(Answer.class));
            }
        }
    }

    @Test
    void createAnswer_shouldThrowException_whenAnswerAlreadyExists() {
        // given
        Long adminId = 1L;
        Long questionId = 2L;

        Answer answer = new Answer();
        Question attachedQuestion = new Question();
        attachedQuestion.setQuestionId(questionId);
        answer.setQuestion(attachedQuestion);

        Question mockQuestion = new Question();
        mockQuestion.setQuestionId(questionId);
        mockQuestion.setAnswer(new Answer());  // 이미 답변이 존재하는 경우

        // ✅ 관리자 권한을 가진 인증 객체 구성
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockAdmin", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContext mockContext = mock(SecurityContext.class);
        when(mockContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            try (MockedStatic<AuthorizationUtils> utilities = mockStatic(AuthorizationUtils.class)) {
                utilities.when(AuthorizationUtils::verifyAdmin).thenCallRealMethod();

                given(memberService.findVerifiedExistsMember(adminId)).willReturn(new Member());
                given(questionService.findVerifiedExistsQuestion(questionId)).willReturn(mockQuestion);

                // when & then
                BusinessLogicException e = assertThrows(BusinessLogicException.class,
                        () -> answerService.createAnswer(answer, adminId));

                assertEquals(ExceptionCode.ANSWER_EXISTS, e.getExceptionCode());

                verify(questionService).findVerifiedExistsQuestion(questionId);
                verify(answerRepository, never()).save(any());
            }
        }
    }
    @Test
    void verifyAdmin_shouldPass_whenRoleIsAdmin() {
        // given
        Authentication auth = mock(Authentication.class);
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        List<GrantedAuthority> authorities = Collections.singletonList(authority);

        when(auth.getAuthorities()).thenReturn((Collection) authorities);
        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(auth);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // when & then
            assertDoesNotThrow(AuthorizationUtils::verifyAdmin);
        }
    }


    @Test
    void verifyAdmin_shouldThrow_whenRoleIsNotAdmin() {
        // given: ROLE_USER만 있는 Authentication 구성
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockUser", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        // when: SecurityContextHolder.getContext()가 위 context를 반환하도록 설정
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            // then: verifyAdmin() 호출 시 예외가 발생해야 함
            BusinessLogicException ex = assertThrows(
                    BusinessLogicException.class,
                    AuthorizationUtils::verifyAdmin
            );
            assertEquals(ExceptionCode.UNAUTHORIZED_OPERATION, ex.getExceptionCode());
        }
    }
    @Test
    void updateAnswer_success_byAdmin() {
        // given
        Long adminId = 1L;
        Long answerId = 10L;

        // 관리자 역할을 가진 Member
        Member admin = new Member();
        admin.setMemberId(adminId);

        // 기존 저장된 Answer
        Answer existingAnswer = new Answer();
        existingAnswer.setAnswerId(answerId);
        existingAnswer.setMember(admin);
        existingAnswer.setAnswerStatus(Answer.AnswerStatus.ANSWER_REGISTERED);

        // 수정 요청용 Answer
        Answer updatedInput = new Answer();
        updatedInput.setAnswerId(answerId);
        updatedInput.setContent("수정된 내용");

        // ✅ SecurityContextHolder mock 설정 (ROLE_ADMIN 포함)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockAdmin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContext mockContext = mock(SecurityContext.class);
        when(mockContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            // 👉 AuthorizationUtils는 mock 하지 않음 (실제 로직 호출)

            // stub 설정
            given(answerRepository.findById(answerId)).willReturn(Optional.of(existingAnswer));
            given(answerRepository.save(any(Answer.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            Answer result = answerService.updateAnswer(updatedInput, adminId);

            // then
            assertEquals("수정된 내용", result.getContent());
            assertEquals(Answer.AnswerStatus.ANSWER_UPDATED, result.getAnswerStatus());

            verify(answerRepository).findById(answerId);
            verify(answerRepository).save(any(Answer.class));
        }
    }

    @Test
    void updateAnswer_shouldThrow_whenNotAdminAndNotOwner() {
        // given
        Long adminId = 1L;
        Long answerId = 10L;

        Member owner = new Member();
        owner.setMemberId(999L); // 다른 사람

        Answer existingAnswer = new Answer();
        existingAnswer.setAnswerId(answerId);
        existingAnswer.setMember(owner);
        existingAnswer.setContent("Old content");

        Answer updatedAnswer = new Answer();
        updatedAnswer.setAnswerId(answerId);
        updatedAnswer.setContent("New content");

        // ✅ ROLE_USER 권한만 있는 mock 인증 객체 구성
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockUser", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext mockContext = mock(SecurityContext.class);
        when(mockContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            // ❌ AuthorizationUtils mock은 제거하고 실제 메서드 호출

            given(answerRepository.findById(answerId)).willReturn(Optional.of(existingAnswer));

            // when & then
            BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                    () -> answerService.updateAnswer(updatedAnswer, adminId));

            assertEquals(ExceptionCode.UNAUTHORIZED_OPERATION, exception.getExceptionCode());
        }
    }
    @Test
    void deleteAnswer_success_byAdmin() {
        // given
        Long adminId = 1L;
        Long answerId = 10L;
        Long questionId = 20L;

        Member admin = new Member();
        admin.setMemberId(adminId);

        Question question = new Question();
        question.setQuestionId(questionId);

        Answer answer = new Answer();
        answer.setAnswerId(answerId);
        answer.setMember(admin);
        answer.setQuestion(question);
        answer.setAnswerStatus(Answer.AnswerStatus.ANSWER_REGISTERED);

        // ✅ ROLE_ADMIN 설정
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockAdmin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContext mockContext = mock(SecurityContext.class);
        when(mockContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            // stub
            given(answerRepository.findById(answerId)).willReturn(Optional.of(answer));
            given(answerRepository.save(any(Answer.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            answerService.deleteAnswer(answerId, adminId);

            // then
            assertEquals(Answer.AnswerStatus.ANSWER_DELETED, answer.getAnswerStatus());

            verify(answerRepository).findById(answerId);
            verify(questionService).setAnswerNull(questionId);
            verify(answerRepository).save(answer);
        }
    }

    @Test
    void deleteAnswer_shouldThrow_whenNotAdminAndNotOwner() {
        // given
        Long adminId = 1L;
        Long answerId = 10L;

        Member otherMember = new Member();
        otherMember.setMemberId(999L); // 관리자 아님, 다른 사람

        Question question = new Question();
        question.setQuestionId(20L);

        Answer answer = new Answer();
        answer.setAnswerId(answerId);
        answer.setMember(otherMember);
        answer.setQuestion(question);

        // ✅ ROLE_USER 설정
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "mockUser", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext mockContext = mock(SecurityContext.class);
        when(mockContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);

            // stub
            given(answerRepository.findById(answerId)).willReturn(Optional.of(answer));

            // when & then
            BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                    () -> answerService.deleteAnswer(answerId, adminId));

            assertEquals(ExceptionCode.UNAUTHORIZED_OPERATION, exception.getExceptionCode());
            verify(answerRepository).findById(answerId);
            verify(questionService, never()).setAnswerNull(anyLong());
            verify(answerRepository, never()).save(any());
        }
    }
}
