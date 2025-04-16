package com.springboot.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleBundleResponseDto {
    private List<ScheduleResponseDto> scheduleList;
    private List<GoogleEventDto> googleEventList;
}
