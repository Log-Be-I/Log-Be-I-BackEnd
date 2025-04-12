package com.springboot.notice.dto;


import com.springboot.notice.entity.Notice;
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
        private String title;

        @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
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
        private String content;

        private String image;
        private Notice.NoticeType noticeType;
        private Notice.NoticeStatus noticeStatus;
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
