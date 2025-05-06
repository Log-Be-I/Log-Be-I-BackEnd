package com.springboot.report.dto;

import com.springboot.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ReportResponseDto {
    private Long reportId;
    private String monthlyTitle;
    private String title;
    private Map<String, String> content;
    private Report.ReportType reportType;
    private int periodNumber;
    private LocalDateTime createdAt;
}
