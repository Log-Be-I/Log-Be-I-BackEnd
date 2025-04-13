package com.springboot.notice.dto;


import com.springboot.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;


public class NoticeDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
        @Schema(description = "공지사항 제목", example = "LOG BE I")
        private String title;

        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
        @Schema(description = "공지사항 내용", example = "LOG BE I 이용 방법")
        private String content;

        private String image;
        private Notice.NoticeType noticeType;
        private Notice.NoticeStatus noticeStatus;
        private Notice.IsPinned isPinned;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        private long noticeId;

        @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
        private String title;

        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
        @Schema(description = "공지사항 내용", example = "LOG BE I 이용 방법")
        private String content;

        private String image;

        @Schema(description = "공지 글 타입", example = "NOTICE")
        private Notice.NoticeType noticeType;

        @Schema(description = "공지 타입", example = "NOTICE_UPDATED")
        private Notice.NoticeStatus noticeStatus;

        @Schema(description = "공지사항 고정여부", example = "PINNED")
        private Notice.IsPinned isPinned;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private long noticeId;
        private String title;
        private String content;
        private String image;
        private Notice.NoticeType noticeType;
        private Notice.NoticeStatus noticeStatus;
        private Notice.IsPinned isPinned;
    }
}
