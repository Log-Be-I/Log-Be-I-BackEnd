package com.springboot.report.service;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.report.dto.ReportAnalysisRequest;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;

import com.springboot.report.dto.ReportAnalysisResponse;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.repository.ReportRepository;
import com.springboot.utils.ReportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final MemberService memberService;
    private final ReportRepository repository;
    private final ReportMapper mapper;
    private final GoogleTextToSpeechService googleTextToSpeechService;

    //ai가 분석한 content 타입변환 ReportAnalysisRequest -> ReportAnalysisResponse 변환
    public ReportAnalysisResponse aiRequestToResponse(ReportAnalysisRequest request, Map<String, String> contentMap) {

        //ReportAnalysisRequest -> ReportAnalysisResponse 매핑
        ReportAnalysisResponse response = new ReportAnalysisResponse();
        response.setMemberId(request.getMemberId());
        response.setReportTitle(request.getReportTitle());
        response.setMonthlyReportTitle(request.getMonthlyReportTitle());
        response.setContent(contentMap);

        return response;
    }

    public Report analysisResponseToReport(ReportAnalysisResponse response) {
       //NPE 방지
        Member member = new Member();
        member.setMemberId(response.getMemberId());

        Report report = new Report();
        report.setTitle(response.getReportTitle());
        report.setMonthlyTitle(response.getMonthlyReportTitle());
        report.setMember(member);
        report.setContent(response.getContent());
        //해당 report가 주간인지 월간인지 구분
        report.setPeriodNumber(extractPeriodNumber(response.getReportTitle()));
        setReportType(report);
        log.info("📌 변환된 Report: {}", report);

        return report;
    }

    //ai 응답 -> Report
    public List<Report> analysisResponseToReportList(List<Report> reports) {
//
//        List<Report> reports = responses.stream().map(
//                response -> analysisResponseToReport(response)).collect(Collectors.toList());
//
//        //생성된 List<Report> DB 저장
//        return repository.saveAll(reports);
        try {
//            List<Report> reports = responses.stream()
//                    .map(response -> analysisResponseToReport(response))
//                    .collect(Collectors.toList());
            log.info("📦 DB 저장 직전 - reports size: {}, titles: {}", reports.size(), reports.stream().map(Report::getTitle).collect(Collectors.toList()));
            return repository.saveAll(reports);
        } catch (Exception e) {
            log.error("💥 Report 변환 중 에러 발생", e);
            throw e;
        }
    }






//    public List<Report> createReport(List<ReportAnalysisResponse> responses) {
//
//
//        //mapper 로 매핑 List<ReportAnalysisResponse> -> List<Report> 변환
//        List<Report> reports = mapper.analysisResponseToReportList(responses);
//        reports.stream().map(report -> report.setPeriodNumber(extractPeriodNumber(response.getReportTitle()));)
//
//        return repository.saveAll(reports);
//    }

    //연도별 전체조회
    public List<Report> findMonthlyReports(long memberId,int year) {
        String yearStr = year + "년";
        return repository.findByMember_MemberIdAndMonthlyTitleStartingWith(memberId, yearStr);
    }

    //Report
    public List<Report> findMonthlyTitleWithReports(String monthlyTitle, long memberId) {
        return repository.findByMember_MemberIdAndMonthlyTitle(memberId, monthlyTitle);

    }


    public List<String> reportToClovaAudio(List<Long> reportsId, long memberId){
        // 유효한 회원인지 검증
        Member member = memberService.validateExistingMember(memberId);
        //활동중인 회원인지 확인
        memberService.validateMemberStatus(member);

        try {
            // reportId 로 report 를 찾아서 List<Report> 생성
            List<Report> reportList = reportsId.stream()
                    .map(reportId -> findReport(reportId))
                    .collect(Collectors.toList());
            // 생성된 파일 이름을 담을 리스트
            List<String> filePathList = new ArrayList<>();
            // 리포트 리스트를 돌면서 하나하나 TTS 변환기에 넣기
            reportList.stream().forEach(record ->
            {
                try {
                    // UUID 로 겹치지 않는 파일명 생성
                    String fileName = UUID.randomUUID().toString() + ".mp3";
                    // 제목과 내용을 같이 전달해서 시작하는 글의 날짜를 말하게 함
                    googleTextToSpeechService.synthesizeText(record.getTitle() + record.getContent(), fileName);
                    // 생성된 파일 경로 복사
                    filePathList.add(fileName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return filePathList;
        } catch (Exception e) {
            log.error("Google TTS 오류 발생", e);
            // 에러 터졌을때는 빈배열 반환
          throw new BusinessLogicException(ExceptionCode.INVALID_SERVER_ERROR);
        }
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
  
    // report 단건 조회
    public Report findReport(long reportId) {
        return repository.findById(reportId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.REPORT_NOT_FOUND));
    }


}
