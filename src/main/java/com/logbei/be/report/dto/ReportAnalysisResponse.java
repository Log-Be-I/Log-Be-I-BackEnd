package com.logbei.be.report.dto;

import com.logbei.be.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter

@Setter
@Schema(description = "기록 분석 GPT 응답 데이터 DTO")
public class ReportAnalysisResponse {
    //content 안에 있는 내용물(JSON)을 담는 그릇
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    @Schema(description = "레포트 제목", example = "2025년 02월 1주차")
    private String reportTitle;
    @Schema(description = "월간 레포트 제목", example = "2025년 02월")
    private String monthlyReportTitle;
    @Schema(description = "레포트 타입", example = "REPORT_WEEKLY")
    private Report.ReportType type;
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
//    private String summary;          // ✅ 기록한 총 횟수, 많이 한 활동
//    private String emotionRatio;     // 😊 긍정/중립/부정 비율
//    private String insight;          // 🧠 자주 쓴 단어, 반복된 키워드
//    private String suggestion;       // 💡 제안
//
//    // 월간일 경우에만 응답받는 필드
//    private String categoryStat;     // 📝 카테고리별 활동 통계
//    private String pattern;          // 📊 시간대/요일 패턴
}
