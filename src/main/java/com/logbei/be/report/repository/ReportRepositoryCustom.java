package com.logbei.be.report.repository;

import com.logbei.be.report.entity.Report;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepositoryCustom {
    List<Long> findMemberIdsWithAtLeastWeeklyReportsInMonth(Report.ReportType type, LocalDateTime start, LocalDateTime end, long count);
}
