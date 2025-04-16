package com.springboot.monthlyreport.dto;

import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class MonthlyReportDto {

    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long monthlyId;
        @Schema(name = "월간 분석 제목", example = "2025년 04월")
        private String title;
        @Schema(name = "회원 번호", example = "1")
        private Long memberId;
        @Schema(name = "type 번호", example = "1")
        private LocalDate yearMonth;
        @Schema(name = "월간별 기록들", example = "2025년 04월 1주차..")
        private List<ReportDto.Response> reports;
    }
}
