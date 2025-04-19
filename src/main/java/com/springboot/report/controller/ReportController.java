package com.springboot.report.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.SingleResponseDto;
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
@RequestMapping("/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {
    private final static String REPORT_DEFAULT_URL = "/reports";
    private final ReportService reportService;
    private final ReportMapper mapper;

    @PostMapping
    public ResponseEntity postReport(@RequestBody ReportDto.Post post,
                                     @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        Report report = reportService.createReport(mapper.reportPostToReport(post), customPrincipal.getMemberId());
        URI location = UriCreator.createUri(REPORT_DEFAULT_URL);
        return ResponseEntity.created(location).body(new SingleResponseDto<>(mapper.reportToResponseDto(report)));
    }
}
