package com.logbei.be.report.service;

import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.report.dto.ReportAnalysisRequest;
import com.logbei.be.report.dto.ReportAnalysisResponse;
import com.logbei.be.report.entity.Report;
import com.logbei.be.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("ReportAnalysisRequest -> ReportAnalysisResponse 변환 성공")
    void aiRequestToResponse_convertsCorrectly() {
        // given
        ReportAnalysisRequest request = new ReportAnalysisRequest(
                "2025년 04월 2주차",
                "2025년 04월",
                1L,
                Report.ReportType.REPORT_WEEKLY,
                List.of() // 생략 가능
        );

        Map<String, String> contentMap = Map.of("summary", "요약입니다");

        // when
        ReportAnalysisResponse response = reportService.aiRequestToResponse(request, contentMap);

        // then
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getReportTitle()).isEqualTo("2025년 04월 2주차");
        assertThat(response.getMonthlyReportTitle()).isEqualTo("2025년 04월");
        assertThat(response.getType()).isEqualTo(Report.ReportType.REPORT_WEEKLY);
        assertThat(response.getContent()).containsEntry("summary", "요약입니다");
    }

    @Test
    @DisplayName("ReportAnalysisResponse -> Report 엔티티 변환 성공")
    void analysisResponseToReport_convertsCorrectly() {
        // given
        ReportAnalysisResponse response = new ReportAnalysisResponse();
        response.setMemberId(1L);
        response.setReportTitle("2025년 04월 2주차");
        response.setMonthlyReportTitle("2025년 04월");
        response.setType(Report.ReportType.REPORT_WEEKLY);
        response.setContent(Map.of("summary", "요약 내용"));

        // when
        Report report = reportService.analysisResponseToReport(response);

        // then
        assertThat(report.getMember().getMemberId()).isEqualTo(1L);
        assertThat(report.getTitle()).isEqualTo("2025년 04월 2주차");
        assertThat(report.getMonthlyTitle()).isEqualTo("2025년 04월");
        assertThat(report.getReportType()).isEqualTo(Report.ReportType.REPORT_WEEKLY);
        assertThat(report.getPeriodNumber()).isEqualTo(2);
        assertThat(report.getContent()).containsEntry("summary", "요약 내용");
    }

    @Test
    @DisplayName("연도별 리포트 조회 - 2024년 기준")
    void findMonthlyReports_returnsCorrectReportsForGivenYear() {
        // given
        long memberId = 1L;
        int year = 2024;
        String yearPrefix = "2024년";

        Report report1 = new Report();
        report1.setMonthlyTitle("2024년 01월");

        Report report2 = new Report();
        report2.setMonthlyTitle("2024년 02월");

        List<Report> mockReports = List.of(report1, report2);

        when(reportRepository.findByMember_MemberIdAndMonthlyTitleStartingWith(memberId, yearPrefix))
                .thenReturn(mockReports);

        // when
        List<Report> result = reportService.findMonthlyReports(memberId, year);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Report::getMonthlyTitle)
                .containsExactlyInAnyOrder("2024년 01월", "2024년 02월");

        verify(reportRepository, times(1))
                .findByMember_MemberIdAndMonthlyTitleStartingWith(memberId, yearPrefix);
    }

    @Test
    @DisplayName("월별 리포트 상세 조회 - 2024년 03월")
    void findMonthlyTitleWithReports_returnsReportsByMonthlyTitle() {
        // given
        long memberId = 1L;
        String monthlyTitle = "2024년 03월";

        Report report1 = new Report();
        report1.setReportId(1L);
        report1.setMonthlyTitle(monthlyTitle);

        Report report2 = new Report();
        report2.setReportId(2L);
        report2.setMonthlyTitle(monthlyTitle);

        List<Report> mockReports = List.of(report1, report2);

        when(reportRepository.findByMember_MemberIdAndMonthlyTitle(memberId, monthlyTitle))
                .thenReturn(mockReports);

        // when
        List<Report> result = reportService.findMonthlyTitleWithReports(monthlyTitle, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Report::getMonthlyTitle)
                .containsOnly(monthlyTitle);

        verify(reportRepository, times(1))
                .findByMember_MemberIdAndMonthlyTitle(memberId, monthlyTitle);
    }

    @Test
    @DisplayName("리포트 ID로 조회 성공")
    void findVerifiedExistsReport_returnsReport() {
        // given
        long reportId = 1L;
        Report mockReport = new Report();
        mockReport.setReportId(reportId);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));

        // when
        Report result = reportService.findVerifiedExistsReport(reportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReportId()).isEqualTo(reportId);
        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("리포트 ID로 조회 실패 시 예외 발생")
    void findVerifiedExistsReport_throwsIfNotFound() {
        // given
        long reportId = 99L;
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessLogicException.class, () -> {
            reportService.findVerifiedExistsReport(reportId);
        });

        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("제목에서 주차 숫자 추출 성공")
    void extractPeriodNumber_returnsCorrectWeek() {
        assertThat(ReportService.extractPeriodNumber("2025년 04월 3주차")).isEqualTo(3);
    }

    @Test
    @DisplayName("월간 리포트 제목일 경우 0 반환")
    void extractPeriodNumber_returnsZeroIfMonthly() {
        assertThat(ReportService.extractPeriodNumber("2025년 04월")).isEqualTo(0);
    }

    @Test
    @DisplayName("잘못된 포맷 예외 처리")
    void extractPeriodNumber_throwsIfInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                ReportService.extractPeriodNumber("2025년 04월 x주차"));  // ✅ 여기서 예외 발생
    }
}