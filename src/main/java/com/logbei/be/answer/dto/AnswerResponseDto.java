package com.springboot.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AnswerResponseDto {
    @Schema(description = "답변 번호", example = "4")
    private Long answerId;

    @Schema(description = "답변 내용", example = "안녕하세요 고객님^^")
    private String content;

    @Schema(description = "문의 글 번호", example = "1")
    private Long questionId;

    @Schema(description = "작성자 번호", example = "4")
    private Long memberId;

    @Schema(description = "시작 날짜", example = "2025-04-12T13:30")
    private LocalDateTime createdAt;

    @Schema(description = "종료 날짜", example = "2025-04-12T13:30")
    private LocalDateTime modifiedAt;
}
