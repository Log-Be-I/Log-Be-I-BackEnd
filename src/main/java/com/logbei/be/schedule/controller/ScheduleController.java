package com.logbei.be.schedule.controller;


<<<<<<< HEAD:src/main/java/com/springboot/schedule/controller/ScheduleController.java
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.swagger.SwaggerErrorResponse;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.schedule.dto.*;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
=======
import com.logbei.be.auth.utils.CustomPrincipal;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.schedule.dto.SchedulePatchDto;
import com.logbei.be.schedule.dto.SchedulePostDto;
import com.logbei.be.schedule.dto.ScheduleResponseDto;
import com.springboot.schedule.dto.*;
import com.logbei.be.schedule.entity.Schedule;
import com.logbei.be.schedule.mapper.ScheduleMapper;
import com.logbei.be.schedule.repository.ScheduleRepository;
import com.logbei.be.schedule.service.ScheduleService;
>>>>>>> 3cfffea (패키지명 변경):src/main/java/com/logbei/be/schedule/controller/ScheduleController.java
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "일정 API", description = "일정 등록, 조회, 수정, 삭제 관련 API")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;

    // 일정 등록 - text
    @Operation(summary = "일정 등록 (텍스트 기반)", description = "텍스트 기반 일정 정보를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "일정 등록 성공",
            content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
            examples = @ExampleObject(value = "{\"error\": \"Unauthorized\", \"message\": \"접근 권한이 없습니다.\"}")))
    @PostMapping("/text-schedules")
    public ResponseEntity postTextSchedule(@Valid @RequestBody SchedulePostDto schedulePostDto,
                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        Schedule schedule = scheduleService.createTextSchedule(scheduleMapper.schedulePostDtoToSchedule(schedulePostDto), customPrincipal.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(scheduleMapper.scheduleToscheduleResponseDto(schedule)), HttpStatus.CREATED);
    }

    // 일정 수정
    @Operation(summary = "일정 수정", description = "기존 등록된 일정을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 수정 성공",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "일정 수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))
    })
    @PatchMapping("/schedules/{schedule-id}")
    public ResponseEntity patchSchedule(@Parameter(description = "수정할 일정의 ID", example = "1")
                                            @PathVariable("schedule-id") @Positive long scheduleId,
                                        @Valid @RequestBody SchedulePatchDto schedulePatchDto,
                                        @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // 일정 수정 서비스 요청
        Schedule schedule = scheduleService.updateSchedule(scheduleMapper.schedulePatchDtoToSchedule(schedulePatchDto), scheduleId, customPrincipal.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(scheduleMapper.scheduleToscheduleResponseDto(schedule)), HttpStatus.OK);
    }

    // 일정 단일 조회
    @Operation(summary = "일정 단건 조회", description = "ID로 특정 일정을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기록 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"Not Found\"}")))
    })
    @GetMapping("/schedules/{schedule-id}")
    public ResponseEntity getSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){

        Schedule schedule = scheduleService.findSchedule(scheduleId, customPrincipal.getMemberId());

        return new ResponseEntity<>(new SingleResponseDto<>(scheduleMapper.scheduleToscheduleResponseDto(schedule)), HttpStatus.OK);
    }

    // 일정 전체 조회 (월 기준 조회)
    @Operation(summary = "월별 일정 전체 조회", description = "사용자의 특정 연도/월에 해당하는 모든 일정을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 전체 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "일정 월 조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))
    })
    @GetMapping("/schedules")
    public ResponseEntity getSchedules(@Parameter(description = "조회할 연도", example = "2025")
                                       @Positive @RequestParam(value = "year") int year,
                                       @Parameter(description = "조회할 월 (1~12)", example = "4")
                                       @Positive @RequestParam(value = "month") int month,
                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws GeneralSecurityException, IOException {

        List<Schedule> syncedSchedules = scheduleService.findSchedules(year, month, customPrincipal.getMemberId());

        return new ResponseEntity<>(new ListResponseDto<>(scheduleMapper.schedulesToScheduleResponseDtos(syncedSchedules)), HttpStatus.OK);
    }

    // 일정 하루 일정 조회
    @Operation(summary = "오늘 일정 조회", description = "현재 날짜 기준으로 오늘 하루의 일정을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "당일 일정 리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = ScheduleResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "일정 월 조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))
    })
    @GetMapping("/main")
    public ResponseEntity getTodaySchedule(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) throws GeneralSecurityException, IOException {

        int year = 0;
        int month = 0;

        List<Schedule> syncedSchedules = scheduleService.findSchedules(year, month, customPrincipal.getMemberId());
        return new ResponseEntity<>(new ListResponseDto<>(scheduleMapper.schedulesToScheduleResponseDtos(syncedSchedules)), HttpStatus.OK);
    }

    // 일정 삭제
    @Operation(summary = "일정 삭제", description = "특정 ID의 일정을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": \"NO_CONTENT\", \"Message\": \"DELETED_DONE\"}"))),
            @ApiResponse(responseCode = "401", description = "로그아웃 되었을 때",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기록 삭제 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Not Found\", \"Message\": \"Schedule Not Found\"}")))
    })
    @DeleteMapping("/schedules/{schedule-id}")
    public ResponseEntity deleteSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                         @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        // 일정 상태 변경
        scheduleService.deletedSchedule(scheduleId, customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
