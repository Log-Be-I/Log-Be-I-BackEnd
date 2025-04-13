package com.springboot.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberResponseDto {

    private String name;

    private String nickname;

    private String email;

    private String region;

    private String birth;

    private String profile;

    private Boolean notification;
}
