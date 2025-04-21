package com.springboot.report.service;


import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.monthlyreport.service.MonthlyReportService;
import com.springboot.report.entity.Report;
import com.springboot.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;


@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository repository;
    private final MonthlyReportService monthlyReportService;

    public Report createReport(Report report, long memberId) {
        //Report 를 MonthlyReport 에 추가하는 로직
        monthlyReportService.addReportToMonthlyReport(report, memberId);

        //해당 report가 주간인지 월간인지 구분
        report.setPeriodNumber(extractPeriodNumber(report.getTitle()));
        setReportType(report);

        return repository.save(report);
    }

    //report title 에서 주차별 월별 구분
    public static int extractPeriodNumber(String title){
       //주간 Report라면 -> title 이 "주차"로 끝나는 경우
        if(title.endsWith("주차")) {
            // 2025년 04월 2주차 -> 2 : 공백으로 구분하여 "N주차" 추출
            String[] parts = title.split(" ");
            //N주차에서 "주차"를 제거하고 숫자만 추출
            String  weekStr = parts[parts.length -1].replace("주차", "");
            //잘못된 title 타입을 받아 정상적인 추출을 하지 못했을 경우 예외처리
            try {
                //문자열 숫자를 정수로 변환
                return Integer.parseInt(weekStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("title에서 주차 숫자 추출 실패: " + title);
            }

       //월간 Report 라면
        } else  {
            return 0;
        }
    }

    //periodNumber가 0(월간) 이면 reportType Monthly로 변겸
    public void setReportType(Report report){
        if(report.getPeriodNumber() == 0){
            //reportType -> month로 변경
            report.setReportType(Report.ReportType.REPORT_MONTHLY);
        } else {
            //periodNumber 1,2,3,4,5 라면 WEEKLY로 변경
            report.setReportType(Report.ReportType.REPORT_WEEKLY);
        }
    }

    //주간 분석 개수 반환
    public int getWeeklyReportCount(YearMonth lastMonth) {
        String yearMonthPrefix = String.format("%d년 %02d월", lastMonth.getYear(), lastMonth.getMonthValue());
        // 1. 해당 월의 주간 Report 개수 조회 (예: JPA 쿼리)
        return repository.countWeeklyReportsByTitle(
                Report.ReportType.REPORT_WEEKLY, yearMonthPrefix + "%", "주차");
    }

    // 단건 조회
    public Report findReport (long reportId) {
       return repository.findById(reportId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.REPORT_NOT_FOUND));
    }


}
