package com.logbei.be.auth.dto;

import lombok.Getter;

@Getter
// 로그인에 필요한 DTO
public class LoginDto {
    private String username;
    private String password;
}
