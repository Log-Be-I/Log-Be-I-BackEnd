package com.springboot.schedule.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.springboot.ai.clova.ClovaSpeechService;
import com.springboot.auth.utils.CustomPrincipal;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.record.repository.RecordRepository;
import com.springboot.redis.RedisService;
import com.springboot.schedule.entity.HistoricalSchedule;
import com.springboot.schedule.entity.Schedule;
import com.springboot.schedule.repository.HistoricalScheduleRepository;
import com.springboot.schedule.repository.ScheduleRepository;
import com.springboot.utils.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final HistoricalScheduleRepository historicalScheduleRepository;
    private final MemberService memberService;

    // 일정 등록 - text
    public Schedule createTextSchedule(Schedule schedule, Long memberId) {
        // 가입된 회원인지 검증
        Member member = memberService.findVerifiedExistsMember(memberId);
        //활동 중인 상태인지 검증
        memberService.validateMemberStatus(member);
        schedule.setMember(member);
        // 일정 저장
        return scheduleRepository.save(schedule);
    }

    // 일정 수정( 서버 수정 )
    // 수정을 원하는 일정 Id, 유저 기본 정보, 원하는 변경 사항을 파라미터로 받는다
    @Transactional
    public Schedule updateSchedule(Schedule schedule, long scheduleId, Long memberId) {
        // 가입된 회원인지 검증
        Member member = memberService.findVerifiedExistsMember(memberId);
        //활동 중인 상태인지 검증
        memberService.validateMemberStatus(member);
        // 등록된 일정 찾기
        Schedule findSchedule = findVeryfiedExistsSchedule(scheduleId);

        // 데이터 수정
        if (Objects.equals(memberId, findSchedule.getMember().getMemberId())) {
            // 데이터 이관
            HistoricalSchedule historicalSchedule = new HistoricalSchedule();
            historicalSchedule.setScheduleStatus(HistoricalSchedule.ScheduleStatus.SCHEDULE_UPDATED);
            historicalSchedule.setMemberId(memberId);
            historicalSchedule.setEndDateTime(findSchedule.getEndDateTime());
            historicalSchedule.setStartDateTime(findSchedule.getStartDateTime());
            historicalSchedule.setOriginalScheduleId(findSchedule.getScheduleId());
            historicalSchedule.setTitle(findSchedule.getTitle());

            // 이관 완료
            historicalScheduleRepository.save(historicalSchedule);

            // 데이터 이관 이후 항상 하던데로 데이터 변경 시작
            findSchedule.setTitle(
                    Optional.ofNullable(schedule.getTitle())
                            .orElse(findSchedule.getTitle()));
            findSchedule.setEndDateTime(
                    Optional.ofNullable(schedule.getEndDateTime())
                            .orElse(findSchedule.getEndDateTime()));
            findSchedule.setStartDateTime(
                    Optional.ofNullable(schedule.getStartDateTime())
                            .orElse(findSchedule.getStartDateTime()));
            // 수정 끝난 일정 데이터 등록
            return scheduleRepository.save(findSchedule);

        } else {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }
    }
    // 일정 조회_단일
    public Schedule findSchedule(long scheduleId, long memberId) {
        // 가입된 회원인지 검증
        Member member = memberService.findVerifiedExistsMember(memberId);
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);
        Schedule findSchedule = findVeryfiedExistsSchedule(scheduleId);
        //관리자 또는 작성자 본인인지 확인
        AuthorizationUtils.isAdminOrOwner(findSchedule.getMember().getMemberId(), memberId);
        // 일정 찾기
        return findSchedule;
    }

    // 일정 조회_전체 (페이지네이션 필요 x) 한달 일정 전체를 리스트로 줘야함
    public List<Schedule> findSchedules(int year, int month, Long memberId) {
        // member 검증
        Member member = memberService.findVerifiedExistsMember(memberId);
        //정상적인 상태인지 검증
        memberService.validateMemberStatus(member);

        //String target = String.format("%d-%02d", year, month);
        List<String> date = dayOrMonthGetDate(year, month);
        //String target = date.get(0);
        // targetStart = 오늘 일 or 월 할당
        LocalDateTime targetStart = LocalDateTime.parse(date.get(1));
        // targetEnd = 시작 시간 할당
        LocalDateTime targetEnd = LocalDateTime.parse(date.get(2));

        // memberId 로 일정 찾기
        List<Schedule> scheduleList = scheduleRepository.findAllByMember_MemberId(memberId);

        // "year" 과 "month" 가 포함된 모든 일정 조회
        List<Schedule> findScheduleList = scheduleList.stream()
                // 스케쥴의 상태가삭제 상태가 아닌것만 필터링
                .filter(schedule -> schedule.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED)
                .filter(schedule ->
                        // 일정의 시작 시간 이전을 파싱
                        !schedule.getEndDateTime().isBefore(targetStart) &&
                                !schedule.getStartDateTime().isAfter(targetEnd)
                )
                .collect(Collectors.toList());
        // 최종 데이터 반영된 findScheduleList 리턴
        return findScheduleList;
    }

    //main -> 오늘 날짜 기준 // 하루 일정일 경우 -> 하루 시작부터 마지막 시간 반환 // 월 일정일 경우 -> 월 시작부터 월 마지막 시간 반환
    public List<String> dayOrMonthGetDate(int year, int month) {
        // year 과 month 를 강제로 0 으로 할당 (FE가 주는 정보는 하나도 없음)
        // 0 이 들어왔다는건 /main 조회한다는것
        if (year == 0 || month == 0) {
            // 새로운 year 객체 생성 현재 시간 기준 yyyy
            int newYear = LocalDateTime.now().getYear();
            // 새로운 month 객체 생성 현재 시간 기준 mm
            int newMonth = LocalDateTime.now().getMonth().getValue();
            // 오늘 날짜 생성
            String today = String.format("%d-%02d-%02d", newYear, newMonth, LocalDateTime.now().getDayOfMonth());
            // start = 오늘의 시작 시간
            String start = LocalDate.parse(today).atStartOfDay().toString();
            // end = 오늘의 마지막 시간
            String end = LocalDate.parse(today).atTime(23, 59, 59).toString();
            // 오늘 날짜 (연, 월, 일) start, end List 에 담기
            List<String> date = List.of(today, start, end);
            // 리스트 반환
            return date;
        } else {
            // newMonth = (mm -- dd) 형태로 변환
            String newMonth = String.format("%d-%02d", year, month);
            // targetStart = 입력받은 year 과 Month 로 해당 월 초 1/1 구함
            String targetStart = YearMonth.of(year, month).atDay(1).atStartOfDay().toString();
            // targetEnd = 입력받은 year 과 month 로 해당 월 마지막 시간대 구함;
            String targetEnd = YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59).toString();
            // 새로운 월, 시작 을 list 로 변환
            List<String> date = List.of(newMonth, targetStart, targetEnd);
            return date;
        }
    }

    // 일정 삭제
    public void deletedSchedule(long scheduleId, Long memberId) {
        // 가입된 회원인지 검증
        Member member = memberService.findVerifiedExistsMember(memberId);
        // 정상적인 상태인지 검증
        memberService.validateMemberStatus(member);
        // scheduleId 로 schedule 찾기
        Schedule schedule = findVeryfiedExistsSchedule(scheduleId);
        // schedule 삭제 상태로 변경
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_DELETED);
        // 상태 변경한 schedule 저장
        scheduleRepository.save(schedule);
    }


    // 존재하는 일정인지 확인
    public Schedule findVeryfiedExistsSchedule(long scheduleId) {
        Optional<Schedule> findSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule = findSchedule.orElseThrow(() -> new BusinessLogicException(ExceptionCode.NOT_FOUND));
        if (schedule.getScheduleStatus() != Schedule.ScheduleStatus.SCHEDULE_DELETED) {
            return schedule;
        }
        throw new BusinessLogicException(ExceptionCode.QUESTION_NOT_FOUND);
    }


    // ISO 8601 기본형 포맷 으로 해당 월의 시작 시간 연/월/일 시/분/초 반환
//    public String getStartOfMonth(int year, int month) {
//        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
//        return startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//    }

    // ISO 8601 기본형 포맷 으로 해당 월의 마지막 시간 연/월/일 시/분/초 반환
//    public String getEndOfMonth(int year, int month) {
//        LocalDateTime endOfMonth = YearMonth.of(year, month)
//                .atEndOfMonth()
//                .atTime(23, 59, 59);
//        return endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//    }

    // ISO 8601 기본형 포맷 으로 해당 일의 마지막 시간대 연/월/일 시/분/초 포멧으로 반환
//    public String getEndOfDay(int year, int month, int day) {
//        LocalDateTime endOfDay = LocalDate.of(year, month, day).atTime(23, 59, 59);
//        return endOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//    }

    // ISO 8601 기본형 포맷 으로 해당 일의 시작 시간 연/월/일 시/분/초 반환
//    public String getStartOfDay(int year, int month, int day) {
//        LocalDateTime startOfDay = LocalDate.of(year, month, day).atTime(00, 00, 00);
//        return startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//    }
}