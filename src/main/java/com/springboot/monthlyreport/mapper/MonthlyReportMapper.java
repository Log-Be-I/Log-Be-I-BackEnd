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
    @Mapping(target = "memberId", source = "member.memberId")
    List<MonthlyReportDto.Response> monthliesToMonthlyResponses(List<MonthlyReport> monthlyReports);
    @Mapping(target = "memberId", source = "member.memberId")
    MonthlyReportDto.Response monthlyToMonthlyResponse(MonthlyReport monthlyReport);

}
