package com.logbei.be.member.dto;

import com.logbei.be.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Setter
public class MemberResponseDto {
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "회원 이름", example = "기로기")
    private String name;

    @Schema(description = "닉네임", example = "방울이")
    private String nickname;

    @Schema(description = "이메일", example = "girogi@gmail.com")
    private String email;

    @Schema(description = "지역", example = "세종시")
    private String region;

    @Schema(description = "생일", example = "1999-11-11")
    private String birth;

    @Schema(description = "프로필 사진", example = "url")
    private String profile;

    @Schema(description = "알림 설정", example = "true")
    private Boolean notification;

    @Schema(description = "회원 상태", example = "MEMBER_ACTIVE")
    private Member.MemberStatus memberStatus;

    @Schema(description = "회원의 마지막 로그인 시간", example = "2025-04-17 12:39:22")
    private LocalDateTime lastLoginAt;

    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;
    @Schema(description = "수정일", example = "2025-04-11T11:30")
    private LocalDateTime modifiedAt;

}
