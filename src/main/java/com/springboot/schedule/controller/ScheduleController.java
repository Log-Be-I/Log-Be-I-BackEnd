package com.springboot.schedule.controller;


import com.springboot.auth.utils.MemberDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.schedule.dto.SchedulePatchDto;
import com.springboot.schedule.dto.SchedulePostDto;
import com.springboot.schedule.dto.ScheduleResponseDto;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    // 일정 등록 - 음성
    @PostMapping("/audio-schedules")
    public ResponseEntity postAudioSchedule(@Valid @RequestBody SchedulePostDto schedulePostDto,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 일정 등록 - text
    @PostMapping("/text-schedules")
    public ResponseEntity postTextSchedule(@Valid @RequestBody SchedulePostDto schedulePostDto,
                                           @AuthenticationPrincipal MemberDetails memberDetails) {

        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);


        Schedule schedule = scheduleMapper.schedulePostDtoToSchedule(schedulePostDto);
        scheduleService.postTextSchedule(schedule, memberDetails);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 일정 수정
    @PatchMapping("/schedules/{schedule-id}")
    public ResponseEntity patchSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                        @RequestBody SchedulePatchDto schedulePatchDto,
                                        @AuthenticationPrincipal MemberDetails memberDetails) {

        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);
        // dto -> entity
        Schedule schedule = scheduleMapper.schedulePatchDtoToSchedule(schedulePatchDto);
        // 일정 수정 서비스 요청
        scheduleService.updateSchedule(scheduleId, memberDetails, schedule);


        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 일정 단일 조회
    @GetMapping("/schedules/{schedule-id}")
    public ResponseEntity getSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                      @AuthenticationPrincipal MemberDetails memberDetails){
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        Schedule schedule = scheduleService.findSchedule(scheduleId);


        return new ResponseEntity<>(scheduleMapper.scheduleToscheduleResponseDto(schedule), HttpStatus.OK);
    }

    // 일정 전체 조회
    @GetMapping("/schedules")
    public ResponseEntity getSchedules(@Positive @RequestParam(value = "year") int year,
                                     @Positive @RequestParam(value = "month") int month,
                                       @AuthenticationPrincipal MemberDetails memberDetails) {
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);
        String target = String.format("%d-%02d", year, month);

        // "year" 과 "month" 가 포함된 모든 일정 조회
        List<Schedule> scheduleList = scheduleRepository.findAll().stream()
                .filter(schedule -> schedule.getStartDateTime().startsWith(target))
                .collect(Collectors.toList());

        List<ScheduleResponseDto> scheduleResponseDtos = scheduleMapper.schedulesToScheduleResponseDtos(scheduleList);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }

    // 일정 삭제
    @DeleteMapping("/schedules/{schedule-id}")
    public ResponseEntity deleteSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                        @AuthenticationPrincipal MemberDetails memberDetails) {
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        // 일정 상태 변경
        scheduleService.deletedSchedule(scheduleId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
