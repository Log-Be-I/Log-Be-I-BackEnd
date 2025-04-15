package com.springboot.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleResponseDto {

    @NotBlank
    private String title;

    @NotBlank
    private String startDateTime;

    @NotBlank
    private String endDateTime;

}
