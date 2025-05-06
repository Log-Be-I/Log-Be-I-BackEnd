package com.springboot.record.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecordPatchDto {
    private Long recordId;

    @Schema(description = "기록 등록 시간", example = "08시 30분")
//        @NotBlank
    private String recordDateTime;

    @Schema(description = "기록의 내용", example = "아침에 삼각김밥 먹고, 약 먹음")
    @NotBlank(message = "내용을 작성해주세요.")
    private String content;

    @Schema(description = "기록 작성 회원번호", example = "1")
    private Long memberId;

    @Schema(description = "기록 분류 번호", example = "1")
    private Long categoryId;
}
