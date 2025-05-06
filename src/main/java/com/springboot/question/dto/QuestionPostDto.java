package com.springboot.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class QuestionPostDto {
    @Schema(description = "문의 글 제목", example = "로그인 문의드립니다.")
    @NotBlank(message = "제목은 필수 입력란입니다.")
    private String title;

    @Schema(description = "문의 글 내용", example = "자동로그인 할 수 있게 해주세요.")
    @NotBlank(message = "내용은 필수 입력란입니다.")
    private String content;

    @Schema(description = "문의 글 첨부파일", example = "uri")
    private String image;

    @Schema(description = "문의 글 작성자", example = "23")
    private Long memberId;
}
