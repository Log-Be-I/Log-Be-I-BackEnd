package com.springboot.schedule.entity;

import com.springboot.audit.BaseEntity;
import com.springboot.member.entity.Member;
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
public class HistoricalSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hScheduleId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String startDateTime;

    @Column(nullable = false)
    private String endDateTime;

    @Enumerated(value = EnumType.STRING)
    private ScheduleStatus scheduleStatus = ScheduleStatus.SCHEDULE_REGISTERED;

    @Column
    private Long memberId;

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
}
