package com.springboot.report.mapper;

import com.springboot.report.dto.ReportAnalysisResponse;

import com.springboot.report.dto.ReportDto;
import com.springboot.report.entity.Report;
import org.mapstruct.Mapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReportMapper {


    //ai 응답 -> Report
//    default List<Report> analysisResponseToEntityList(List<Report> reports) {
//        return responses.stream()
//                .map(response -> {
//                    Report report = new Report();
//                    report.getMember().setMemberId(response.getMemberId());
//                    report.setMonthlyTitle(response.getMonthlyReportTitle());
//                    report.setTitle(response.getReportTitle());
//                    report.setContent(response.getContent());
//                    report.setReportType(Report.ReportType.REPORT_WEEKLY); // or MONTHLY, 타입 필요 시 response에 추가
//                    report.setPeriodNumber(0);
//                    return report;
//                })
//                .collect(Collectors.toList());
//    }

    //Report 전체 목록 조회
    default List<ReportDto.summaryResponse> reportTosummaryResponse (List<Report> reports){
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
    List<ReportDto.Response> reportsToReportsResponseDtos (List<Report> reports);


    ReportDto.Response reportToReportResponseDto(Report report);

}
