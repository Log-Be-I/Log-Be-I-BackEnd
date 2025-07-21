package com.logbei.be.schedule.repository;

import com.logbei.be.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByMember_MemberId (long memberId);
}
