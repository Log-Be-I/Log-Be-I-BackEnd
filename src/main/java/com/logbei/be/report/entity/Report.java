package com.logbei.be.report.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.logbei.be.audit.BaseEntity;
import com.logbei.be.member.entity.Member;
import com.logbei.be.utils.ContentMapConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String monthlyTitle;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
//JPA가 알아서 저장 시 JSON String, 조회 시 Map<String, String>으로 바꿔줌
    //Utils -> ContentMapConverte class 정의함
    @Convert(converter = ContentMapConverter.class)
    private Map<String, String> content;


    @Enumerated(value = EnumType.STRING)
    private ReportType reportType = ReportType.REPORT_WEEKLY;

    //월간(0), 주차(1~5)
    @Column(nullable = true)
    private int periodNumber;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonBackReference
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
