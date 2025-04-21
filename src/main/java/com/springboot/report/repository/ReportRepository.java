package com.springboot.report.repository;

import com.springboot.report.entity.Report;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {
    //특정 타입, 연도, 월에 해당하는 Report 수 반환
    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportType = :type AND r.title LIKE :titlePrefix AND r.title LIKE %:weekKeyword%")
    int countWeeklyReportsByTitle(@Param("type") Report.ReportType type, @Param("titlePrefix") String titlePrefix, @Param("weekKeyword") String weekKeyword);

}
