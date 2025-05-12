package com.springboot.report.service;

import com.springboot.ai.googleTTS.GoogleTextToSpeechService;
import com.springboot.member.service.MemberService;
import com.springboot.pushToken.service.PushTokenService;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.dto.ReportAnalysisResponse;
import com.springboot.report.entity.Report;
import com.springboot.report.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private GoogleTextToSpeechService googleTextToSpeechService;

    @Mock
    private PushTokenService pushTokenService;

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