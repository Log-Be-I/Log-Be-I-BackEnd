package com.springboot.monthlyreport.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.monthlyreport.dto.MonthlyReportDto;
import com.springboot.monthlyreport.mapper.MonthlyReportMapper;
import com.springboot.monthlyreport.service.MonthlyReportService;
import com.springboot.report.entity.Report;
import com.springboot.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/monthly-reports")
@RequiredArgsConstructor
@Validated
public class MonthlyReportController {
    private final static String MONTHLY_DEFAULT_URL = "/monthly-reports";
    private final MonthlyReportService monthlyReportService;
    private final MonthlyReportMapper mapper;

//    @PostMapping
//    public ResponseEntity postMonthly(@RequestBody MonthlyReportDto.Post post,
//                                      @AuthenticationPrincipal CustomPrincipal customPrincipal){
//
//        post.setMemberId(customPrincipal.getMemberId());
//        //post에서 year, month 추출
//        Report report = mapper.re
//        URI location = UriCreator.createUri(MONTHLY_DEFAULT_URL, monthly);
//        return ResponseEntity.created(location).build();
//    }
}
