package com.springboot.notice.dto;

import com.springboot.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class NoticeResponseDto {
    @Schema(description = "공지사항 ID", example = "1")
    private long noticeId;
    @Schema(description = "공지사항 제목", example = "LOG BE I")
    private String title;
    @Schema(description = "공지사항 내용", example = "이번 여름 이벤트는 기로기랑 수영장파티 입니다.")
    private String content;

    @Schema(description = "수정할 첨부 이미지 URL 리스트", example = "[\"https://...\", \"https://...\"]")
    private List<String> fileUrls;

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    @Schema(description = "공지사항 종류", example = "EVENT")
    private Notice.NoticeType noticeType;
    @Schema(description = "공지사항 상태", example = "NOTICE_REGISTERED")
    private Notice.NoticeStatus noticeStatus;
    @Schema(description = "공지사항 고정 유무", example = "PINNED")
    private Notice.IsPinned isPinned;
    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;
    @Schema(description = "수정일", example = "2025-04-11T11:30")
    private LocalDateTime modifiedAt;
}
