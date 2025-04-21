package com.springboot.question.repository;


import com.springboot.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.questionStatus != 'QUESTION_DEACTIVATED'")
    Page<Question> findAllQuestionsWithoutDeactivated(Pageable pageable);
    Page<Question> findAllByMember_MemberId(Long memberId, Pageable pageable);
}
