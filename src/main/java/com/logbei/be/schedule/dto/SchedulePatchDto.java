package com.logbei.be.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "일정 수정 요청 DTO")
public class SchedulePatchDto {

    @NotBlank
    @Schema(description = "제목", example = "오늘 점심은 한식")
    private String title;

    @NotBlank
    @Schema(description = "시작 날짜", example = "2025-04-12T13:30")
    private String startDateTime;

    @NotBlank
    @Schema(description = "종료 날짜", example = "2025-04-12T14:30")
    private String endDateTime;
}
