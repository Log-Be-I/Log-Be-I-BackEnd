package com.springboot.schedule.mapper;

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
    default ScheduleResponseDto scheduleToscheduleResponseDto (Schedule schedule) {
        ScheduleResponseDto responseDto = new ScheduleResponseDto();
        responseDto.setScheduleId(schedule.getScheduleId());
        responseDto.setTitle(schedule.getTitle());
        responseDto.setStartDateTime(schedule.getStartDateTime());
        responseDto.setEndDateTime(schedule.getEndDateTime());
        responseDto.setCreatedAt(schedule.getCreatedAt());
        responseDto.setModifiedAt(schedule.getModifiedAt());
        return responseDto;
    }
    default List<ScheduleResponseDto> schedulesToScheduleResponseDtos (List<Schedule> schedules) {
        List<ScheduleResponseDto> scheduleResponseDtos = schedules.stream()
                .map(schedule -> scheduleToscheduleResponseDto(schedule))
                .collect(Collectors.toList());

        return scheduleResponseDtos;
    }

}
