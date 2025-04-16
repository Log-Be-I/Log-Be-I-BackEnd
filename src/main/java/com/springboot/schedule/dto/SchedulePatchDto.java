package com.springboot.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class SchedulePatchDto {
    @Schema(description = "제목", example = "점심점심")
    @NotBlank
    private String title;

    @Schema(description = "시작 날짜", example = "2025-04-12T13:30")
    @NotBlank
    private String startDateTime;

    @Schema(description = "종료 날짜", example = "2025-04-12T14:30")
    @NotBlank
    private String endDateTime;
}
