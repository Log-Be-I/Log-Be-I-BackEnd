package com.springboot.notice.dto;

import com.springboot.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticePatchDto {
    private long noticeId;

    @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
    private String title;

    @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
    @Schema(description = "공지사항 내용", example = "LOG BE I 이용 방법")
    private String content;

    private String image;

    @Schema(description = "공지 글 타입", example = "NOTICE")
    private Notice.NoticeType noticeType;

    @Schema(description = "공지사항 고정여부", example = "PINNED")
    private Notice.IsPinned isPinned;
}
