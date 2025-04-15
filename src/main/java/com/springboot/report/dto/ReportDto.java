package com.springboot.report.dto;


import com.springboot.report.entity.MonthlyReport;
import com.springboot.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class ReportDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @Schema(name = "분석데이터 제목", example = "2025년 4월 1주차")
        private String title;
        @Schema(name = "분석데이터 내용", example = "4월 1주차에 당신의 감정의 굴곡이 많았습니다. 벚꽃이 만개했을 땐 매우 하이텐션이었는데..")
        private String content;
        @Schema(name = "데이터 분석기간", example = "1주차")
        private Report.ReportType reportType;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long reportId;
        private String title;
        private String content;
        private Report.ReportType reportType;
        private MonthlyReport monthlyReport;
    }
}
