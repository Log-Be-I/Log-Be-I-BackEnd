package com.springboot.monthlyreport.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
import com.springboot.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long monthlyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate yearMonth;    //2025-04-01 로 등록 -> 연/월만 기록 (검증)

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "monthlyReport", cascade = CascadeType.PERSIST)
    private List<Report> reports = new ArrayList<>();


    // member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getMonthlyReports().contains(this)) {
            member.setMonthlyReport(this);
        }
    }

    //report 영속성 -> MonthlyReport에서만 관리
    public void addReport(Report report){
        this.reports.add(report);
        report.setMonthlyReport(this);
    }

//     report 영속성
//    public void setReport(Report report) {
//        if(report.getMonthlyReport() != this) {
//            report.setMonthlyReport(this);
//        }
//        if(!this.getReports().contains(report)) {
//            report.setMonthlyReport(this);
//        }
//    }

}
