package com.logbei.be.question.service;

import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.question.entity.Question;
import com.logbei.be.question.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import com.logbei.be.dashboard.dto.UnansweredQuestion;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private MemberService memberService;
    @InjectMocks
    private QuestionService questionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // createQuestion() 테스트
    @Test
    void createQuestion_shouldReturnSavedQuestion() {
        Question question = new Question();
        question.setMember(new Member());
        when(memberService.findVerifiedExistsMember(anyLong())).thenReturn(question.getMember());
        when(questionRepository.save(any())).thenReturn(question);

        Question result = questionService.createQuestion(question, 1L);

        assertThat(result).isNotNull();
        verify(questionRepository).save(question);
    }

    // findQuestions() 필터 조건 테스트
    @Test
    void findQuestions_shouldReturnFilteredQuestions() {
        Question q1 = new Question();
        Member member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@example.com");
        q1.setTitle("Java");
        q1.setMember(member);
        q1.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);

        List<Question> questions = List.of(q1);
        Page<Question> page = new PageImpl<>(questions);

        when(questionRepository.findAllByQuestionStatus(any(), any(Pageable.class))).thenReturn(page);

        Page<Question> result = questionService.findQuestions(1, 10, "NEWEST", false, "test@example.com", "Java");

        assertThat(result.getContent()).hasSize(1);
    }

    // findMyQuestions() 테스트
    @Test
    void findMyQuestions_shouldReturnQuestionsSortedByCreatedAt() {
        Page<Question> page = new PageImpl<>(List.of(new Question()));
        when(memberService.findVerifiedExistsMember(anyLong())).thenReturn(new Member());
        when(questionRepository.findAllByMember_MemberId(anyLong(), any(Pageable.class))).thenReturn(page);

        Page<Question> result = questionService.findMyQuestions(1, 10, 1L, "ASC");
        assertThat(result.getContent()).isNotEmpty();
    }

    // findQuestion() 테스트
    @Test
    void findQuestion_shouldReturnIfAuthorized() {
        Member member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@example.com");
        Question question = new Question();
        question.setMember(member);
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);

        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(question));

        Question result = questionService.findQuestion(1L, 1L);
        assertThat(result).isNotNull();
    }

    // deleteQuestion() 테스트
    @Test
    void deleteQuestion_shouldChangeStatus() {
        Question question = new Question();
        Member member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@example.com");

        question.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);
        question.setMember(member);

        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(question));

        questionService.deleteQuestion(1L, 1L);
        verify(questionRepository).save(question);
        assertThat(question.getQuestionStatus()).isEqualTo(Question.QuestionStatus.QUESTION_DELETED);
    }

    // isAnswered() 예외 테스트
    @Test
    void isAnswered_shouldThrowIfAnswered() {
        Question question = new Question();
        question.setQuestionAnswerStatus(Question.QuestionAnswerStatus.DONE_ANSWER);

        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(question));

        assertThrows(BusinessLogicException.class, () -> questionService.isAnswered(1L));
    }

    // verifyExistsQuestion() 예외 테스트
    @Test
    void verifyExistsQuestion_shouldThrowIfDeleted() {
        Question question = new Question();
        question.setQuestionStatus(Question.QuestionStatus.QUESTION_DELETED);

        assertThrows(BusinessLogicException.class, () -> questionService.verifyExistsQuestion(question));
    }

    // findUnansweredQuestions() 테스트
    @Test
    void findUnansweredQuestions_shouldReturnFilteredTitles() {
        Question q1 = new Question();
        q1.setTitle("Title");
        q1.setQuestionStatus(Question.QuestionStatus.QUESTION_REGISTERED);

        when(questionRepository.findAllByQuestionAnswerStatus(any())).thenReturn(List.of(q1));

        List<UnansweredQuestion> result = questionService.findUnansweredQuestions();
        assertThat(result).extracting("title").contains("Title");
    }
}