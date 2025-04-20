package com.springboot.schedule.mapper;

import com.springboot.googleCalendar.dto.GoogleEventDto;
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

    default ScheduleResponseDto googleEventDtoToScheduleResponseDto (GoogleEventDto googleEventDto) {
        ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto();
        scheduleResponseDto.setTitle(googleEventDto.getSummary());
        scheduleResponseDto.setStartDateTime(googleEventDto.getStartDateTime());
        scheduleResponseDto.setEndDateTime(googleEventDto.getEndDateTime());
        scheduleResponseDto.setCalendarId(googleEventDto.getCalendarId());

        return scheduleResponseDto;
    }

    default List<ScheduleResponseDto> googleEventDtoListToScheduleResponseDtoList (List<GoogleEventDto> googleEventDtoList) {
        return googleEventDtoList.stream().map(googleEventDto ->
                googleEventDtoToScheduleResponseDto(googleEventDto))
                .collect(Collectors.toList());
    }

    default GoogleEventDto scheduleToGoogleEventDto(Schedule schedule) {
        return GoogleEventDto.builder()
                .summary(schedule.getTitle())
                .startDateTime(schedule.getStartDateTime())
                .endDateTime(schedule.getEndDateTime())
                .calendarId("primary") // 고정
                .build();
    }

}
