package com.logbei.be.answer.entity;


import com.logbei.be.audit.BaseEntity;
import com.logbei.be.member.entity.Member;
import com.logbei.be.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(nullable = false, length = 800)
    private String content;

    @Enumerated(value = EnumType.STRING)
    private AnswerStatus answerStatus = AnswerStatus.ANSWER_REGISTERED;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public enum AnswerStatus {
        ANSWER_REGISTERED("답변 등록"),
        ANSWER_UPDATED("답변 수정"),
        ANSWER_DELETED("답변 삭제");

        @Getter
        private String status;

        AnswerStatus (String status) {
            this.status = status;
        }
    }

    // question 영속성
    public void setQuestion(Question question){
        this.question = question;
        if(question != null && question.getAnswer() != this) {
            question.setAnswer(this);
        }
    }
}
