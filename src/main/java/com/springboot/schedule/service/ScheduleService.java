package com.springboot.schedule.service;

import com.google.api.client.util.DateTime;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.redis.RedisService;
import com.springboot.schedule.dto.GoogleEventDto;
import com.springboot.schedule.entity.HistoricalSchedule;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.repository.HistoricalScheduleRepository;
import com.springboot.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final HistoricalScheduleRepository historicalScheduleRepository;
    private final RedisService redisService;
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

    // 구글 캘린더
    public void sendEventToGoogleCalendar(GoogleEventDto dto) {
        try {
            // 서버에 저장된 accessToken (실제 환경에서는 DB, Redis, 혹은 사용자별 저장소에서 가져와야 함)
            String accessToken = redisService.getGoogleAccessToken(dto.getCalendarId());

            // 인증 설정
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

            // 구글 Calendar 클라이언트 생성
            Calendar calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("LogBeI").build();

            // 이벤트 객체 생성
            Event event = new Event()
                    .setSummary(dto.getSummary())
                    .setDescription(dto.getDescription())
                    .setLocation(dto.getLocation());

            // 날짜 설정
            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(dto.getStartDateTime()))
                    .setTimeZone("Asia/Seoul");

            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(dto.getEndDateTime()))
                    .setTimeZone("Asia/Seoul");

            event.setStart(start);
            event.setEnd(end);

            // 캘린더 ID 설정 (없으면 기본값 "primary" 사용)
//            String calendarId = dto.getCalendarId() != null ? dto.getCalendarId() : "primary";
            String calendarId = "primary"; // 고정
            // 구글 캘린더에 이벤트 삽입
            calendar.events().insert(calendarId, event).execute();

        } catch (Exception e) {
            throw new RuntimeException("Google Calendar 이벤트 등록 실패: " + e.getMessage(), e);
        }
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
