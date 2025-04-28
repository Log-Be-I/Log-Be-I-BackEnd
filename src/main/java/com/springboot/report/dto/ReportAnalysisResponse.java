package com.springboot.report.dto;

import com.springboot.report.entity.Report;
import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter

@Setter
public class ReportAnalysisResponse {
    //content 안에 있는 내용물(JSON)을 담는 그릇
    private Long memberId;
    private String reportTitle;
    private String monthlyReportTitle;
    private Report.ReportType type;
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
