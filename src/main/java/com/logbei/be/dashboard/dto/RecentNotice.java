package com.logbei.be.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentNotice {
    @Schema(description = "제목", example = "회원가입 버튼 클릭이 안됩니다.")
    private String title;
    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;
}
