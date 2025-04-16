package com.springboot.monthlyreport.repository;

import com.springboot.monthlyreport.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
}
