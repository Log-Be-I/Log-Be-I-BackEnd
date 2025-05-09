package com.springboot.notice.dto;

import com.springboot.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class NoticeResponseDto {
    private long noticeId;
    private String title;
    private String content;
    private List<String> fileUrls;
    private Long memberId;
    private Notice.NoticeType noticeType;
    private Notice.NoticeStatus noticeStatus;
    private Notice.IsPinned isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
