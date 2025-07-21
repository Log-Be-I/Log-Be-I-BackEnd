package com.logbei.be.oauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class GoogleInfoDto {
    private String email;
    private String name;

//    public MemberPostDto toMemberRegisterDto() {
//        return new MemberPostDto(email, name, );
//    }
}
