package com.springboot.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@Setter
public class SchedulePostDto {

    @NotBlank
    private String title;

    @NotBlank
    private String startDateTime;

    @NotBlank
    private String endDateTime;

}
