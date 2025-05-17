package com.springboot.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class AnswerPostDto {
    @Schema(description = "답변 내용", example = "안녕하세요 고객님^^")
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String content;

    @Schema(description = "문의 글 번호", example = "1")
    private Long questionId;

    @Schema(description = "작성자 번호", example = "4")
    private Long memberId;
}
