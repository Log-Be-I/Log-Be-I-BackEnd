package com.springboot.report.mapper;

import com.springboot.member.entity.Member;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.report.dto.ReportAnalysisResponse;
import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    //ai 응답 -> ReportDto.Post 변환
    default ReportDto.Post reportAnalysisResponseToReportPost(ReportAnalysisResponse aiResponse){
        ReportDto.Post post = new ReportDto.Post();
        post.setTitle(aiResponse.getReportTitle());
        post.setContent(aiResponse.getContent());
        post.setMonthlyReportTitle(aiResponse.getMonthlyReportTitle());
        post.setMemberId(aiResponse.getMemberId());
        return post;

    }
    default Report reportPostToReport(ReportDto.Post post){
         Report report = new Report();
         report.setTitle(post.getTitle());
         report.setContent(post.getContent());
         //report가
         MonthlyReport monthlyReport = new MonthlyReport();
         monthlyReport.setTitle(post.getMonthlyReportTitle());
//         Member member = new Member();
//         member.setMemberId(post.getMemberId());
//         monthlyReport.setMember(member);
         report.setMonthlyReport(monthlyReport);
         return report;
     }

    ReportDto.Response reportToReportResponse(Report report);
    List<ReportDto.Response> reportsToReportResponses(List<Report> reports);

}
