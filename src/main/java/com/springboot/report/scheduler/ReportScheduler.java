package com.logbei.be.report.scheduler;

import com.logbei.be.ai.openai.service.OpenAiService;
import com.logbei.be.record.entity.Record;
import com.logbei.be.record.service.RecordService;
import com.logbei.be.report.dto.ReportAnalysisRequest;
import com.logbei.be.report.entity.Report;

import com.logbei.be.report.service.ReportService;
import com.logbei.be.utils.ReportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReportScheduler {
    private final ReportService reportService;
    private final RecordService recordService;
    private final OpenAiService openAiService;


    //매주 월요일 07:00에 실행
    @Scheduled(cron = "0 0 7 * * MON")
    @Transactional
    @Async //비동기처리
    public void sendWeeklyRecordsToAi() throws IOException { //OpenAiService 내부에서 HTTP 요청 시 IOException 발생할 수 있음
        //오늘이 4/14(월) 라면, 전 주 월요일은 4/7
        LocalDateTime today = LocalDateTime.now();
        //전 주 월요일(4/7) 00:00:00
        LocalDateTime weekStart = today.minusWeeks(1).with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        //전 주 일요일(4/13) 23:59:59
        LocalDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        //주간 기록 10개 이상인 회원의 기록 묶음
        List<List<Record>> weeklyRecordsByMember = recordService.getWeeklyRecords(weekStart, weekEnd);

        //분석 조건 : 기록이 10개 이상일 때만 실행
        if (weeklyRecordsByMember.isEmpty()) {
            log.info("주간 기록이 10개 이상인 사용자가 없어 리포트 생성을 생략합니다.");
            return;
        }
        List<ReportAnalysisRequest> weeklies = ReportUtil.toReportRequests(weeklyRecordsByMember, Report.ReportType.REPORT_WEEKLY);
        log.info("✅ 주간 리포트 생성 시작");
        //ai에 해당 데이터 전달
        openAiService.createReportsFromAiInBatch(weeklies);

    }

    @Scheduled(cron = "0 0 6 1 * *")
    @Transactional
    @Async //비동기처리
    public void sendMonthlyRecordsToAi() throws IOException {

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        //전 달 1일 00:00:00
        LocalDateTime monthStart = lastMonth.atDay(1).atStartOfDay();
        //전 달 말일 23:59:59
        LocalDateTime monthEnd = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        // 월간분석 조건 검증 : 주간 분석 개수가 2개 이상인 경우에만 월간분석 가능
        List<Long> memberIds = reportService.getMemberIdWithAtLeastTwoWeeklyReports(monthStart, monthEnd);
        //월간 리포트 생성
        if (!memberIds.isEmpty()) {
            List<List<Record>> monthlyRecords = recordService.getMonthlyRecordsByMemberIds(memberIds, monthStart, monthEnd);
            List<ReportAnalysisRequest> monthlies = ReportUtil.toReportRequests(monthlyRecords, Report.ReportType.REPORT_MONTHLY);
            log.info("월간 리포트 생성 시작");
            //ai에 해당 데이터 전달
            openAiService.createReportsFromAiInBatch(monthlies);
        } else  {
            log.info("월간 리포트 생성 조건을 만족하는 회원이 없습니다.");
        }
    }

}
