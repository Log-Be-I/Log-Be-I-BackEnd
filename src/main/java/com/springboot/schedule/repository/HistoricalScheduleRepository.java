package com.springboot.schedule.repository;

import com.springboot.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import com.springboot.schedule.entity.HistoricalSchedule;

import java.util.Optional;

public interface HistoricalScheduleRepository extends JpaRepository<HistoricalSchedule, Long> {
    Optional<Schedule> findVByMemberId(long memberId);
}
