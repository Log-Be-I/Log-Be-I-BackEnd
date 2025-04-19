package com.springboot.monthlyreport.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.monthlyreport.entity.MonthlyReport;
import com.springboot.monthlyreport.repository.MonthlyReportRepository;
import com.springboot.report.entity.Report;
import com.springboot.utils.AuthorizationUtils;
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
        //회원이 가지고 있는 monthlyReport에 같은 title이 있다면 꺼내고 없다면 새로 만든다.
        MonthlyReport monthlyReport = getOrCreateMonthlyReport(report.getMonthlyReport().getTitle(), memberId);
//        //Report.title 문자열 -> yyyy년 MM월
//        String title = splitReportTitle(report.getTitle());
//        //반환된 문자열을  LocalDate 로 변환
//        LocalDate reportTitle = DateUtil.parseToLocalDate(title, "yyyy년 M월");
//        //해당 LocalDate를 통해 MonthlyReport 조회
//        Optional<MonthlyReport> optional = findVerifiedMonthlyReport(memberId,reportTitle);

//        //이미 포함된 Report 라면 예외처리
//        verifyExistsReport(monthlyReport, report);
        //포함되지 않은 Report 라면 추가
        //Report 추가  양방향 연관 설정
        report.setMonthlyReport(monthlyReport);
//        monthlyReport.addReport(report);

        //저장
        repository.save(monthlyReport);


    }

    //특정 회원이 같은 title의 monthlyReport가 있다면 꺼내고, 없다면 새로 만들어서 꺼내기
    private MonthlyReport getOrCreateMonthlyReport(String yearMonthTitle, Long memberId) {
        return repository.findByMember_MemberIdAndTitle(memberId, yearMonthTitle)
                .orElseGet(() -> {
                    MonthlyReport newReport = new MonthlyReport();
                    newReport.setTitle(yearMonthTitle);
                    newReport.setMember(memberService.validateExistingMember(memberId));
                    newReport.setYearMonth(LocalDate.now());
                    return repository.save(newReport);
                });
    }

    //상세 조회
    public MonthlyReport findMonthlyReport(long monthlyId, long memberId){
        //존재하는 회원인지 확인
//        memberService.validateExistingMember(memberId);
        //이미 등록된 monthlyReport인지 확인
        MonthlyReport monthlyReport = findVerifiedMonthlyReport(monthlyId);
        //작성자 본인 또는 관리자 인지 확인
        AuthorizationUtils.isAdminOrOwner(monthlyReport.getMember().getMemberId(), memberId);
        return monthlyReport;
    }

    //연도별 전체 조회
    //@RequestParam으로 year 입력 받음
    public List<MonthlyReport> findMonthlyReports(int year, long memberId) {
        //존재하는 회원인지 확인
        memberService.validateExistingMember(memberId);
        //회원이 가지고 있는 List를 연도별 + 월별내림차순 형태로 반환
        return findVerifiedMonthlyReportList(year, memberId);

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
            return parts[0] + " " + parts[1] + " 1일";
        } else {
            return title;
        }
    }

    //회원이 가지고 있는 monthlyReport 반환
    public List<MonthlyReport> findVerifiedMonthlyReportList(int year, long memberId){
        return repository.findByMember_MemberIdAndYearOrderByYearMonthDesc(memberId, year);

    }

    //monthlyReport 반환
    public MonthlyReport findVerifiedMonthlyReport(long monthlyId){

        return repository.findById(monthlyId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.MONTHLY_REPORT_NOT_FOUND)
        );
    }

    //LocalDate 로 monthlyReport 찾아서 반환 없으면 비어있는 곳
    public Optional<MonthlyReport> findVerifiedMonthlyReport(long memberId, LocalDate yaerMonth){
        //존재하는 회원인지 검증
        memberService.validateExistingMember(memberId);
         return repository.findByMember_MemberIdAndYearMonth(memberId, yaerMonth);
    }

    //이미 존재하는 Report의 경우 추가하지 않는다.
    public void verifyExistsReport(MonthlyReport monthlyReport, Report report) {
        if(monthlyReport.getReports().contains(report)) {
            repository.findById(report.getReportId()).orElseThrow(
                    ()->new BusinessLogicException(ExceptionCode.REPORT_EXISTS));
        }
    }


}
