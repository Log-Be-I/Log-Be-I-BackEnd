package com.springboot.monthlyreport.repository;

import com.springboot.monthlyreport.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
    //memberId, LocalDate 로 찾는 메서드
    Optional<MonthlyReport> findByMemberIdAndYearMonth(long memberId, LocalDate yearMonth);
    //memberId, title로 찾는 메서드
    Optional<MonthlyReport> findByMemberIdAndTitle(long memberId, String title);
    //회원이 가지고있는 monthlyReport 반환
    List<MonthlyReport> findByMemberId(long memberId);
}
