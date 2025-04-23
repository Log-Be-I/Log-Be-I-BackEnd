package com.springboot.report.dto;


import com.springboot.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;


public class ReportDto {

    @Getter
    @AllArgsConstructor
    public static class summaryResponse{
        private Long reportId;
        private String monthlyTitle;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {

        private Long reportId;
        private String monthlyTitle;
        private String title;
        private Map<String, String> content;
        private Report.ReportType reportType;
        private int periodNumber;
    }
}