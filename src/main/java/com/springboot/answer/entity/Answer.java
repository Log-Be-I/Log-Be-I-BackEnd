package com.springboot.answer.entity;

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

    @OneToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false, length = 800)
    private String content;

    @Enumerated(value = EnumType.STRING)
    private AnswerStatus answerStatus = AnswerStatus.DONE_ANSWER;

    public enum AnswerStatus {
        NOT_ANSWER,
        DONE_ANSWER
    }
}
