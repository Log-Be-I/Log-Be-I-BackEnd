package com.springboot.schedule.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.springboot.redis.RedisService;
import com.springboot.schedule.dto.GoogleEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GoogleCalendarService {

    private final RedisService redisService;


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

    // 구글 캘린더 조회 요청
    public List<Event> getEventsFromGoogleCalendar(String timeMin, String timeMax) {
        try {
            String accessToken = redisService.getGoogleAccessToken("primary");
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

            Calendar calendar = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("LogBeI").build();

            Events events = calendar.events().list("primary")
                    .setTimeMin(new DateTime(timeMin)) // e.g., "2025-04-01T00:00:00+09:00"
                    .setTimeMax(new DateTime(timeMax)) // e.g., "2025-04-30T23:59:59+09:00"
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems();

        } catch (Exception e) {
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

}
