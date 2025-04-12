package com.springboot.report.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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

    // report 영속성
    public void setReport(Report report) {
        if(report.getMonthlyReport() != this) {
            report.setMonthlyReport(this);
        }
        if(!this.getReports().contains(report)) {
            report.setMonthlyReport(this);
        }
    }
}
