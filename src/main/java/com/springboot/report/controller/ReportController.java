package com.springboot.report.controller;

import com.springboot.auth.utils.CustomPrincipal;

import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.responsedto.SingleResponseDto;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper mapper;



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
