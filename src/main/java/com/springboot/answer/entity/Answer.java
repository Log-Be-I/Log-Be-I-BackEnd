package com.springboot.answer.entity;

import com.springboot.member.entity.Member;
import com.springboot.question.entity.Question;
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
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(nullable = false, length = 800)
    private String content;

    @Enumerated(value = EnumType.STRING)
    private AnswerStatus answerStatus = AnswerStatus.DONE_ANSWER;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public enum AnswerStatus {
        NOT_ANSWER,
        DONE_ANSWER
    }

    // question 영속성
    public void setQuestion(Question question){
        this.question = question;
        if(question != null && question.getAnswer() != this) {
            question.setAnswer(this);
        }
    }
}
