package com.springboot.question.dto;

import com.springboot.answer.dto.AnswerResponseDto;
import com.springboot.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QuestionResponseDto {
    private Long questionId;
    private String title;
    private String content;
    private Question.QuestionStatus questionStatus;
    private String questionImage;
    private Long memberId;
    private String writerEmail;
    private AnswerResponseDto answer;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Question.QuestionAnswerStatus questionAnswerStatus;
}
