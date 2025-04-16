package com.springboot.monthlyreport.mapper;

import com.springboot.monthlyreport.dto.MonthlyReportDto;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MonthlyReportMapper {

    List<MonthlyReportDto.Response> monthliesToMonthlyResponses(List<MonthlyReport> monthlyReports);

    default  MonthlyReportDto.Response monthlyToMonthlyResponse(MonthlyReport monthlyReport) {
        return new MonthlyReportDto.Response(
                monthlyReport.getMonthlyId(),
                monthlyReport.getTitle(),
                monthlyReport.getMember().getMemberId(),
                monthlyReport.getYearMonth(),
                reportsToReportResponses(monthlyReport.getReports())
        );

    }

    default List<ReportDto.Response> reportsToReportResponses(List<Report> reports) {
        return reports.stream()
                .map(report -> reportToReportResponse(report))
                .collect(Collectors.toList());

    }

    default ReportDto.Response reportToReportResponse(Report report){
        return new ReportDto.Response(
                report.getReportId(),
                report.getTitle(),
                report.getContent(),
                report.getReportType(),
                report.getPeriodNumber(),
                report.getMonthlyReport()

        );
    }
}
