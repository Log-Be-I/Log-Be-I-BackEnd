package com.springboot.report.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.springboot.report.entity.QReport;
import com.springboot.report.entity.Report;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findMemberIdsWithAtLeastWeeklyReportsInMonth(Report.ReportType type, LocalDateTime start, LocalDateTime end, long count) {
        QReport report = QReport.report;

        return queryFactory
                .select(report.member.memberId)
                .from(report)
                .where(
                        report.reportType.eq(type),
                        report.createdAt.between(start, end)
                )
                .groupBy(report.member.memberId)
                .having(report.count().goe(count))
                .fetch();
    }


}
