package com.springboot.report.dto;

import com.springboot.record.entity.Record;
import com.springboot.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Schema(description = "기록 분석 요청 DTO")
public class ReportAnalysisRequest { // Entity와 바로 매핑 가능하지만, 포맷 유지 불확실 -> JSON 구조 신뢰성이 낮고, 파싱 실패 리스크가 있음
    //AI한테 보내는 데이터
    @Schema(description = "레포트 제목", example = "2025년 02월 1주차")
    private String reportTitle;
    @Schema(description = "월간 레포트 제목", example = "2025년 02월")
    private String monthlyReportTitle;
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    @Schema(description = "레포트 타입", example = "REPORT_WEEKLY")
    private Report.ReportType reportType;
    @Schema(description = "분석 맡길 기록 리스트",
            example = "[{\"content\": \"화창한 하루\", \"recordDateTime\": \"2025-04-12T13:30:00\", \"categoryName\": \"일상\"}]")
    private List<RecordForAnalysisDto> analysisDtoList;
}
