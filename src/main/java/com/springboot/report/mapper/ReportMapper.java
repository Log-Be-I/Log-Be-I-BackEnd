package com.springboot.report.mapper;

import com.springboot.monthlyreport.dto.MonthlyReportDto;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

//
//@Mapper(componentModel = "spring")
//public interface ReportMapper {
//    Report reportPostToReport(ReportDto.Post post);
//    ReportDto.Response reportToReportResponse(Report report);
//
//
//}

@Mapper(componentModel = "spring")
public interface ReportMapper {

    default Report reportPostToReport(ReportDto.Post post) {

        String[] parts = post.getTitle().split(" ");
        String weekPart = parts[parts.length - 1]; // "1주차"
        if(weekPart.length() != 2) {
            String numberOnly = weekPart.replaceAll("[^0-9]", ""); // 숫자만 추출
            Report report = new Report();
            report.setReportType(post.getReportType());
            report.setTitle(post.getTitle());
            report.setContent(post.getContent());
            report.setPeriodNumber(Integer.parseInt(numberOnly));

            return report;
        } else {
            Report report = new Report();
            report.setReportType(post.getReportType());
            report.setTitle(post.getTitle());
            report.setContent(post.getContent());
            report.setPeriodNumber(0);

            return report;
        }






    }

    default ReportDto.Response reportToResponseDto (Report report) {
        ReportDto.Response response= new ReportDto.Response(
                report.getReportId(),
                report.getTitle(),
                report.getContent(),
                report.getReportType(),
                report.getPeriodNumber(),
                report.getMonthlyReport()
        );
        return response;
    }

}
