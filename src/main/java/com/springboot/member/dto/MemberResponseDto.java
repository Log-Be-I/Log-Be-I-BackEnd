package com.springboot.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberResponseDto {

    private String name;

    private String nickname;

    private String email;

    private String region;

}
