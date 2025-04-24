package com.springboot.report.dto;

import com.springboot.record.entity.Record;
import com.springboot.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReportAnalysisRequest { // Entity와 바로 매핑 가능하지만, 포맷 유지 불확실 -> JSON 구조 신뢰성이 낮고, 파싱 실패 리스크가 있음
    //AI한테 보내는 데이터
    private String reportTitle;
    private String monthlyReportTitle;
    private Long memberId;
    private Report.ReportType reportType;
    private List<RecordForAnalysisDto> analysisDtoList;
}
