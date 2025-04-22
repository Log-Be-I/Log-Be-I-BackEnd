package com.springboot.report.controller;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.auth.utils.CustomPrincipal;

import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;

import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.AuthorizationUtils;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper mapper;
    private final GoogleTextToSpeechService googleTextToSpeechService;




    // 구글 TTS (유저가 선택한 reportId 리스트를 받는다)
    @PostMapping("/audio")
    public ResponseEntity<List<String>> generateTts(@RequestBody List<Long> reportsId,
                                              @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        List<String> audioReports =  reportService.reportToClovaAudio(reportsId, customPrincipal.getMemberId());

     return new ResponseEntity<>(audioReports, HttpStatus.OK);
    }
//    @PostMapping("/tts")
//    public ResponseEntity<String> generateTts(@RequestBody String text) {
//        try {
//
//            googleTextToSpeechService.synthesizeText(text, "output01.mp3");
//            return ResponseEntity.ok("음성 생성 완료");
//        } catch (Exception e) {
//            log.error("Google TTS 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("음성 생성 중 오류가 발생했습니다.");
//        }
//    }

//     }



//    @GetMapping
//    public ResponseEntity getReportList(@Positive @RequestParam(value = "year", required = false) Integer year,  // year를 안보내도 기본값 처리 하도록 설정
//                                         @AuthenticationPrincipal CustomPrincipal customPrincipal){
//        //year 값이 설정되지 않았다면 올해 기준으로 정렬
//        int searchYear =(year != null) ? year : LocalDate.now().getYear();
//
//        List<Report> reports = reportService.findMonthlyReports(searchYear, customPrincipal.getMemberId());
//
//        return new ResponseEntity<>(new ListResponseDto<>(mapper.monthliesToMonthlyResponses(monthlyReports)), HttpStatus.OK);
//    }
//
//
//    @GetMapping("{monthly-id}")
//    public ResponseEntity getReport(@Positive @PathVariable("monthly-id") long monthlyId,
//                                     @AuthenticationPrincipal CustomPrincipal customPrincipal){
//
//        MonthlyReport monthlyReport = monthlyReportService.findMonthlyReport(monthlyId, customPrincipal.getMemberId());
//        return new ResponseEntity<>( new SingleResponseDto<>(mapper.monthlyToMonthlyResponse(monthlyReport)), HttpStatus.OK);
//    }
}
