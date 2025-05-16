package com.logbei.be.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기록 분석 요청 DTO")
public class RecordForAnalysisDto {
    @Schema(description = "내용", example = "오늘 하루는 화창한 무지개빛 하늘이야")
    private String content;
    @Schema(description = "기록 날짜", example = "2025-04-12T13:30")
    private LocalDateTime recordDateTime;
    @Schema(description = "카테고리 이름", example = "일상")
    private String categoryName; // or categoryId if needed
}
