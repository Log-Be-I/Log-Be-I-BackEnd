package com.springboot.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponseDto {

    private String nickname;

    private String name;

    private String phone;

    private String email;
}
