package com.springboot.schedule.repository;

import com.springboot.keyword.entity.Keyword;
import com.springboot.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByMember_MemberId (long memberId);
}
