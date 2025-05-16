package com.springboot.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordPostDto {
    @Schema(description = "기록 등록 시간", example = "09시 30분")
    @NotBlank
    private String recordDateTime;

    @Schema(description = "기록의 내용", example = "아침에 삼각김밥 먹고, 약 먹음")
    @NotBlank(message = "내용을 작성해주세요.")
    private String content;

    @Schema(description = "등록 회원", example = "50")
    private long memberId;

    @Schema(description = "기록 분류", example = "1")
    private long categoryId;

}
