package com.springboot.member.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.category.entity.Category;
import com.springboot.keyword.entity.Keyword;
import com.springboot.question.entity.Question;
import com.springboot.record.entity.Record;
import com.springboot.report.entity.MonthlyReport;
import com.springboot.schedule.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String profile;

    @Column(nullable = false)
    private LocalDateTime lastLoginAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean notification;

    @Column
    private String refreshToken;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<MonthlyReport> monthlyReports = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Keyword> keywords = new ArrayList<>();


    public enum MemberStatus {
        MEMBER_ACTIVE("활동 중"),
        MEMBER_SLEEP("휴면 상태"),
        MEMBER_DELETEED("탈퇴 상태");

        @Getter
        private String status;

        MemberStatus(String status) {
            this.status = status;
        }
    }

    // schedule 영속성
    public void setSchedule(Schedule schedule){
        if(schedule.getMember() != this) {
            schedule.setMember(this);
        }
        if (!this.schedules.contains(schedule)) {
            schedules.add(schedule);
        }
    }

    // record 영속성
    public void setRecord(Record record){
        if(record.getMember() != this) {
            record.setMember(this);
        }
        if (!this.records.contains(record)) {
            records.add(record);
        }
    }

    // category 영속성
    public void setCategory(Category category){
        if(category.getMember() != this) {
            category.setMember(this);
        }
        if (!this.categories.contains(category)) {
            categories.add(category);
        }
    }

    // question 영속성
    public void setQuestion(Question question) {
        if(question.getMember() != this) {
            question.setMember(this);
        }
        if(!this.questions.contains(question)) {
            questions.add(question);
        }
    }

    // keyword 영속성
    public void setKeyword(Keyword keyword) {
        if(keyword.getMember() != this) {
            keyword.setMember(this);
        }
        if(!this.keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }

    // monthlyReport 영속성
    public void setMonthlyReport(MonthlyReport monthlyReport) {
        if(monthlyReport.getMember() != this) {
            monthlyReport.setMember(this);
        }
        if(!this.monthlyReports.contains(monthlyReport)) {
            monthlyReports.add(monthlyReport);
        }
    }
}
