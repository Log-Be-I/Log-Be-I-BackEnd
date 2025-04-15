package com.springboot.schedule.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public void deletedSchedule (long scheduleId) {



        // scheduleId 로 schedule 찾기
        Optional<Schedule> findSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule = findSchedule.orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));

        // schedule 삭제 상태로 변경
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_DELETED);

        // 상태 변경한 schedule 저장
        scheduleRepository.save(schedule);
    }
}
