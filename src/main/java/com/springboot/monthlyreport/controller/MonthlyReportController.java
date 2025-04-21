package com.springboot.monthlyreport.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.monthlyreport.mapper.MonthlyReportMapper;
import com.springboot.monthlyreport.service.MonthlyReportService;
import com.springboot.responsedto.ListResponseDto;
import com.springboot.responsedto.SingleResponseDto;
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
@RequestMapping("/monthly-reports")
@RequiredArgsConstructor
@Validated
public class MonthlyReportController {
    private final MonthlyReportService monthlyReportService;
    private final MonthlyReportMapper mapper;

    @GetMapping("{monthly-id}")
    public ResponseEntity getMonthly(@Positive @PathVariable("monthly-id") long monthlyId,
                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){

        MonthlyReport monthlyReport = monthlyReportService.findMonthlyReport(monthlyId, customPrincipal.getMemberId());
        return new ResponseEntity<>( new SingleResponseDto<>(mapper.monthlyToMonthlyResponse(monthlyReport)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getMonthlyList(@Positive @RequestParam(value = "year", required = false) Integer year,  // year를 안보내도 기본값 처리 하도록 설정
                                         @AuthenticationPrincipal CustomPrincipal customPrincipal){
        //year 값이 설정되지 않았다면 올해 기준으로 정렬
        int searchYear =(year != null) ? year : LocalDate.now().getYear();

        List<MonthlyReport> monthlyReports = monthlyReportService.findMonthlyReports(searchYear, customPrincipal.getMemberId());

        return new ResponseEntity<>(new ListResponseDto<>(mapper.monthliesToMonthlyResponses(monthlyReports)), HttpStatus.OK);
    }
}
