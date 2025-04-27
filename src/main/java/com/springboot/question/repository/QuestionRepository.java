package com.springboot.question.repository;


import com.springboot.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {
//    @Query("SELECT q FROM Question q WHERE q.questionStatus = 'QUESTION_REGISTERED'")
    Page<Question> findAllByQuestionStatus(Question.QuestionStatus status, Pageable pageable);
    Page<Question> findAllByMember_MemberId(Long memberId, Pageable pageable);
    //Question, QuestionAnswer 상태에 맞는 질문 글만 조회
    Page<Question> findAllByQuestionStatusAndQuestionAnswerStatus(Question.QuestionStatus status, Question.QuestionAnswerStatus answerStatus, Pageable pageable);
}
