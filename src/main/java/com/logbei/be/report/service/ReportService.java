package com.logbei.be.report.service;

import com.logbei.be.ai.googleTTS.GoogleTextToSpeechService;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.pushToken.service.PushTokenService;
import com.logbei.be.report.dto.ReportAnalysisRequest;

import com.logbei.be.exception.exception.BusinessLogicException;
import com.logbei.be.exception.exception.ExceptionCode;

import com.logbei.be.report.dto.ReportAnalysisResponse;
import com.logbei.be.report.entity.Report;
import com.logbei.be.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final MemberService memberService;
    private final ReportRepository repository;
    private final GoogleTextToSpeechService googleTextToSpeechService;
    private final PushTokenService pushTokenService;

    //ai가 분석한 content 타입변환 ReportAnalysisRequest -> ReportAnalysisResponse 변환
    public ReportAnalysisResponse aiRequestToResponse(ReportAnalysisRequest request, Map<String, String> contentMap) {
        //ReportAnalysisRequest -> ReportAnalysisResponse 매핑
        ReportAnalysisResponse response = new ReportAnalysisResponse();
        response.setMemberId(request.getMemberId());
        response.setReportTitle(request.getReportTitle());
        response.setMonthlyReportTitle(request.getMonthlyReportTitle());
        response.setType(request.getReportType());
        //Map<K,V> -> chatGPT 한테 받은 JSON 형태의 분석 데이터 매핑
        response.setContent(contentMap);

        return response;
    }

    //ReportAnalysisResponse -> Report 변환
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
        report.setReportType(response.getType());
//        setReportType(report);
        log.info("📌 변환된 Report: {}", report);

        return report;
    }

    // DB에 저장
    public List<Report> analysisResponseToReportList(List<Report> reports) {
        //생성된 List<Report> DB 저장
        log.info("📦 DB 저장 직전 - reports size: {}, titles: {}", reports.size(), reports.stream().map(Report::getTitle).collect(Collectors.toList()));
        List<Report> savedReports = repository.saveAll(reports);

        // 저장된 각 레포트에 대해 알림 전송
        savedReports.forEach(report -> {
            String notificationTitle = report.getReportType() == Report.ReportType.REPORT_WEEKLY ?
                    "주간 분석 리포트" : "월간 분석 리포트";
            String notificationContent = String.format(
                    "%s 분석이 완료되었습니다.",
                    report.getTitle()
            );

            pushTokenService.sendAnalysisNotification(
                    report.getMember().getMemberId(),
                    notificationTitle,
                    notificationContent
            );
        });

        return savedReports;
    }

    // GET : 연도별 전체조회
    public List<Report> findMonthlyReports(long memberId, int year) {
        String yearStr = year + "년";
        return repository.findByMember_MemberIdAndMonthlyTitleStartingWith(memberId, yearStr);
    }

    // GET : Report 상세조회
    public List<Report> findMonthlyTitleWithReports(String monthlyTitle, long memberId) {
        return repository.findByMember_MemberIdAndMonthlyTitle(memberId, monthlyTitle);

    }

    //등록된 Report -> TTS : 음성 출력
    public List<String> reportToGoogleAudio(List<Long> reportsId, long memberId) {
        // 유효한 회원인지 검증
        Member member = memberService.findVerifiedExistsMember(memberId);
        //활동중인 회원인지 확인
        memberService.validateMemberStatus(member);

        try {
            // reportId 로 report 를 찾아서 List<Report> 생성
            List<Report> reportList = reportsId.stream()
                    .map(reportId -> findVerifiedExistsReport(reportId))
                    .collect(Collectors.toList());
            // 생성된 파일 이름을 담을 리스트
            List<String> filePathList = new ArrayList<>();
            // 리포트 리스트를 돌면서 하나하나 TTS 변환기에 넣기
            reportList.stream().forEach(report ->
            {
                try {
                    // UUID 로 겹치지 않는 파일명 생성
                    String fileName = UUID.randomUUID().toString() + ".mp3";
                    // 제목과 내용을 같이 전달해서 시작하는 글의 날짜를 말하게 함
                    googleTextToSpeechService.synthesizeText(report.getTitle() + report.getContent(), fileName);
                    // 생성된 파일 경로 복사
                    filePathList.add("https://logbe-i.com/" + fileName);
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
    public static int extractPeriodNumber(String title) {
        //주간 Report라면 -> title 이 "주차"로 끝나는 경우
        if (title.endsWith("주차")) {
            // 2025년 04월 2주차 -> 2 : 공백으로 구분하여 "N주차" 추출
            String[] parts = title.split(" ");
            //N주차에서 "주차"를 제거하고 숫자만 추출
            String weekStr = parts[parts.length - 1].replace("주차", "");
            //잘못된 title 타입을 받아 정상적인 추출을 하지 못했을 경우 예외처리
            try {
                //문자열 숫자를 정수로 변환
                return Integer.parseInt(weekStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("title에서 주차 숫자 추출 실패: " + title);
            }

            //월간 Report 라면
        } else {
            return 0;
        }
    }

    //주간 분석 개수 반환 : 월간 분석 조건 - 주간분석 2개 이상 시 실행
    public List<Long> getMemberIdWithAtLeastTwoWeeklyReports(LocalDateTime start, LocalDateTime end){
        return repository.findMemberIdsWithAtLeastWeeklyReportsInMonth(Report.ReportType.REPORT_WEEKLY, start, end, 2);
    }


    // report 단건 조회
    public Report findVerifiedExistsReport(long reportId) {
        return repository.findById(reportId).orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.REPORT_NOT_FOUND));
    }
}
