package com.springboot.report.dto;

import com.springboot.record.entity.Record;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReportAnalysisRequest {
    //AI한테 보내는 데이터
    private String reportTitle;
    private String monthlyReportTitle;
    private Long memberId;
    private List<Record> records;

}
