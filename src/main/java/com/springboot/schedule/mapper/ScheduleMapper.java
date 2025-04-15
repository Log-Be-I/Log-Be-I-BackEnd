package com.springboot.schedule.mapper;

import com.springboot.member.entity.Member;
import com.springboot.schedule.dto.SchedulePatchDto;
import com.springboot.schedule.dto.SchedulePostDto;
import com.springboot.schedule.dto.ScheduleResponseDto;
import com.springboot.schedule.entity.Schedule;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ScheduleMapper{
    Schedule schedulePostDtoToSchedule (SchedulePostDto schedulePostDto);
    Schedule schedulePatchDtoToSchedule (SchedulePatchDto schedulePatchDto);
    ScheduleResponseDto scheduleToscheduleResponseDto (Schedule schedule);
    default List<ScheduleResponseDto> schedulesToScheduleResponseDtos (List<Schedule> schedules) {
        List<ScheduleResponseDto> scheduleResponseDtos = schedules.stream()
                .map(schedule -> scheduleToscheduleResponseDto(schedule))
                .collect(Collectors.toList());

        return scheduleResponseDtos;
    }
}
