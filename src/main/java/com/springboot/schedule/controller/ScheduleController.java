package com.springboot.schedule.controller;


import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.keyword.dto.KeywordResponseDto;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.responsedto.MultiResponseDto;
import com.springboot.schedule.dto.*;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.GoogleEventMapper;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.schedule.service.GoogleCalendarService;
import com.springboot.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final GoogleCalendarService googleCalendarService;
    private final GoogleEventMapper googleEventMapper;

    // 일정 등록 - 음성
    @PostMapping("/audio-schedules")
    public ResponseEntity postAudioSchedule(@Valid @RequestBody SchedulePostDto schedulePostDto,
                                            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {


        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //swagger API - 등록
    @Operation(summary = "일정 수동 등록", description = "일정을 수동 등록합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 일정 등록"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })
    // 일정 등록 - text
    @PostMapping("/text-schedules")
    public ResponseEntity postTextSchedule(@Valid @RequestBody SchedulePostDto schedulePostDto,
                                           @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);


        Schedule schedule = scheduleMapper.schedulePostDtoToSchedule(schedulePostDto);
        scheduleService.postTextSchedule(schedule, memberDetails);

        // 구글 캘린더
        try {
            // google calendar 등록시 필수 정보
            GoogleEventDto googleEventDto = new GoogleEventDto();
            googleEventDto.setStartDateTime(schedule.getStartDateTime());
            googleEventDto.setEndDateTime(schedule.getEndDateTime());
            googleEventDto.setSummary(schedule.getTitle());
            googleEventDto.setCalendarId(memberDetails.getEmail());

            googleCalendarService.sendEventToGoogleCalendar(googleEventDto);
            return ResponseEntity.ok(googleEventDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Google Calendar API 호출 실패: " + e.getMessage());
        }

    }

    //swagger API - 수정
    @Operation(summary = "일정 수정", description = "일정을 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새로운 일정 수정"),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })
    // 일정 수정
    @PatchMapping("/schedules/{schedule-id}")
    public ResponseEntity patchSchedule(@Parameter(description = "수정할 일정의 ID", example = "1")
                                            @PathVariable("schedule-id") @Positive long scheduleId,
                                        @RequestBody SchedulePatchDto schedulePatchDto,
                                        @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {

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

    //swagger API - 조회
    @Operation(summary = "일정 조회", description = "일정을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "특정 id 일정 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })
    // 일정 단일 조회
    @GetMapping("/schedules/{schedule-id}")
    public ResponseEntity getSchedule(@Parameter(description = "조회할 일정의 ID", example = "1")
                                          @PathVariable("schedule-id") @Positive long scheduleId,
                                      @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails){
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        Schedule schedule = scheduleService.findSchedule(scheduleId);


        return new ResponseEntity<>(scheduleMapper.scheduleToscheduleResponseDto(schedule), HttpStatus.OK);
    }

    //swagger API - 조회
    @Operation(summary = "일정 전체 조회", description = "특정 월의 일정 전체를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "날짜 범위에 맞는 전체 일정 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })
    // 일정 전체 조회
    @GetMapping("/schedules")
    public ResponseEntity getSchedules(@Parameter(description = "조회할 연도", example = "2025")
                                           @Positive @RequestParam(value = "year") int year,
                                       @Parameter(description = "조회할 월 (1~12)", example = "4")
                                       @Positive @RequestParam(value = "month") int month,
                                       @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {
        // 가입된 회원인지 검증
        List<Schedule> scheduleList = scheduleService.findSchedules(year, month, memberDetails);

        // 시간 객체 생성
        String timeMin = googleCalendarService.getStartOfMonth(year, month);
        String timeMax = googleCalendarService.getEndOfMonth(year, month);

        // 구글 캘린더 조회 요청
        List<Event> eventList = googleCalendarService.getEventsFromGoogleCalendar(timeMin, timeMax);

        // 구글 일정 조회 리스트
        List<GoogleEventDto> googleEventDtoList = googleEventMapper.eventListToGoogleEventDtoList(eventList);
        // 서버 db 일정 조회 리스트
        List<ScheduleResponseDto> scheduleResponseDtos = scheduleMapper.schedulesToScheduleResponseDtos(scheduleList);

        return new ResponseEntity<>(scheduleResponseDtos, HttpStatus.OK);
    }

    //swagger API - 삭제
    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "일정이 삭제되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효한 인증 자격 증명이 없습니다",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"Your session has expired. Please log in again to continue.\"}")))
    })

    // 일정 삭제
    @DeleteMapping("/schedules/{schedule-id}")
    public ResponseEntity deleteSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal MemberDetails memberDetails) {
        // 가입된 회원인지 검증
        Member member = memberService.validateExistingMember(memberDetails.getMemberId());
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        // 일정 상태 변경
        scheduleService.deletedSchedule(scheduleId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
