package com.springboot.schedule.controller;


import com.springboot.auth.utils.MemberDetails;
import com.springboot.member.service.MemberService;
import com.springboot.schedule.dto.SchedulePatchDto;
import com.springboot.schedule.dto.SchedulePostDto;
import com.springboot.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

        private final ScheduleService scheduleService;

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

            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        // 일정 수정
        @PatchMapping("/schedules/{schedule-id}")
        //앱 푸쉬 알림 수신동의 여부 저장
        public ResponseEntity patchSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                            @RequestBody SchedulePatchDto schedulePatchDto,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {



            return new ResponseEntity<>(HttpStatus.OK);
        }

        // 일정 삭제
        @DeleteMapping("/schedules/{schedule-id}")
        //앱 푸쉬 알림 수신동의 여부 저장
        public ResponseEntity patchSchedule(@PathVariable("schedule-id") @Positive long scheduleId,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {

            scheduleService.deletedSchedule(scheduleId);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


}
