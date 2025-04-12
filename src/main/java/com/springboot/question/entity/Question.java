package com.springboot.question.entity;

import com.springboot.answer.entity.Answer;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;


    @OneToOne(mappedBy = "question")
    private Answer answer;

    @Enumerated(value = EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.QUESTION_REGISTERED;

    @Column
    private String image;

    public enum QuestionStatus {
        QUESTION_REGISTERED("질문 등록"),
        QUESTION_ANSWERED("답변 완료"),
        QUESTION_DELETED("질문 삭제"),
        QUESTION_DEACTIVED("질문 비활성화");

        private final String message;

        QuestionStatus(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}
