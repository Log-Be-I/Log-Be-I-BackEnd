package com.springboot.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@Setter
@Schema(description = "일정 등록 요청 DTO")
public class SchedulePostDto {

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
