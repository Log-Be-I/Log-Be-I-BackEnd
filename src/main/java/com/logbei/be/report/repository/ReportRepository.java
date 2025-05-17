package com.logbei.be.report.repository;

import com.logbei.be.report.entity.Report;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ReportRepository extends JpaRepository<Report, Long> {
    //특정 타입, 연도, 월에 해당하는 Report 수 반환
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportType = :type AND r.title LIKE :titlePrefix AND r.title LIKE %:weekKeyword%")
    int countWeeklyReportsByTitle(@Param("type") Report.ReportType type, @Param("titlePrefix") String titlePrefix, @Param("weekKeyword") String weekKeyword);
    //특정회원의 Report 연도별 전체조회
    List<Report> findByMember_MemberIdAndMonthlyTitleStartingWith(Long memberId, String monthlyTitlePrefix);
    //특정회원의 특정 년/월의 Report 조회
    List<Report> findByMember_MemberIdAndMonthlyTitle(Long memberId, String monthlyTitle);

}
