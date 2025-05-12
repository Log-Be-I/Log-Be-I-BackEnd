package com.springboot.report.scheduler;

import com.springboot.ai.openai.service.OpenAiService;
import com.springboot.record.entity.Record;
import com.springboot.record.service.RecordService;
import com.springboot.report.dto.ReportAnalysisRequest;
import com.springboot.report.entity.Report;
import com.springboot.report.service.ReportService;
import com.springboot.utils.ReportUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportSchedulerTest {

    @InjectMocks
    private ReportScheduler reportScheduler;

    @Mock
    private ReportService reportService;

    @Mock
    private RecordService recordService;

    @Mock
    private OpenAiService openAiService;

    @BeforeEach
    void setup() {
        Mockito.reset(reportService, recordService, openAiService);
    }

    @Test
    void sendWeeklyRecordsToAi_shouldGenerateReport_whenRecordCountIsSufficient() throws IOException {
        // given
        List<Record> dummyRecords = Collections.nCopies(10, new Record()); // 10개 이상
        when(recordService.getWeeklyRecords(any(), any())).thenReturn(dummyRecords);

        List<ReportAnalysisRequest> mockRequests = List.of(mock(ReportAnalysisRequest.class));
        try (MockedStatic<ReportUtil> utilities = mockStatic(ReportUtil.class)) {
            utilities.when(() -> ReportUtil.toReportRequests(dummyRecords, Report.ReportType.REPORT_WEEKLY))
                    .thenReturn(mockRequests);

            // when
            reportScheduler.sendWeeklyRecordsToAi();

            // then
            verify(openAiService).createReportsFromAiInBatch(mockRequests);
        }
    }

    @Test
    void sendWeeklyRecordsToAi_shouldNotGenerateReport_whenRecordCountIsInsufficient() throws IOException {
        // given
        when(recordService.getWeeklyRecords(any(), any())).thenReturn(List.of());

        // when
        reportScheduler.sendWeeklyRecordsToAi();

        // then
        verify(openAiService, never()).createReportsFromAiInBatch(any());
    }

    @Test
    void sendMonthlyRecordsToAi_shouldGenerateReport_whenWeeklyCountSufficient() throws IOException {
        // given
        when(reportService.getWeeklyReportCount(any())).thenReturn(2);
        List<Record> dummyMonthly = Collections.nCopies(15, new Record());
        when(recordService.getMonthlyRecords(any(), any())).thenReturn(dummyMonthly);

        List<ReportAnalysisRequest> mockRequests = List.of(mock(ReportAnalysisRequest.class));
        try (MockedStatic<ReportUtil> utilities = mockStatic(ReportUtil.class)) {
            utilities.when(() -> ReportUtil.toReportRequests(dummyMonthly, Report.ReportType.REPORT_MONTHLY))
                    .thenReturn(mockRequests);

            // when
            reportScheduler.sendMonthlyRecordsToAi();

            // then
            verify(openAiService).createReportsFromAiInBatch(mockRequests);
        }
    }

    @Test
    void sendMonthlyRecordsToAi_shouldNotGenerateReport_whenWeeklyCountInsufficient() throws IOException {
        // given
        when(reportService.getWeeklyReportCount(any())).thenReturn(1);

        // when
        reportScheduler.sendMonthlyRecordsToAi();

        // then
        verify(recordService, never()).getMonthlyRecords(any(), any());
        verify(openAiService, never()).createReportsFromAiInBatch(any());
    }
}