package com.springboot.report.repository;

import com.springboot.report.entity.Report;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    //특정 타입, 연도, 월에 해당하는 Report 수 반환
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportType = :type AND r.title LIKE :titlePrefix AND r.title LIKE %:weekKeyword%")
    int countWeeklyReportsByTitle(@Param("type") Report.ReportType type, @Param("titlePrefix") String titlePrefix, @Param("weekKeyword") String weekKeyword);


//    Optional<Report> findByMember_MemberIdAndTitle(long memberId, String title);
//    //memberId, LocalDate 로 찾는 메서드
//    Optional<Report> findByMember_MemberIdAndYearMonth(long memberId, LocalDate yearMonth);
//    // 연도와 memberId로 조회, yearMonth 내림차순 정렬
//    @Query("SELECT m FROM MonthlyReport m WHERE m.member.id = :memberId AND YEAR(m.yearMonth) = :year ORDER BY m.yearMonth DESC")
//    List<Report> findByMember_MemberIdAndYearOrderByYearMonthDesc(@Param("memberId") Long memberId, @Param("year") int year);

}
