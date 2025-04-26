package com.springboot.report.controller;

import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.member.service.MemberService;
import com.springboot.record.entity.Record;
import com.springboot.record.service.RecordService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper mapper;
    private final MemberService memberService;
    //postman Test 진행
    private final OpenAiService openAiService;
    private final RecordService recordService;
    //test
    @PostMapping("/test")
    public ResponseEntity testGenerateReports() {

        LocalDateTime today = LocalDateTime.now();
        //전 주 월요일(4/7) 00:00:00
        LocalDateTime weekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        //전 주 일요일(4/13) 23:59:59
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);

//        Member findMember = memberService.validateExistingMember(customPrincipal.getMemberId());
        List<Record> weeklyRecords = recordService.getWeeklyRecords(weekStart, weekEnd);

        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecords, Report.ReportType.REPORT_WEEKLY);
        // GPT 분석 → Report 생성 -> DB 저장
//        List<Report> reports = openAiService.createReportsFromAi(weeklies);
        List<Report> reports = openAiService.createReportsFromAiInBatch(weeklies);

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
