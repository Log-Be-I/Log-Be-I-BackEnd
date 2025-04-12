package com.springboot.question.dto;

import com.springboot.answer.dto.AnswerDto;
import com.springboot.question.entity.Question;
import com.springboot.validator.NotSpace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class QuestionDto {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class Post{
        @NotBlank(message = "제목은 필수 입력란입니다.")
        private String title;

        @NotBlank(message = "내용은 필수 입력란입니다.")
        private String content;

        private String image;

        private Long memberId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class Patch{
        private long questionId;

        @NotSpace
        private String title;

        @NotSpace
        private String content;

        private String image;

        private long memberId;

        private Question.QuestionStatus questionStatus;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long questionId;
        private String title;
        private String content;
        private Question.QuestionStatus questionStatus;
        private String questionImage;
        private Long memberId;
        private AnswerDto.Response answer;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
