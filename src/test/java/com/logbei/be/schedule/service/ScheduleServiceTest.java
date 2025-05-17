package com.logbei.be.schedule.service;

import com.logbei.be.exception.BusinessLogicException;
import com.logbei.be.exception.ExceptionCode;
import com.logbei.be.member.entity.Member;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.schedule.entity.Schedule;
import com.logbei.be.schedule.repository.HistoricalScheduleRepository;
import com.logbei.be.schedule.repository.ScheduleRepository;
import com.logbei.be.schedule.service.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private HistoricalScheduleRepository historicalScheduleRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private ScheduleService scheduleService;

    public ScheduleServiceTest() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }
    // SecurityContextHolder 안에 “현재 로그인한 유저” 정보를 직접 설정해주는 코드.
    void setMockAuthenticatedUser() {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                "testUser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }



    @BeforeEach
    void setupAuthentication() {
        setMockAuthenticatedUser();
    }

    @Test
    @DisplayName("일정 등록 테스트 - 정상 동작")
    void createTextSchedule_success() {
        // given
        Long memberId = 1L;
        Member mockMember = new Member();
        mockMember.setMemberId(memberId);

        Schedule inputSchedule = new Schedule();
        Schedule savedSchedule = new Schedule();
        savedSchedule.setScheduleId(100L);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(mockMember);
        doNothing().when(memberService).validateMemberStatus(mockMember);
        when(scheduleRepository.save(inputSchedule)).thenReturn(savedSchedule);

        // when
        Schedule result = scheduleService.createTextSchedule(inputSchedule, memberId);

        // then
        assertNotNull(result);
        assertEquals(100L, result.getScheduleId());
        verify(scheduleRepository).save(inputSchedule);
        verify(memberService).findVerifiedExistsMember(memberId);
        verify(memberService).validateMemberStatus(mockMember);
    }

    @Test
    @DisplayName("일정 등록 테스트 - 존재하지 않는 멤버로 예외 발생")
    void createTextSchedule_memberNotFound_throwsException() {
        // given
        Long invalidMemberId = 999L;
        Schedule inputSchedule = new Schedule();

        when(memberService.findVerifiedExistsMember(invalidMemberId))
            .thenThrow(new BusinessLogicException(ExceptionCode.NOT_FOUND));

        // when & then
        BusinessLogicException exception = assertThrows(
            BusinessLogicException.class,
            () -> scheduleService.createTextSchedule(inputSchedule, invalidMemberId)
        );

        assertEquals(ExceptionCode.NOT_FOUND, exception.getExceptionCode());
        verify(memberService).findVerifiedExistsMember(invalidMemberId);
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("일정 등록 테스트 - 일정이 삭제된 상태면 예외 발생")
    void createTextSchedule_scheduleDeleted_throwsException() {
        // given
        Long memberId = 1L;
        Long scheduleId = 100L;

        Member mockMember = new Member();
        mockMember.setMemberId(memberId);

        Schedule deletedSchedule = new Schedule();
        deletedSchedule.setScheduleId(scheduleId);
        deletedSchedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_DELETED);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(mockMember);
        doNothing().when(memberService).validateMemberStatus(mockMember);
        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(deletedSchedule));

        // when & then
        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> scheduleService.findVeryfiedExistsSchedule(scheduleId)
        );

        assertEquals(ExceptionCode.QUESTION_NOT_FOUND, exception.getExceptionCode());
        verify(scheduleRepository).findById(scheduleId);
    }

    @Test
    @DisplayName("일정 단일 조회 테스트 - 정상 조회")
    void findSchedule_success() {
        Long memberId = 1L;
        Long scheduleId = 10L;

        Member member = new Member();
        member.setMemberId(memberId);

        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setMember(member);
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_REGISTERED);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        doNothing().when(memberService).validateMemberStatus(member);
        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(schedule));

        Schedule result = scheduleService.findSchedule(scheduleId, memberId);

        assertNotNull(result);
        assertEquals(scheduleId, result.getScheduleId());
    }

    @Test
    @DisplayName("일정 전체 조회 테스트 - 정상 케이스")
    void findSchedules_success() {
        Long memberId = 1L;
        int year = 2025, month = 5;

        Member member = new Member();
        member.setMemberId(memberId);

        Schedule schedule = new Schedule();
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_REGISTERED);
        schedule.setStartDateTime(java.time.LocalDateTime.of(2025, 5, 10, 9, 0));
        schedule.setEndDateTime(java.time.LocalDateTime.of(2025, 5, 10, 10, 0));

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        doNothing().when(memberService).validateMemberStatus(member);
        when(scheduleRepository.findAllByMember_MemberId(memberId)).thenReturn(java.util.List.of(schedule));

        java.util.List<Schedule> result = scheduleService.findSchedules(year, month, memberId);

        assertEquals(1, result.size());
    }
    @Test
    @DisplayName("일정 단일 조회 테스트 - 관리자 또는 본인이 아닐 경우 예외 발생")
    void findSchedule_notOwnerOrAdmin_throwsException() {
        // given
        Long memberId = 1L;
        Long scheduleId = 5L;

        Member member = new Member();
        member.setMemberId(memberId);

        Member scheduleOwner = new Member();
        scheduleOwner.setMemberId(999L); // 다른 사용자

        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setMember(scheduleOwner);
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_REGISTERED);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        doNothing().when(memberService).validateMemberStatus(member);
        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(schedule));

        // AuthorizationUtils.isAdminOrOwner 예외 발생
        assertThrows(
                BusinessLogicException.class,
                () -> scheduleService.findSchedule(scheduleId, memberId)
        );
    }

    @Test
    @DisplayName("일정 전체 조회 테스트 - 삭제된 일정은 필터링됨")
    void findSchedules_deletedSchedule_filteredOut() {
        // given
        Long memberId = 1L;
        int year = 2025, month = 5;

        Member member = new Member();
        member.setMemberId(memberId);

        Schedule deletedSchedule = new Schedule();
        deletedSchedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_DELETED);
        deletedSchedule.setStartDateTime(java.time.LocalDateTime.of(2025, 5, 1, 10, 0));
        deletedSchedule.setEndDateTime(java.time.LocalDateTime.of(2025, 5, 1, 11, 0));

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        doNothing().when(memberService).validateMemberStatus(member);
        when(scheduleRepository.findAllByMember_MemberId(memberId)).thenReturn(java.util.List.of(deletedSchedule));

        // when
        java.util.List<Schedule> result = scheduleService.findSchedules(year, month, memberId);

        // then
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("일정 삭제 테스트 - 정상 처리")
    void deletedSchedule_success() {
        Long memberId = 1L;
        Long scheduleId = 100L;

        Member member = new Member();
        member.setMemberId(memberId);

        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setScheduleStatus(Schedule.ScheduleStatus.SCHEDULE_REGISTERED);

        when(memberService.findVerifiedExistsMember(memberId)).thenReturn(member);
        doNothing().when(memberService).validateMemberStatus(member);
        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(schedule));

        scheduleService.deletedSchedule(scheduleId, memberId);

        assertEquals(Schedule.ScheduleStatus.SCHEDULE_DELETED, schedule.getScheduleStatus());
        verify(scheduleRepository).save(schedule);
    }
}