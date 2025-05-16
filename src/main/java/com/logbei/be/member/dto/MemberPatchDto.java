package com.logbei.be.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberPatchDto {

//    private Long memberId;

//    @Schema(description = "회원 이름", example = "기로기")
//    private String name;

    @Schema(description = "닉네임", example = "방울이")
    @Pattern(regexp = "^[가-힣a-z]{2,8}$", message = "한글과 영어 소문자만 사용 가능합니다.")
    private String nickname;

    @Schema(description = "지역", example = "세종시")
    private String region;

    @Schema(description = "생일", example = "1999-11-11")
    private String birth;

    @Schema(description = "프로필 사진", example = "url")
    private String profile;

    @Schema(description = "알림 설정", example = "true")
    private Boolean notification;
}
