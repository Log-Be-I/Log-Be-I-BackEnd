package com.springboot.schedule.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.schedule.dto.*;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.schedule.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.http.ResponseEntity;

class ScheduleControllerTest {

    @InjectMocks
    private ScheduleController scheduleController;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private ScheduleMapper scheduleMapper;


    private CustomPrincipal mockPrincipal;

    @BeforeEach
    // CustomPrincipal 객체 자체를 Mockito 로 수동 생성
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockPrincipal = mock(CustomPrincipal.class);
        when(mockPrincipal.getMemberId()).thenReturn(1L);
    }

    @Test
    void 일정등록_성공() {
        SchedulePostDto postDto = new SchedulePostDto();
        Schedule schedule = new Schedule();
        ScheduleResponseDto responseDto = new ScheduleResponseDto();

        when(scheduleMapper.schedulePostDtoToSchedule(postDto)).thenReturn(schedule);
        when(scheduleService.createTextSchedule(schedule, 1L)).thenReturn(schedule);
        when(scheduleMapper.scheduleToscheduleResponseDto(schedule)).thenReturn(responseDto);

        ResponseEntity<?> response = scheduleController.postTextSchedule(postDto, mockPrincipal);

        assertEquals(201, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof SingleResponseDto);
    }

    @Test
    void 일정수정_성공() {
        SchedulePatchDto patchDto = new SchedulePatchDto();
        Schedule schedule = new Schedule();
        ScheduleResponseDto responseDto = new ScheduleResponseDto();

        when(scheduleMapper.schedulePatchDtoToSchedule(patchDto)).thenReturn(schedule);
        when(scheduleService.updateSchedule(schedule, 100L, 1L)).thenReturn(schedule);
        when(scheduleMapper.scheduleToscheduleResponseDto(schedule)).thenReturn(responseDto);

        ResponseEntity<?> response = scheduleController.patchSchedule(100L, patchDto, mockPrincipal);

        assertEquals(200, response.getStatusCodeValue());
        verify(scheduleService).updateSchedule(schedule, 100L, 1L);
    }

    @Test
    void 일정조회_성공() {
        Schedule schedule = new Schedule();
        ScheduleResponseDto responseDto = new ScheduleResponseDto();

        when(scheduleService.findSchedule(1L, 1L)).thenReturn(schedule);
        when(scheduleMapper.scheduleToscheduleResponseDto(schedule)).thenReturn(responseDto);

        ResponseEntity<?> response = scheduleController.getSchedule(1L, mockPrincipal);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void 월별일정조회_성공() throws GeneralSecurityException, IOException {
        List<Schedule> scheduleList = Collections.singletonList(new Schedule());
        List<ScheduleResponseDto> dtoList = Collections.singletonList(new ScheduleResponseDto());

        when(scheduleService.findSchedules(2025, 5, 1L)).thenReturn(scheduleList);
        when(scheduleMapper.schedulesToScheduleResponseDtos(scheduleList)).thenReturn(dtoList);

        ResponseEntity<?> response = scheduleController.getSchedules(2025, 5, mockPrincipal);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof ListResponseDto);
    }

    @Test
    void 오늘일정조회_성공() throws GeneralSecurityException, IOException {
        List<Schedule> scheduleList = Collections.singletonList(new Schedule());
        List<ScheduleResponseDto> dtoList = Collections.singletonList(new ScheduleResponseDto());

        when(scheduleService.findSchedules(0, 0, 1L)).thenReturn(scheduleList);
        when(scheduleMapper.schedulesToScheduleResponseDtos(scheduleList)).thenReturn(dtoList);

        ResponseEntity<?> response = scheduleController.getTodaySchedule(mockPrincipal);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof ListResponseDto);
    }

    @Test
    void 일정삭제_성공() {
        doNothing().when(scheduleService).deletedSchedule(1L, 1L);

        ResponseEntity<?> response = scheduleController.deleteSchedule(1L, mockPrincipal);

        assertEquals(204, response.getStatusCodeValue());
        verify(scheduleService).deletedSchedule(1L, 1L);
    }
}