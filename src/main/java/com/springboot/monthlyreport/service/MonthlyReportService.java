package com.springboot.monthlyreport.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.monthlyreport.repository.MonthlyReportRepository;
import com.springboot.report.entity.Report;
import com.springboot.report.service.ReportService;
import com.springboot.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final MonthlyReportRepository repository;
    private final MemberService memberService;

    //새로운 Report 생성 시 추가하는 로직
    @Transactional
    public void addReportToMonthlyReport(Report report, long memberId) {

        //존재하는 회원인지 검증
        Member member = memberService.validateExistingMember(memberId);
        //Report.title 문자열 -> yyyy년 MM월
        String title = splitReportTitle(report.getTitle());
        //반환된 문자열을  LocalDate 로 변환
        LocalDate reportTitle = DateUtil.parseToLocalDate(title, "yyyy년 M월");
        //해당 LocalDate를 통해 MonthlyReport 조회
        Optional<MonthlyReport> optional = findVerifiedMonthlyReport(memberId,reportTitle);

        MonthlyReport monthlyReport;
        if(optional.isPresent()) {
            //같은 PK를 가진 엔티티 객체를 반환
            monthlyReport = optional.get();
            //이미 포함된 Report 라면 예외처리
            verifyExistsReport(monthlyReport, report);
            //포함되지 않은 Report 라면 추가
            monthlyReport.getReports().add(report);
            //저장
            repository.save(monthlyReport);
            //존재하지 않는다면
        } else {
            MonthlyReport newMonthlyReport = new MonthlyReport();
            newMonthlyReport.getReports().add(report);
            newMonthlyReport.setTitle(DateUtil.formatLocalDateToString(reportTitle, "yyyy년 M월"));
            newMonthlyReport.setMember(member);
            newMonthlyReport.setYearMonth(reportTitle);
            //저장
            repository.save(newMonthlyReport);

        }

    }



    //Report title 중 년/월 까지 추출 (예 : 2025년 4월 1주차 -> 2025년 4월)
    public String splitReportTitle(String title){
        if(title == null) {
            throw new IllegalArgumentException("제목의 데이터가 없습니다.");
        }
        // Report title "주차"가 있는 경우
        if(title.endsWith("주차")){
            //공백 기준으로 분할
            String[] parts = title.split(" ");
            return parts[0] + " " + parts[1];
        } else {
            return title;
        }
    }

    //회원이 가지고 있는 monthlyReport 반환
    public List<MonthlyReport> findVerifiedMonthlyReport(long memberId){
        List<MonthlyReport> findMonthly = repository.findByMemberId(memberId);
        if(findMonthly == null) {
            throw new BusinessLogicException(ExceptionCode.MONTHLY_REPORT_NOT_FOUND);
        }
        return findMonthly;
    }

    //LocalDate 로 monthlyReport 찾아서 반환
    public Optional<MonthlyReport> findVerifiedMonthlyReport(long memberId, LocalDate yaerMonth){
        //존재하는 회원인지 검증
        memberService.validateExistingMember(memberId);
         return repository.findByMemberIdAndYearMonth(memberId, yaerMonth);
    }

    //이미 존재하는 Report의 경우 추가하지 않는다.
    public void verifyExistsReport(MonthlyReport monthlyReport, Report report) {
        if(monthlyReport.getReports().contains(report)) {
            repository.findById(report.getReportId()).orElseThrow(
                    ()->new BusinessLogicException(ExceptionCode.REPORT_EXISTS));
        }
    }


}
