package com.springboot.googleCalendar.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.log.LogStorageService;
import com.springboot.member.service.MemberService;
import com.springboot.redis.RedisService;
import com.springboot.googleCalendar.dto.GoogleEventDto;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.mapper.ScheduleMapper;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.temporal.ChronoUnit;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GoogleCalendarService {

    private final RedisService redisService;
    private final ScheduleService scheduleService;
    private final ScheduleMapper scheduleMapper;
    private final MemberService memberService;
    private final ScheduleRepository scheduleRepository;
    private final LogStorageService logStorageService;
    String logName = "Google_Calendar";
    // 구글 캘린더 등록 요청
    public Event sendEventToGoogleCalendar(GoogleEventDto dto) {
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
            String calendarId = "primary"; // 고정
            // 구글 캘린더에 이벤트 삽입
            return calendar.events().insert(calendarId, event).execute();

        } catch (Exception e) {
            logStorageService.logAndStoreWithError("Google Calendar schedule post failed: {}", logName, e.getMessage(), e);
            throw new RuntimeException("Google Calendar 이벤트 등록 실패: " + e.getMessage(), e);
        }
    }

    // 구글 캘린더 조회 요청 (월 기준 조회)
    public List<Event> getEventsFromGoogleCalendar(String timeMin, String timeMax, CustomPrincipal customPrincipal) {
        try {

            // 엑세스 토큰 받아오기
            String accessToken = redisService.getGoogleAccessToken(customPrincipal.getEmail());
            // 받은 accessToken 으로 GoogleCredential 객체 생성
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

            // calendar 객체 생성 (구글 캘린더와 연결해주는 객체 음,. 구글 db?)
            Calendar calendar = new Calendar.Builder(
                    // http 전송을 위한 기본 트랜스포트 객체 생성 ( 서버랑 통신할 때 어떤 방식 데이터를 보낼지 정해주는 도구 )
                    GoogleNetHttpTransport.newTrustedTransport(),
                    // json 처리를 위한 Jackson 라이브러리 사용
                    JacksonFactory.getDefaultInstance(),
                    // 사용자 인증 정보
                    credential
            ).setApplicationName("LogBeI").build();

            // event 객체 생성
            Events events = calendar.events().list("primary")
                    // 여기부터
                    .setTimeMin(new DateTime(timeMin)) // e.g., "2025-04-01T00:00:00+09:00"
                    // 여기까지
                    .setTimeMax(new DateTime(timeMax)) // e.g., "2025-04-30T23:59:59+09:00"
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems();

        } catch (Exception e) {
            logStorageService.logAndStoreWithError("Google Calendar schedule get failed: {}",logName , e.getMessage(), e);
            throw new RuntimeException("Google Calendar 일정 조회 실패: " + e.getMessage(), e);
        }
    }

    public String getStartOfMonth(int year, int month) {
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        return startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public String getEndOfMonth(int year, int month) {
        LocalDateTime endOfMonth = YearMonth.of(year, month)
                .atEndOfMonth()
                .atTime(23, 59, 59);
        return endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public String getEndOfDay(int year, int month, int day) {
        LocalDateTime endOfDay = LocalDate.of(year, month, day).atTime(23, 59, 59);
        return endOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
    public String getStartOfDay(int year, int month, int day) {
        LocalDateTime startOfDay = LocalDate.of(year, month, day).atTime(00, 00, 00);
        return startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public String updateGoogleCalendarEvent(Schedule schedule) {
        try {
            String calendarId = schedule.getMember().getEmail();
            // accessToken 가져오기
            String accessToken = redisService.getGoogleAccessToken(calendarId);
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

            Calendar calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("LogBeI").build();

            // 기존 이벤트 불러오기
            Event event = calendar.events().get(calendarId, schedule.getEventId()).execute();

            // 이벤트 정보 수정
            event.setSummary(schedule.getTitle());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(schedule.getStartDateTime()))
                    .setTimeZone("Asia/Seoul");
            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(schedule.getEndDateTime()))
                    .setTimeZone("Asia/Seoul");

            event.setStart(start);
            event.setEnd(end);

            // 수정된 이벤트 저장
            Event updatedEvent = calendar.events().update(calendarId, event.getId(), event).execute();

            // 수정된 시간 반환
            return updatedEvent.getUpdated().toStringRfc3339();

        } catch (Exception e) {
            logStorageService.logAndStoreWithError("Google Calendar schedule update failed: {}", logName, e.getMessage(), e);
            throw new RuntimeException("Google Calendar 이벤트 수정 실패: " + e.getMessage(), e);
        }
    }

    public List<Schedule> syncSchedulesWithGoogleCalendar(int year, int month, CustomPrincipal principal) throws GeneralSecurityException, IOException {
        // ✅ 1. 서버(DB)에서 해당 연/월의 일정 목록 조회
        List<Schedule> serverScheduleList = scheduleService.findSchedules(year, month, principal);

        // ✅ 2. 서버 일정들을 eventId 기준으로 Map으로 변환
        Map<String, Schedule> serverScheduleMap = serverScheduleList.stream()
                .filter(schedule -> schedule.getEventId() != null &&
                        schedule.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED)
                .collect(Collectors.toMap(Schedule::getEventId, schedule -> schedule));

        // ✅ 3. 해당 월의 시작과 끝 날짜 구하기
        // /main 인 경우 임의값 설정하여 이럴때 하루 일정 조회로 변경되게 작성
//        String timeMin = getStartOfMonth(year, month);
//        String timeMax = getEndOfMonth(year, month);
        List<String> time = dayOrMonthTime(year, month);
        String timeMin = time.get(0);
        String timeMax = time.get(1);

        // ✅ 4. 구글 캘린더에서 일정 가져오기
        List<Event> googleList = getEventsFromGoogleCalendar(timeMin, timeMax, principal);

        // ✅ 5. 구글에만 있는 일정 → DB에 새로 저장
        List<Schedule> schedulesToSave = googleList.stream()
                .filter(google -> !serverScheduleMap.containsKey(google.getId()))
                .map(google -> {
                    // 기존 동일 eventId를 가진 스케줄이 DB에 있는 경우 삭제
                    scheduleRepository.findByEventId(google.getId())
                            .ifPresent(scheduleRepository::delete);

                    Schedule schedule = eventToSchedule(principal, google);
                    LocalDateTime googleUpdated = toLocalDateTime(google.getUpdated()).truncatedTo(ChronoUnit.SECONDS);
                    schedule.setModifiedAt(googleUpdated);
                    return schedule;
                })
                .collect(Collectors.toList());

        schedulesToSave.forEach(schedule -> scheduleService.postTextSchedule(schedule, principal));

        // ✅ 6. 동기화 이후 다시 서버 일정 조회
        List<Schedule> updatedServerList = scheduleService.findSchedules(year, month, principal);

        // ✅ 7. 다시 Map으로 변환
        Map<String, Schedule> serverMap = updatedServerList.stream()
                .filter(s -> s.getEventId() != null && s.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED)
                .collect(Collectors.toMap(
                    Schedule::getEventId,
                    Function.identity(),
                    (existing, duplicate) -> existing  // 중복 발생 시 기존 값을 유지
                ));

        // ✅ 8. 구글과 서버 일정 비교
        for (Event google : googleList) {
            String eventId = google.getId();
            if (!serverMap.containsKey(eventId)) continue;

            Schedule server = serverMap.get(eventId);
            LocalDateTime googleTime = toLocalDateTime(google.getUpdated()).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime serverTime = server.getModifiedAt().truncatedTo(ChronoUnit.SECONDS);

            if (Duration.between(serverTime, googleTime).getSeconds() > 3) {
                // 구글이 최신 → 서버 업데이트
                scheduleService.updateSchedule(server.getScheduleId(), principal);
//                scheduleRepository.delete(server);
            } else if (googleTime.isBefore(serverTime)) {
                // 서버가 최신 → 구글 업데이트
                updateGoogleCalendarEvent(server);
            }
        }

        // ✅ 9. 삭제된 일정 동기화: 서버에는 있지만 구글에 없는 일정 제거
        Set<String> googleEventIds = googleList.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        List<Schedule> schedulesToDelete = updatedServerList.stream()
                .filter(schedule -> schedule.getEventId() != null && !googleEventIds.contains(schedule.getEventId()))
                .collect(Collectors.toList());

//        schedulesToDelete.forEach(schedule -> scheduleRepository.delete(schedule));

        schedulesToDelete.forEach(schedule -> {
            schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_DELETED);
            scheduleRepository.save(schedule);
        });
        // ✅ 10. 최종 반영된 서버 일정 반환
        return scheduleService.findSchedules(year, month, principal);
    }

    public Schedule eventToSchedule(CustomPrincipal principal, Event event) {
        Schedule schedule = new Schedule();
        schedule.setMember(memberService.validateExistingMember(principal.getMemberId()));
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_REGISTERED);
        schedule.setTitle(event.getSummary());
        schedule.setEventId(event.getId());
        schedule.setStartDateTime(event.getStart().getDateTime().toString());
        schedule.setEndDateTime(event.getEnd().getDateTime().toString());
        return schedule;
    }

    public LocalDateTime toLocalDateTime(DateTime dateTime) {
        return Instant.ofEpochMilli(dateTime.getValue())
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
    }

    public List<String> dayOrMonthTime(int year, int month) {
        if(year == 0 || month == 0){
            int newYear = LocalDateTime.now().getYear();
            int newMonth = LocalDateTime.now().getMonth().getValue();
            int day = LocalDateTime.now().getDayOfMonth();
            String timeMin = getStartOfDay(newYear, newMonth, day);
            String timeMax = getEndOfDay(newYear, newMonth, day);
            List<String> timeList = List.of(timeMin, timeMax);
            return timeList;
        } else {
            String timeMin = getStartOfMonth(year, month);
            String timeMax = getEndOfMonth(year, month);
            List<String> timeList = List.of(timeMin, timeMax);
            return timeList;
        }
    }

    // 삭제 요청
    public void deleteGoogleCalendarEvent(String eventId, String email) {
        try {
            String calendarId = email;
            // accessToken 가져오기
            String accessToken = redisService.getGoogleAccessToken(calendarId);
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

            Calendar calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("LogBeI").build();

            // 기존 이벤트 불러오기
            Event event = calendar.events().get(calendarId, eventId).execute();

            // 이벤트 삭제
            calendar.events().delete(calendarId, event.getId()).execute();

        } catch (Exception e) {
            logStorageService.logAndStoreWithError("Google Calendar schedule delete failed: {}", logName, e.getMessage(), e);
            throw new RuntimeException("Google Calendar 이벤트 삭제 실패: " + e.getMessage(), e);
        }
    }


}