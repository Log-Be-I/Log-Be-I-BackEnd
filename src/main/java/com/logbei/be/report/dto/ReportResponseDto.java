package com.logbei.be.report.dto;

import com.logbei.be.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
@Schema(description = "분석 응답 DTO")
public class ReportResponseDto {
    @Schema(description = "레포트 ID", example = "1")
    private Long reportId;
    @Schema(description = "월간 레포트 제목", example = "2025년 02월")
    private String monthlyTitle;
    @Schema(description = "레포트 제목", example = "2025년 02월 2주차")
    private String title;
    @Schema(
            description = "레포트 내용",
            example = "{\n" +
                    "  \"summary\": \"가장 많이 기록된 Category는 [할 일]이고, 총 9회 기록되었습니다. 주요 활동은 스쿼트 기록 갱신입니다.\",\n" +
                    "  \"emotionRatio\": \"기쁨: 7, 불안: 3, 편안: 8 [긍정: 75%, 중립: 15%, 부정: 10%]\",\n" +
                    "  \"insight\": \"자주 사용한 단어: 회의, 준비, 피드백, 반복된 키워드: 스쿼트 기록, 회의록 요약, 헬스장 이용\",\n" +
                    "  \"suggestion\": \"수요일 저녁에 집중력이 자주 낮아졌습니다. 루틴을 조정해보거나 짧은 산책을 넣어보는 건 어때요?\"\n" +
                    "}"
    )
    private Map<String, String> content;
    @Schema(description = "레포트 타입", example = "REPORT_WEEKLY")
    private Report.ReportType reportType;
    @Schema(description = "월간, 주간 구분 숫자", example = "2025-04-11T11:30")
    private int periodNumber;
    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;
}
