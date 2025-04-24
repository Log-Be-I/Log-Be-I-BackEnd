package com.springboot.report.mapper;

import com.springboot.report.dto.ReportAnalysisResponse;

import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReportMapper {


    //ai 응답 -> Report
    default List<Report> analysisResponseToEntityList(List<ReportAnalysisResponse> responses) {
        return responses.stream()
                .map(response -> {
                    Report report = new Report();
                    report.getMember().setMemberId(response.getMemberId());
                    report.setMonthlyTitle(response.getMonthlyReportTitle());
                    report.setTitle(response.getReportTitle());
                    report.setContent(response.getContent());
                    report.setReportType(Report.ReportType.REPORT_WEEKLY); // or MONTHLY, 타입 필요 시 response에 추가
                    report.setPeriodNumber(0);
                    return report;
                })
                .collect(Collectors.toList());
    }



}
