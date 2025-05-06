package com.springboot.answer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AnswerResponseDto {
    private Long answerId;
    private String content;
    private Long questionId;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
