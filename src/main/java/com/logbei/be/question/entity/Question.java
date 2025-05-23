package com.logbei.be.question.entity;

import com.logbei.be.answer.entity.Answer;
import com.logbei.be.audit.BaseEntity;
import com.logbei.be.member.entity.Member;
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
public class Question extends BaseEntity {

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


    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    @Enumerated(value = EnumType.STRING)
    private QuestionAnswerStatus questionAnswerStatus = QuestionAnswerStatus.NONE_ANSWER;

    @Column
    private String image;

    @Enumerated(value = EnumType.STRING)
    private QuestionStatus questionStatus = QuestionStatus.QUESTION_REGISTERED;

    public void deactivate(){
        this.questionStatus = QuestionStatus.QUESTION_DEACTIVED;
    }

    public enum QuestionStatus {
        QUESTION_REGISTERED("질문 등록"),
        QUESTION_DELETED("질문 삭제"),
        QUESTION_DEACTIVED("질문 비활성화");

        @Getter
        private String message;

        QuestionStatus(String message) {
            this.message = message;
        }
    }

    public enum QuestionAnswerStatus{
        NONE_ANSWER("답변 없음"),
        DONE_ANSWER("답변 있음");

        @Getter
        private String status;

        QuestionAnswerStatus(String status) {
            this.status = status;
        }
    }
    // Member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getQuestions().contains(this)) {
            member.setQuestion(this);
        }
    }

    // answer 영속성
    public void setAnswer(Answer answer){
        this.answer = answer;
        if(answer != null){
            answer.setQuestion(this);
            this.questionAnswerStatus = QuestionAnswerStatus.DONE_ANSWER;
        }
    }

}
