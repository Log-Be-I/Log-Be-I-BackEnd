package com.springboot.report.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.Map;

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

    @Column(nullable = false)
    private String monthlyTitle;

    @Enumerated(value = EnumType.STRING)
    private ReportType reportType = ReportType.REPORT_WEEKLY;

    //월간(0), 주차(1~5)
    @Column(nullable = true)
    private int periodNumber;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public enum ReportType {
        REPORT_WEEKLY("주간 분석 정보"),
        REPORT_MONTHLY("월간 분석 정보");

        @Getter
        private String status;

        ReportType(String status) {
            this.status = status;
        }
    }

}
