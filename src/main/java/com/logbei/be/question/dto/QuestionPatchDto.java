package com.logbei.be.question.dto;

import com.logbei.be.question.entity.Question;
import com.logbei.be.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class QuestionPatchDto {
    private long questionId;

    @Schema(description = "문의 글 제목", example = "로그인 문의드립니다.")
    @NotSpace
    private String title;

    @Schema(description = "문의 글 내용", example = "자동로그인 할 수 있게 해주세요^^")
    @NotSpace
    private String content;

    @Schema(description = "문의 글 작성자", example = "23")
    private long memberId;

    @Schema(description = "문의 글 상태", example = "QUESTION_REGISTERED")
    private Question.QuestionStatus questionStatus;
}
