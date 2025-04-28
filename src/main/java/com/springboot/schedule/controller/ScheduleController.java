package com.springboot.schedule.controller;


import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.schedule.dto.*;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;
    // 일정 등록 - text
    @PostMapping("/text-schedules")
    public ResponseEntity postTextSchedule(@Valid @RequestBody SchedulePostDto schedulePostDto,
                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

    // 가입된 회원인지 검증
    Member member = memberService.validateExistingMember(customPrincipal.getMemberId());
    // 정상적인 상태인지 검증
    memberService.validateMemberStatus(member);

    Schedule schedule = scheduleMapper.schedulePostDtoToSchedule(schedulePostDto);
    schedule.setMember(member);
    // 1. DB에 먼저 저장
    scheduleService.postTextSchedule(schedule, customPrincipal);

//
        scheduleRepository.save(schedule);

        return new ResponseEntity<>(scheduleMapper.scheduleToscheduleResponseDto(schedule), HttpStatus.CREATED);
    }

    // 일정 수정
    @PatchMapping("/schedules/{schedule-id}")
    public ResponseEntity patchSchedule(@Parameter(description = "수정할 일정의 ID", example = "1")
                                            @PathVariable("schedule-id") @Positive long scheduleId,
                                        @RequestBody SchedulePatchDto schedulePatchDto,
                                        @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws GeneralSecurityException, IOException {

        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);
        // dto -> entity
        Schedule schedule = scheduleMapper.schedulePatchDtoToSchedule(schedulePatchDto);
        // schedule 검증
        scheduleService.validateExistingSchedule(scheduleId);
        // 일정 수정 서비스 요청
        scheduleService.updateServerSchedule(scheduleId, customPrincipal, schedule);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 일정 단일 조회
    @GetMapping("/schedules/{schedule-id}")
    public ResponseEntity getSchedule(@Parameter(description = "조회할 일정의 ID", example = "1")
                                          @PathVariable("schedule-id") @Positive long scheduleId,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        Schedule schedule = scheduleService.findSchedule(scheduleId);

        return new ResponseEntity<>(scheduleMapper.scheduleToscheduleResponseDto(schedule), HttpStatus.OK);
    }

    // 일정 전체 조회 (월 기준 조회)
    @GetMapping("/schedules")
    public ResponseEntity getSchedules(@Parameter(description = "조회할 연도", example = "2025")
                                       @Positive @RequestParam(value = "year") int year,
                                       @Parameter(description = "조회할 월 (1~12)", example = "4")
                                       @Positive @RequestParam(value = "month") int month,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws GeneralSecurityException, IOException {

        List<Schedule> syncedSchedules = scheduleService.findSchedules(year, month, customPrincipal);
        List<ScheduleResponseDto> scheduleResponseDtoList = scheduleMapper.schedulesToScheduleResponseDtos(syncedSchedules);
        return new ResponseEntity<>(scheduleResponseDtoList, HttpStatus.OK);
    }
    // 일정 하루 일정 조회
    @GetMapping("/main")
    public ResponseEntity getSchedule(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws GeneralSecurityException, IOException {

        int year = 0;
        int month = 0;

        List<Schedule> syncedSchedules = scheduleService.findSchedules(year, month, customPrincipal);
        List<ScheduleResponseDto> scheduleResponseDtoList = scheduleMapper.schedulesToScheduleResponseDtos(syncedSchedules);
        return new ResponseEntity<>(scheduleResponseDtoList, HttpStatus.OK);
    }

    // 일정 삭제
    @DeleteMapping("/schedules/{schedule-id}")
    public ResponseEntity deleteSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        // 일정 찾기
        Schedule schedule = scheduleService.validateExistingSchedule(scheduleId);

        // 일정 상태 변경
        scheduleService.deletedSchedule(schedule.getScheduleId());


        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
