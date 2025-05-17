package com.logbei.be.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminPostDto {
    @Schema(description = "관리자 ID", example = "admin1@gmail.com")
    private String Id;
    @Schema(description = "비밀번호", example = "1234")
    private String password;
}
