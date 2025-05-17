package com.logbei.be.dashboard.controller;

import com.logbei.be.dashboard.dto.DashBoardResponseDto;
import com.logbei.be.dashboard.service.DashBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import com.logbei.be.dashboard.dto.UnansweredQuestion;
import java.time.LocalDateTime;
import java.util.List;
import com.logbei.be.dashboard.dto.RecentNotice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashBoardControllerTest {

    @Mock
    private DashBoardService dashBoardService;

    @InjectMocks
    private DashBoardController dashBoardController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDashBoard_returnsResponseEntityWithCorrectData() {
        // given
        List<String> memberNames = List.of("회원A", "회원B");
        List<UnansweredQuestion> questions = List.of(
                new UnansweredQuestion("질문1"),
                new UnansweredQuestion("질문2")
        );
        List<RecentNotice> notices = List.of(
                new RecentNotice("공지1", LocalDateTime.now().minusDays(1)),
                new RecentNotice("공지2", LocalDateTime.now())
        );

        DashBoardResponseDto mockResponse = new DashBoardResponseDto();
        mockResponse.setTodayMemberCount(memberNames.size());
        mockResponse.setTodayMemberNames(memberNames);
        mockResponse.setUnansweredQuestionCount(questions.size());
        mockResponse.setQuestionTitles(questions);
        mockResponse.setRecentNotices(notices);

        when(dashBoardService.findVerifiedExistsDashBoard()).thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = dashBoardController.getDashBoard();

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof DashBoardResponseDto);
        DashBoardResponseDto responseBody = (DashBoardResponseDto) response.getBody();
        assertEquals(2, responseBody.getTodayMemberCount());
        assertEquals("회원A", responseBody.getTodayMemberNames().get(0));
        assertEquals("질문1", responseBody.getQuestionTitles().get(0).getTitle());
        assertEquals("공지1", responseBody.getRecentNotices().get(0).getTitle());

        verify(dashBoardService, times(1)).findVerifiedExistsDashBoard();
    }
}