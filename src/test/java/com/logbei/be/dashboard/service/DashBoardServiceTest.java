package com.logbei.be.dashboard.service;

import com.logbei.be.dashboard.dto.DashBoardResponseDto;
import com.logbei.be.member.service.MemberService;
import com.logbei.be.notice.service.NoticeService;
import com.logbei.be.question.service.QuestionService;
import com.springboot.dashboard.dto.UnansweredQuestion;
import com.springboot.dashboard.dto.RecentNotice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashBoardServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private QuestionService questionService;

    @Mock
    private NoticeService noticeService;

    @InjectMocks
    private DashBoardService dashBoardService;

    @Test
    void findVerifiedExistsDashBoard_returnsCorrectData() {
        // given
        List<String> mockMemberNames = List.of("김코딩", "이자바");
        List<UnansweredQuestion> mockQuestions = List.of(
                new UnansweredQuestion( "답변 안된 질문 1"),
                new UnansweredQuestion("답변 안된 질문 2")
        );
        List<RecentNotice> mockNotices = List.of(
                new RecentNotice("공지사항 1", LocalDateTime.now().minusDays(1)),
                new RecentNotice("공지사항 2", LocalDateTime.now())
        );

        when(memberService.findTodayRegisteredMembers()).thenReturn(mockMemberNames);
        when(questionService.findUnansweredQuestions()).thenReturn(mockQuestions);
        when(noticeService.findTop5RecentNotices()).thenReturn(mockNotices);

        // when
        DashBoardResponseDto result = dashBoardService.findVerifiedExistsDashBoard();

        // then
        assertEquals(2, result.getTodayMemberCount());
        assertEquals(List.of("김코딩", "이자바"), result.getTodayMemberNames());
        assertEquals(2, result.getUnansweredQuestionCount());
        assertEquals(mockQuestions, result.getQuestionTitles());
        assertEquals(mockNotices, result.getRecentNotices());
    }
}