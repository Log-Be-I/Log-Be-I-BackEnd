package com.springboot.schedule.mapper;

import com.springboot.schedule.dto.SchedulePatchDto;
import com.springboot.schedule.dto.SchedulePostDto;
import com.springboot.schedule.dto.ScheduleResponseDto;
import com.springboot.schedule.entity.Schedule;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleMapperTest {

    private final ScheduleMapper scheduleMapper = Mappers.getMapper(ScheduleMapper.class);

    // SchedulePostDto -> Schedule 매핑이 정확히 되는지 테스트
    @Test
    void schedulePostDtoToSchedule_shouldMapCorrectly() {
        SchedulePostDto dto = new SchedulePostDto();
        dto.setTitle("Test Title");
        dto.setStartDateTime("2025-05-10T10:00:00");
        dto.setEndDateTime("2025-05-10T11:00:00");

        Schedule schedule = scheduleMapper.schedulePostDtoToSchedule(dto);

        assertThat(schedule.getTitle()).isEqualTo("Test Title");
        assertThat(schedule.getStartDateTime()).isEqualTo(LocalDateTime.of(2025, 5, 10, 10, 0));
        assertThat(schedule.getEndDateTime()).isEqualTo(LocalDateTime.of(2025, 5, 10, 11, 0));
    }

    // SchedulePatchDto -> Schedule 매핑이 정확히 되는지 테스트
    @Test
    void schedulePatchDtoToSchedule_shouldMapCorrectly() {
        SchedulePatchDto dto = new SchedulePatchDto();
        dto.setTitle("Patched Title");
        dto.setStartDateTime("2025-05-11T09:00:00");
        dto.setEndDateTime("2025-05-11T10:00:00");

        Schedule schedule = scheduleMapper.schedulePatchDtoToSchedule(dto);

        assertThat(schedule.getStartDateTime()).isEqualTo(LocalDateTime.of(2025, 5, 11, 9, 0));
        assertThat(schedule.getEndDateTime()).isEqualTo(LocalDateTime.of(2025, 5, 11, 10, 0));
    }

    // Schedule -> ScheduleResponseDto 매핑이 정확히 되는지 테스트
    @Test
    void scheduleToScheduleResponseDto_shouldMapCorrectly() {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(1L);
        schedule.setTitle("Response Test");
        schedule.setStartDateTime(LocalDateTime.of(2025, 5, 12, 14, 0));
        schedule.setEndDateTime(LocalDateTime.of(2025, 5, 12, 15, 0));
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setModifiedAt(LocalDateTime.now());

        ScheduleResponseDto responseDto = scheduleMapper.scheduleToscheduleResponseDto(schedule);

        assertThat(responseDto.getScheduleId()).isEqualTo(1L);
        assertThat(responseDto.getTitle()).isEqualTo("Response Test");
    }

    // Schedule 리스트 -> ScheduleResponseDto 리스트 매핑이 정확히 되는지 테스트
    @Test
    void schedulesToScheduleResponseDtos_shouldMapListCorrectly() {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(1L);
        schedule.setTitle("Test List");
        schedule.setStartDateTime(LocalDateTime.of(2025, 5, 13, 10, 0));
        schedule.setEndDateTime(LocalDateTime.of(2025, 5, 13, 11, 0));
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setModifiedAt(LocalDateTime.now());

        List<ScheduleResponseDto> list = scheduleMapper.schedulesToScheduleResponseDtos(List.of(schedule));

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTitle()).isEqualTo("Test List");
    }
}