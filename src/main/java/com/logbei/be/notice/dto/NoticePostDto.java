package com.logbei.be.notice.dto;

import com.logbei.be.notice.entity.Notice;
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
public class NoticePostDto {
    @NotBlank(message = "공지사항/이벤트의 제목은 필수 입력란입니다.")
    @Schema(description = "공지사항 제목", example = "LOG BE I")
    private String title;

    @NotBlank(message = "공지사항/이벤트의 설명글은 필수 입력란입니다.")
    @Schema(description = "공지사항 내용", example = "LOG BE I 이용 방법")
    private String content;

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "공지사항 종류", example = "EVENT")
    private Notice.NoticeType noticeType;

    @Schema(description = "공지사항 고정 유무", example = "PINNED")
    private Notice.IsPinned isPinned;
}
