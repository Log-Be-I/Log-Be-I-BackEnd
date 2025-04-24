package com.springboot.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecordForAnalysisDto {
    private String content;
    private LocalDateTime recordDateTime;
    private String categoryName; // or categoryId if needed
}
