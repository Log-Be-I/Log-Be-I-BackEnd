package com.springboot.report.mapper;

import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    Report reportPostToReport(ReportDto.Post post);
    ReportDto.Response reportToReportResponse(Report report);
    List<ReportDto.Response> reportsToReportResponses(List<Report> reports);

}
