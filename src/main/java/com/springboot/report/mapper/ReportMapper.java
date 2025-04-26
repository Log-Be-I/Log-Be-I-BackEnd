package com.springboot.report.mapper;

import com.springboot.member.entity.Member;
import com.springboot.report.dto.ReportAnalysisResponse;

import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReportMapper {


    //Report 전체 목록 조회
    default List<ReportDto.summaryResponse> reportToSummaryResponse (List<Report> reports){
        //monthlyTitle 기준으로 Map<String, String>> 형태로 그룹핑
        return  reports.stream().collect(Collectors.groupingBy(
                report -> report.getMonthlyTitle(), //각 report에서 monthlyTitle -> key로 추출
                Collectors.mapping( // 각 그룹에서 reportId 만 뽑아서
                        report -> report.getReportId(),
                        Collectors.toList() // 리스트로 모음
                )

        ))//Map<String, List<Long>> -> Set<Entry<String, List<Long>>> : stream
                .entrySet().stream().map(entry ->  //각 entry를 Dto 변환
                        new ReportDto.summaryResponse(entry.getValue(), entry.getKey()))
                .sorted(Comparator.comparing(ReportDto.summaryResponse::getMonthlyTitle)) //정렬
                .collect(Collectors.toList());  //List<ReportDto.SummaryResponse> 반환
    }


    //Report 상세 그룹조회
    default List<ReportDto.Response> reportsToReportsResponseDtos (List<Report> reports){
        return reports.stream().map(
                report-> reportToReportResponseDto(report))
                .collect(Collectors.toList());
    }


    default ReportDto.Response reportToReportResponseDto(Report report){
        return new ReportDto.Response(
                report.getReportId(),
                report.getMonthlyTitle(),
                report.getTitle(),
                report.getContent(),
                report.getReportType(),
                report.getPeriodNumber()
        );
    }

}
