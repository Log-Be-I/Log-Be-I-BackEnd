package com.logbei.be.schedule.entity;

import com.logbei.be.audit.BaseEntity;
import com.logbei.be.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Schedule extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(value = EnumType.STRING)
    private ScheduleStatus scheduleStatus = ScheduleStatus.SCHEDULE_REGISTERED;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public enum ScheduleStatus {
        SCHEDULE_REGISTERED("일정 등록"),
        SCHEDULE_UPDATED("일정 수정"),
        SCHEDULE_DELETED("일정 삭제");

        @Getter
        private String status;

        ScheduleStatus(String status) {
            this.status = status;
        }
    }

    // member 영속성
    public void setMember(Member member) {
        this.member = member;
        if(!member.getSchedules().contains(this)) {
            member.setSchedule(this);
        }
    }
}
