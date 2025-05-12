package com.springboot.report.controller;

import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.report.dto.SummaryResponseDto;
import com.springboot.report.entity.Report;
import com.springboot.report.mapper.ReportMapper;
import com.springboot.report.service.ReportService;
import com.springboot.responsedto.ListResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Mock
    private ReportMapper mapper;

    private CustomPrincipal customPrincipal;

    @BeforeEach
    void setUp() {
        customPrincipal = mock(CustomPrincipal.class);
        when(customPrincipal.getMemberId()).thenReturn(1L);
    }

    @Test
    @DisplayName("음성 리포트 변환 성공 - 단위 테스트")
    void generateTts_returnsAudioUrls() {
        // given
        List<Long> reportIds = List.of(1L, 2L);
        List<String> expectedUrls = List.of("https://tts.com/audio1.mp3", "https://tts.com/audio2.mp3");

        when(reportService.reportToGoogleAudio(reportIds, 1L)).thenReturn(expectedUrls);

        // when
        ResponseEntity<List<String>> response = reportController.generateTts(reportIds, customPrincipal);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedUrls, response.getBody());
        verify(reportService, times(1)).reportToGoogleAudio(reportIds, 1L);
    }

    @Test
    void getReportList_returnsReportSummaryList_whenYearProvided() {
        // given
        Long memberId = 1L;
        int year = 2024;
        List<Report> mockReports = List.of(new Report());
        List<SummaryResponseDto> summaryDtos = List.of(new SummaryResponseDto());

        when(customPrincipal.getMemberId()).thenReturn(memberId);
        when(reportService.findMonthlyReports(memberId, year)).thenReturn(mockReports);
        when(mapper.reportToSummaryResponse(mockReports)).thenReturn(summaryDtos);

        // when
        ResponseEntity<?> response = reportController.getReportList(year, customPrincipal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ListResponseDto<?>) response.getBody()).getData()).hasSize(1);
    }

    @Test
    void getReportList_usesCurrentYear_whenYearIsNull() {
        // given
        Long memberId = 2L;
        int currentYear = LocalDate.now().getYear();
        List<Report> mockReports = List.of(new Report());
        List<SummaryResponseDto> summaryDtos = List.of(new SummaryResponseDto());

        when(customPrincipal.getMemberId()).thenReturn(memberId);
        when(reportService.findMonthlyReports(memberId, currentYear)).thenReturn(mockReports);
        when(mapper.reportToSummaryResponse(mockReports)).thenReturn(summaryDtos);

        // when
        ResponseEntity<?> response = reportController.getReportList(null, customPrincipal);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ListResponseDto<?>) response.getBody()).getData()).hasSize(1);
    }

}