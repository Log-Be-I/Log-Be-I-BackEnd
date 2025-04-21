package com.springboot.answer.dto;

import com.springboot.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class AnswerDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Post{
        @Schema(description = "답변 내용", example = "안녕하세요 고객님^^")
        @NotBlank(message = "답변 내용은 필수입니다.")
        private String content;

        @Schema(description = "문의 글 번호", example = "3")
        private Long questionId;

        @Schema(description = "작성자 번호", example = "23")
        private Long memberId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Patch{
        private Long answerId;
        @Schema(description = "답변 내용", example = "되게 해드릴게요!!")
        @NotSpace(message = "답변 내용은 필수입니다.")
        private String content;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response{
        private Long answerId;
        private String content;
        private Long questionId;
        private Long memberId;
    }
}
