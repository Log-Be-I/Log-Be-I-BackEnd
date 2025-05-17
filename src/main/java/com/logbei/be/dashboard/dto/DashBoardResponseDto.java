package com.logbei.be.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardResponseDto {
    @Schema(description = "오늘 가입한 회원 수", example = "20")
    private int todayMemberCount;

    @Schema(description = "오늘 가입한 회원 명단", example = "[{\"name\": \"기로기\"}]")
    private List<String> todayMemberNames;

    @Schema(description = "답변이 없는 질문글 개수", example = "20")
    private int unansweredQuestionCount;

    @Schema(description = "답변이 없는 질문글 제목 리스트", example = "[{\"title\": \"회원가입 버튼 클릭이 안됩니다.\"}]")
    private List<UnansweredQuestion> questionTitles;

    @Schema(description = "최근 공지 리스트",
            example = "[{\"title\": \"회원가입 버튼 클릭이 안됩니다.\", \"createdAt\": \"2025-04-11T11:30\"}]")
    private List<RecentNotice> recentNotices;
}
