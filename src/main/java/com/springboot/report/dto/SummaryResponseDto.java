package com.springboot.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SummaryResponseDto {
    private List<Long> reportIds;
    private String monthlyTitle;
}
