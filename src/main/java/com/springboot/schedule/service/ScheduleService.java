package com.springboot.schedule.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.springboot.auth.utils.CustomAuthorityUtils;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.auth.utils.MemberDetails;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.redis.RedisService;
import com.springboot.schedule.entity.HistoricalSchedule;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.repository.HistoricalScheduleRepository;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final HistoricalScheduleRepository historicalScheduleRepository;
    private final RedisService redisService;
    private final MemberService memberService;

    // 일정 등록 - 음성


    // 일정 등록 - text
    public void postTextSchedule (Schedule schedule, CustomPrincipal customPrincipal) {

        schedule.setMember(memberService.validateExistingMember(customPrincipal.getMemberId()));
        // 일정 저장
        scheduleRepository.save(schedule);
    }

    // 일정 조회_단일
    public Schedule findSchedule (long scheduleId) {
        // 일정 찾기
        return validateExistingSchedule(scheduleId);
    }

    // 일정 조회_전체 (페이지네이션 필요 x) 한달 일정 전체를 리스트로 줘야함
    public List<Schedule> findSchedules (int year, int month, CustomPrincipal customPrincipal) {
        // member 검증
        Member member = memberService.validateExistingMember(customPrincipal.getMemberId());

        //정상적인 상태인지 검증
        memberService.validateMemberStatus(member);
        String target = String.format("%d-%02d", year, month);

        // memberId 로 일정 찾기
        List<Schedule> scheduleList = scheduleRepository.findAllByMember_MemberId(customPrincipal.getMemberId());

        // "year" 과 "month" 가 포함된 모든 일정 조회
        List<Schedule> findScheduleList = scheduleList.stream()
                .filter(schedule -> schedule.getStartDateTime().startsWith(target))
                .filter(schedule -> schedule.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED)
                .collect(Collectors.toList());

        return findScheduleList;
    }

    // 일정 수정( 구글 )
    public void updateSchedule (long scheduleId, CustomPrincipal customPrincipal) throws GeneralSecurityException, IOException {
        // 일정 찾기
        Schedule findSchedule = validateExistingSchedule(scheduleId);

        Event schedule = getEvent(findSchedule.getEventId());

        // 데이터 수정
        if(Objects.equals(customPrincipal.getEmail(), findSchedule.getMember().getEmail())){
            // 데이터 이관
            HistoricalSchedule historicalSchedule = new HistoricalSchedule();
            historicalSchedule.setScheduleStatus(HistoricalSchedule.ScheduleStatus.SCHEDULE_UPDATED);
            historicalSchedule.setMemberId(customPrincipal.getMemberId());
            historicalSchedule.setEndDateTime(findSchedule.getEndDateTime());
            historicalSchedule.setStartDateTime(findSchedule.getStartDateTime());
            historicalSchedule.setOriginalScheduleId(findSchedule.getScheduleId());
            historicalSchedule.setTitle(findSchedule.getTitle());

            findSchedule.setTitle(
                    Optional.ofNullable(schedule.getSummary())
                            .orElse(findSchedule.getTitle()));
            findSchedule.setEndDateTime(
                    Optional.ofNullable(schedule.getEnd().getDateTime().toString())
                            .orElse(findSchedule.getEndDateTime()));
            findSchedule.setStartDateTime(
                    Optional.ofNullable(schedule.getStart().getDateTime().toString())
                            .orElse(findSchedule.getStartDateTime()));

            // 수정 데이터 등록
            scheduleRepository.save(findSchedule);
            // 이관 완료
            historicalScheduleRepository.save(historicalSchedule);
        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }

    }

    // 일정 수정( 서버 수정 )
    public void updateServerSchedule (long scheduleId, CustomPrincipal customPrincipal, Schedule schedule) {
        // 일정 찾기
        Schedule findSchedule = validateExistingSchedule(scheduleId);

        // 데이터 수정
        if(Objects.equals(customPrincipal.getEmail(), findSchedule.getMember().getEmail())){
            // 데이터 이관
            HistoricalSchedule historicalSchedule = new HistoricalSchedule();
            historicalSchedule.setScheduleStatus(HistoricalSchedule.ScheduleStatus.SCHEDULE_UPDATED);
            historicalSchedule.setMemberId(customPrincipal.getMemberId());
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
//            findSchedule.setEventId(schedule.getEventId());

            // 수정 데이터 등록
            scheduleRepository.save(findSchedule);
            // 이관 완료
            historicalScheduleRepository.save(historicalSchedule);
        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }

    }


    // 일정 삭제
    public void deletedSchedule (long scheduleId) {
        // scheduleId 로 schedule 찾기
       Schedule schedule = validateExistingSchedule(scheduleId);
       // 삭제 가능한 상태인지 확인
        validateScheduleStatus(schedule);
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

    // 단일 조회
    public Event getEvent (String eventId) throws GeneralSecurityException, IOException {

        String calendarId = "6feetlife56@gmail.com";
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

        return event;
    }
}
