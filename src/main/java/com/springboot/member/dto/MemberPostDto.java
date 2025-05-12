package com.springboot.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Setter
@Getter
@NoArgsConstructor
public class MemberPostDto {

    @Schema(description = "회원 이름", example = "기로기")
    @NotBlank
    private String name;

    @Schema(description = "닉네임", example = "방울이")
    @NotBlank
    @Pattern(regexp = "^[가-힣a-zA-Z]{2,8}$", message = "한글과 영어 대소문자만 사용 가능합니다.")
    private String nickname;

    @Schema(description = "이메일", example = "girogi@gmail.com")
    @Email
    private String email;

    @Schema(description = "지역", example = "세종시")
    @NotBlank
    private String region;

    @Schema(description = "생일", example = "1999-11-11")
    @NotBlank
    private String birth;

    @Schema(description = "프로필 사진", example = "url")
    @NotBlank
    private String profile;

    @Schema(description = "알림 설정", example = "true")
    @NotNull
    private Boolean notification;
}
