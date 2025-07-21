package com.logbei.be.report.repository;

import com.logbei.be.report.entity.Report;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;


public interface ReportRepository extends JpaRepository<Report, Long>, ReportRepositoryCustom{
   //특정회원의 Report 연도별 전체조회
    List<Report> findByMember_MemberIdAndMonthlyTitleStartingWith(Long memberId, String monthlyTitle);
    //특정회원의 특정 년/월의 Report 조회
    List<Report> findByMember_MemberIdAndMonthlyTitle(Long memberId, String monthlyTitle);
    List<Long> findMemberIdsWithAtLeastWeeklyReportsInMonth(Report.ReportType type, LocalDateTime start, LocalDateTime end, long count);
}
