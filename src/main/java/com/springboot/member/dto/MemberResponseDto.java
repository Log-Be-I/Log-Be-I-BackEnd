package com.springboot.member.dto;

import com.springboot.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@Setter
public class MemberResponseDto {

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

}
