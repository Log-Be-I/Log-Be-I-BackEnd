package com.springboot.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardResponseDto {
    private int todayMemberCount;
    private List<String> todayMemberNames;

    private int unansweredQuestionCount;
    private List<UnansweredQuestion> questionTitles;

    private List<RecentNotice> recentNotices;

    @Getter
    @AllArgsConstructor
    public static class RecentNotice {
        private String title;
        private LocalDateTime createdAt;
    }

    @Getter
    @AllArgsConstructor
    public static class UnansweredQuestion {
        private String title;
    }
}
