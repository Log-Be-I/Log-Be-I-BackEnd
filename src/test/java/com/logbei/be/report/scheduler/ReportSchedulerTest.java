package com.logbei.be.report.scheduler;

import com.logbei.be.ai.openai.service.OpenAiService;
import com.logbei.be.record.entity.Record;
import com.logbei.be.record.service.RecordService;
import com.logbei.be.report.dto.ReportAnalysisRequest;
import com.logbei.be.report.entity.Report;
import com.logbei.be.report.service.ReportService;
import com.logbei.be.utils.ReportUtil;
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
        List<Record> dummyRecordsList = Collections.nCopies(10, new Record());
        List<List<Record>> dummyRecords = List.of(dummyRecordsList);
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
        List<Long> memberIds = List.of(1L, 2L);
        when(reportService.getMemberIdWithAtLeastTwoWeeklyReports(any(), any())).thenReturn(memberIds);

        List<Record> memberRecords = Collections.nCopies(10, new Record());
        List<List<Record>> dummyMonthlyRecords = List.of(memberRecords);
        when(recordService.getMonthlyRecordsByMemberIds(any(), any(), any())).thenReturn(dummyMonthlyRecords);

        List<ReportAnalysisRequest> mockRequests = List.of(mock(ReportAnalysisRequest.class));
        try (MockedStatic<ReportUtil> utilities = mockStatic(ReportUtil.class)) {
            utilities.when(() -> ReportUtil.toReportRequests(dummyMonthlyRecords, Report.ReportType.REPORT_MONTHLY))
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
        when(reportService.getMemberIdWithAtLeastTwoWeeklyReports(any(), any())).thenReturn(Collections.emptyList());

        // when
        reportScheduler.sendMonthlyRecordsToAi();

        // then
        verify(recordService, never()).getMonthlyRecordsByMemberIds(any(), any(), any());
        verify(openAiService, never()).createReportsFromAiInBatch(any());
    }
}