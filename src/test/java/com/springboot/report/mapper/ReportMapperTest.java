package com.springboot.report.mapper;

import com.springboot.report.dto.ReportResponseDto;
import com.springboot.report.dto.SummaryResponseDto;
import com.springboot.report.entity.Report;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportMapperUnitTest {

    private final ReportMapper mapper = new ReportMapperImpl(); // MapStruct mapper 구현체 사용

    @Test
    void reportToSummaryResponse_groupsReportsByMonthlyTitle() {
        // given
        Report report1 = new Report();
        report1.setReportId(1L);
        report1.setMonthlyTitle("2024-01");

        Report report2 = new Report();
        report2.setReportId(2L);
        report2.setMonthlyTitle("2024-01");

        Report report3 = new Report();
        report3.setReportId(3L);
        report3.setMonthlyTitle("2024-02");

        List<Report> input = List.of(report1, report2, report3);

        // when
        List<SummaryResponseDto> result = mapper.reportToSummaryResponse(input);

        // then
        assertThat(result).hasSize(2);

        SummaryResponseDto jan = result.get(0);
        assertThat(jan.getMonthlyTitle()).isEqualTo("2024-01");
        assertThat(jan.getReportIds()).containsExactlyInAnyOrder(1L, 2L);

        SummaryResponseDto feb = result.get(1);
        assertThat(feb.getMonthlyTitle()).isEqualTo("2024-02");
        assertThat(feb.getReportIds()).containsExactly(3L);
    }


    @Test
    void reportsToReportsResponseDtos_convertsReportListCorrectly() {
        // given
        Report report1 = new Report();
        ReflectionTestUtils.setField(report1, "createdAt", LocalDateTime.of(2024, 3, 5, 12, 0));
        report1.setReportId(1L);
        report1.setMonthlyTitle("2024년 03월");
        report1.setTitle("2024년 3월 1주차");
        report1.setContent(Map.of(
                "summary", "요약 내용",
                "emotionRatio", "기쁨: 7, 슬픔: 3",
                "insight", "반복된 키워드: 공부, 산책",
                "suggestion", "주말 루틴 조정 필요"
        ));
        report1.setReportType(Report.ReportType.REPORT_WEEKLY);
        report1.setPeriodNumber(1);

        Report report2 = new Report();
        ReflectionTestUtils.setField(report2, "createdAt", LocalDateTime.of(2024, 3, 12, 8, 30));
        report2.setReportId(2L);
        report2.setMonthlyTitle("2024년 03월");
        report2.setTitle("2024년 3월 2주차");
        report2.setContent(Map.of(
                "summary", "두 번째 요약",
                "emotionRatio", "기쁨: 5, 불안: 4",
                "insight", "자주 사용한 단어: 노력",
                "suggestion", "수면 시간을 늘려보세요"
        ));
        report2.setReportType(Report.ReportType.REPORT_WEEKLY);
        report2.setPeriodNumber(2);

        List<Report> reports = List.of(report1, report2);

        // when
        List<ReportResponseDto> result = mapper.reportsToReportsResponseDtos(reports);

        // then
        assertThat(result).hasSize(2);

        ReportResponseDto dto1 = result.get(0);
        assertThat(dto1.getReportId()).isEqualTo(1L);
        assertThat(dto1.getMonthlyTitle()).isEqualTo("2024년 03월");  // 수정됨
        assertThat(dto1.getTitle()).isEqualTo("2024년 3월 1주차");
        assertThat(dto1.getContent()).isEqualTo(Map.of(
                "emotionRatio", "기쁨: 7, 슬픔: 3",
                "insight", "반복된 키워드: 공부, 산책",
                "suggestion","주말 루틴 조정 필요",
                "summary", "요약 내용"
        ));
        assertThat(dto1.getReportType()).isEqualTo(Report.ReportType.REPORT_WEEKLY);
        assertThat(dto1.getPeriodNumber()).isEqualTo(1);
        assertThat(dto1.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 3, 5, 12, 0));
    }
}


