package com.springboot.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleInfoDto {
    private String email;
    private String name;

//    public MemberPostDto toMemberRegisterDto() {
//        return new MemberPostDto(email, name, );
//    }
}
