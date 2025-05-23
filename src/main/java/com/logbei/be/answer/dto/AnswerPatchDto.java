package com.logbei.be.answer.dto;

import com.logbei.be.validator.NotSpace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerPatchDto {

    @Schema(description = "답변 번호", example = "4")
    private Long answerId;

    @Schema(description = "답변 내용", example = "되게 해드릴게요!!")
    @NotSpace(message = "답변 내용은 필수입니다.")
    private String content;
}
