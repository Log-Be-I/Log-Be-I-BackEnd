package com.springboot.report.dto;

import com.springboot.record.entity.Record;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReportAnalysisResponse {
    private Long memberId;
    private String monthlyReportTitle;
    private String reportTitle;
    private String content;
}
