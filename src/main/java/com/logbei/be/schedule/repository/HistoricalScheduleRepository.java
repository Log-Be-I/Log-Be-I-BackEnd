package com.logbei.be.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.logbei.be.schedule.entity.HistoricalSchedule;

public interface HistoricalScheduleRepository extends JpaRepository<HistoricalSchedule, Long> {
}
