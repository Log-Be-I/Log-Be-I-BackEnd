package com.springboot.report.dto;


import com.springboot.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ReportResponseDto {

    private Long reportId;
    private String title;
    private String content;
    private Report.ReportType reportType;
    private int periodNumber;
}