package com.springboot.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberPostDto {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[가-힣a-z]{2,8}$", message = "한글과 영어 소문자만 사용 가능합니다.")
    private String nickname;

    @NotBlank
    private String email;

    @NotBlank
    private String region;

    @NotBlank
    private String birth;

    @NotBlank
    private String profile;

    @NotNull
    private Boolean notification;
}
