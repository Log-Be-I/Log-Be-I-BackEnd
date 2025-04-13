package com.springboot.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberPatchDto {

    private String name;

    @Pattern(regexp = "^[가-힣a-z]{2,8}$", message = "한글과 영어 소문자만 사용 가능합니다.")
    private String nickname;

    private String region;

    private String birth;

    private String profile;

    private Boolean notification;
}
