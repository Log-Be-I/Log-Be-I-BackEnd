package com.springboot.report.repository;

import com.springboot.report.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {
}
