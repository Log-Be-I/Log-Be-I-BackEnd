package com.springboot.monthlyreport.repository;

import com.springboot.monthlyreport.entity.MonthlyReport;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    //memberId, LocalDate 로 찾는 메서드
    Optional<MonthlyReport> findByMember_MemberIdAndYearMonth(long memberId, LocalDate yearMonth);
      // 연도와 memberId로 조회, yearMonth 내림차순 정렬
    @Query("SELECT m FROM MonthlyReport m WHERE m.member.id = :memberId AND YEAR(m.yearMonth) = :year ORDER BY m.yearMonth DESC")
    List<MonthlyReport> findByMember_IdAndYearOrderByYearMonthDesc(@Param("memberId") Long memberId, @Param("year") int year);

}
