package com.springboot.report.controller;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.record.entity.Record;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.utils.ReportUtil;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.checkerframework.checker.index.qual.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
    //postman Test 진행
    private final OpenAiService openAiService;
    //test
    @PostMapping("/test")
    public ResponseEntity testGenerateReports(@RequestBody List<Record> requests) {

        List<ReportAnalysisRequest> weeklies = ReportUtil.createWeeklyReportRequests(requests);
        // GPT 분석 → Report 생성 -> DB 저장
        List<Report> reports = openAiService.createReportsFromAi(weeklies);

        return new ResponseEntity<>(new ListResponseDto<>(
                mapper.reportsToReportsResponseDtos(reports)), HttpStatus.CREATED);
    }



    // 구글 TTS (유저가 선택한 reportId 리스트를 받는다)
    @PostMapping("/audio")
    public ResponseEntity<List<String>> generateTts(@RequestBody List<Long> reportsId,
                                              @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        List<String> audioReports =  reportService.reportToClovaAudio(reportsId, customPrincipal.getMemberId());

     return new ResponseEntity<>(audioReports, HttpStatus.OK);
    }

    //연도별 그룹 조회
    @GetMapping
    public ResponseEntity getReportList(@Positive @RequestParam(value = "year", required = false) Integer year,  // year를 안보내도 기본값 처리 하도록 설정
                                        @AuthenticationPrincipal CustomPrincipal customPrincipal){
        //year 값이 설정되지 않았다면 올해 기준으로 정렬
        int searchYear =(year != null) ? year : LocalDate.now().getYear();

        List<Report> reports = reportService.findMonthlyReports(customPrincipal.getMemberId(), searchYear);

        return new ResponseEntity<>(new ListResponseDto<>(mapper.reportTosummaryResponse(reports)), HttpStatus.OK);
    }

    //Report - MonthlyTitle 상세 그룹 조회
    @GetMapping("/detail")
    public ResponseEntity getReports(@RequestParam("monthly-title") String monthlyTitle,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {

        List<Report> reports = reportService.findMonthlyTitleWithReports(monthlyTitle, customPrincipal.getMemberId());

        return new ResponseEntity<>(new ListResponseDto<>(mapper.reportsToReportsResponseDtos(reports)), HttpStatus.OK);
    }

}
