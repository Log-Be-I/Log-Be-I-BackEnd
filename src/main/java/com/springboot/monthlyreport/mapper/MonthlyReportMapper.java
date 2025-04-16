package com.springboot.monthlyreport.mapper;

import com.springboot.monthlyreport.dto.MonthlyReportDto;
import com.springboot.monthlyreport.entity.MonthlyReport;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MonthlyReportMapper {
    MonthlyReport monthlyPostToMonthly(MonthlyReportDto.Post post);
    MonthlyReportDto.Response monthlyToMonthlyResponse(MonthlyReport monthlyReport);
    List<MonthlyReportDto.Response> monthliesToMonthlyResponses(List<MonthlyReport> monthlyReports);
}
