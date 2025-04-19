package com.springboot.monthlyreport.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @Column(nullable = false, unique = false)
    private LocalDate yearMonth;    //2025-04-01 로 등록

    @ManyToOne
    @JoinColumn(name = "member_id")
    @JsonBackReference
    private Member member;

    @OneToMany(mappedBy = "monthlyReport", cascade = CascadeType.PERSIST)
    @JsonManagedReference
    private List<Report> reports = new ArrayList<>();


    //monthly 처음 생성 기준으로 1번만 발생 (연도별 조회시 필요)
    @PrePersist
    public void setDefaultYearMonth() {
        if (this.yearMonth == null) {
            this.yearMonth = LocalDate.now(); // 생성 시점에만 한 번
        }
    }
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

////     report 영속성
//    public void setReport(Report report) {
//        if(report.getMonthlyReport() != this) {
//            report.setMonthlyReport(this);
//        }
//        if(!this.getReports().contains(report)) {
//            report.setMonthlyReport(this);
//        }
//    }

}
