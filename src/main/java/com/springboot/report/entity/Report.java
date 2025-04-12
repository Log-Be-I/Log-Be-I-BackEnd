package com.springboot.report.entity;

import com.springboot.audit.BaseEntity;
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
    private ReportType reportType = ReportType.REPORT_WEEK_1;

    @ManyToOne
    @JoinColumn(name = "monthly_id")
    private MonthlyReport monthlyReport;

    public enum ReportType {
        REPORT_WEEK_1("1주차"),
        REPORT_WEEK_2("2주차"),
        REPORT_WEEK_3("3주차"),
        REPORT_WEEK_4("4주차"),
        REPORT_WEEK_5("5주차"),
        REPORT_MONTH("월간");

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
