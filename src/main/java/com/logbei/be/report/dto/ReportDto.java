package com.logbei.be.report.dto;


import com.logbei.be.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class ReportDto {

    @Getter
    @AllArgsConstructor
    public static class summaryResponse{
        private List<Long> reportIds;
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
        private LocalDateTime createdAt;
    }
}