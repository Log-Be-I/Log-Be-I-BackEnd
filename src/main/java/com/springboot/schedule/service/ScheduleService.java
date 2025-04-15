package com.springboot.schedule.service;

import com.springboot.auth.utils.MemberDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.schedule.entity.HistoricalSchedule;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.repository.HistoricalScheduleRepository;
import com.springboot.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final HistoricalScheduleRepository historicalScheduleRepository;

    // 일정 등록 - 음성


    // 일정 등록 - text
    public void postTextSchedule (Schedule schedule, MemberDetails memberDetails) {
        schedule.setMember(memberDetails);
        // 일정 저장
        scheduleRepository.save(schedule);
    }

    // 일정 조회_단일
    public Schedule findSchedule (long scheduleId) {
        // 일정 찾기
        Optional<Schedule> findSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule = findSchedule.orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));

        validateScheduleStatus(schedule);

        return schedule;
    }

    // 일정 조회_전체
    public Page<Schedule> findSchedules (int page, int size) {
        // 페이지네이션 양식 생성
        Pageable pageable =  PageRequest.of(page, size);

        Page<Schedule> schedule = scheduleRepository.findAll(pageable);

        return schedule;
    }

    // 일정 수정
    public void updateSchedule (long scheduleId, MemberDetails memberDetails, Schedule schedule) {
        // 일정 찾기
        Schedule findSchedule = validateExistingSchedule(scheduleId);
        // 데이터 수정
        if(Objects.equals(memberDetails.getEmail(), findSchedule.getMember().getEmail())){
            // 데이터 이관
            HistoricalSchedule historicalSchedule = new HistoricalSchedule();
            historicalSchedule.setScheduleStatus(HistoricalSchedule.ScheduleStatus.SCHEDULE_UPDATED);
            historicalSchedule.setMemberId(memberDetails.getMemberId());
            historicalSchedule.setEndDateTime(findSchedule.getEndDateTime());
            historicalSchedule.setStartDateTime(findSchedule.getStartDateTime());
            historicalSchedule.setOriginalScheduleId(findSchedule.getScheduleId());
            historicalSchedule.setTitle(findSchedule.getTitle());

            findSchedule.setTitle(
                    Optional.ofNullable(schedule.getTitle())
                            .orElse(findSchedule.getTitle()));
            findSchedule.setEndDateTime(
                    Optional.ofNullable(schedule.getEndDateTime())
                            .orElse(findSchedule.getEndDateTime()));
            findSchedule.setStartDateTime(
                    Optional.ofNullable(schedule.getStartDateTime())
                            .orElse(findSchedule.getStartDateTime()));
            findSchedule.setScheduleStatus(
                    Optional.ofNullable(schedule.getScheduleStatus())
                            .orElse(findSchedule.getScheduleStatus()));

            // 이관 완료
            historicalScheduleRepository.save(historicalSchedule);
        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }
        // 수정 데이터 등록
        scheduleRepository.save(findSchedule);
    }

    // 일정 삭제
    public void deletedSchedule (long scheduleId) {
        // scheduleId 로 schedule 찾기
        Optional<Schedule> findSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule = findSchedule.orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));
        // schedule 삭제 상태로 변경
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_DELETED);
        // 상태 변경한 schedule 저장
        scheduleRepository.save(schedule);
    }

    // 존재하는 일정인지 확인
    public Schedule validateExistingSchedule(long scheduleId) {
        Optional<Schedule> findSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule = findSchedule.orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));
        if(schedule.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED) {
            return schedule;
        }
        return null;
    }

    public void validateScheduleStatus (Schedule schedule) {
        if (schedule.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED) {
            new BusinessLogicException(ExceptionCode.NOT_FOUND);
        }
    }
}
