package com.springboot.report.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.monthlyreport.entity.MonthlyReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(value = EnumType.STRING)
    private ReportType reportType = ReportType.REPORT_WEEKLY;

    //월간(0), 주차(1~5)
    @Column(nullable = false)
    private int periodNumber;

    @ManyToOne
    @JoinColumn(name = "monthly_id")
    private MonthlyReport monthlyReport;

    public enum ReportType {
        REPORT_WEEKLY("주간 분석 정보"),
        REPORT_MONTH("월간 분석 정보");

        @Getter
        private String status;

        ReportType(String status) {
            this.status = status;
        }
    }

    // monthlyReport 영속성
    public void setMonthlyReport(MonthlyReport monthlyReport) {
        this.monthlyReport = monthlyReport;
        if(monthlyReport.getReports().contains(this)) {
            monthlyReport.setReport(this);
        }
    }
}
