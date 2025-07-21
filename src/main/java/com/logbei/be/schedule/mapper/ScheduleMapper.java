package com.logbei.be.schedule.mapper;

import com.logbei.be.schedule.dto.SchedulePatchDto;
import com.logbei.be.schedule.dto.SchedulePostDto;
import com.logbei.be.schedule.dto.ScheduleResponseDto;
import com.logbei.be.schedule.entity.Schedule;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ScheduleMapper{
    default Schedule schedulePostDtoToSchedule (SchedulePostDto schedulePostDto){
        Schedule schedule = new Schedule();
        schedule.setTitle(schedulePostDto.getTitle());
        schedule.setStartDateTime(postStringToLocalDateTime(schedulePostDto.getStartDateTime()));
        schedule.setEndDateTime(postStringToLocalDateTime(schedulePostDto.getEndDateTime()));

        return schedule;
    }

    default Schedule schedulePatchDtoToSchedule (SchedulePatchDto schedulePatchDto){
        Schedule schedule = new Schedule();
        schedule.setTitle(schedule.getTitle());
        schedule.setStartDateTime(postStringToLocalDateTime(schedulePatchDto.getStartDateTime()));
        schedule.setEndDateTime(postStringToLocalDateTime(schedulePatchDto.getEndDateTime()));

        return schedule;
    }

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

    //PatchDto의 recordDateTime (String -> LocalDateTime) 타입 변환 메서드
    default LocalDateTime postStringToLocalDateTime(String dateTimeStr){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);

    }
}
