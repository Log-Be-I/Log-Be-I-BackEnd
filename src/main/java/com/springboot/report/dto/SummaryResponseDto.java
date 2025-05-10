package com.springboot.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "분석 기록 확인용 DTO")
public class SummaryResponseDto {
   @Schema(description = "전체 조회 요청", example = "[1, 2, 3]")
    private List<Long> reportIds;
    @Schema(description = "월간 레포트 제목", example = "2025년 02월")
    private String monthlyTitle;
}
