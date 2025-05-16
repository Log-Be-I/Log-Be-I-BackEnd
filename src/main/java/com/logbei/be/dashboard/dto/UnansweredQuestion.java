package com.springboot.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnansweredQuestion {
    @Schema(description = "제목", example = "회원가입 버튼 클릭이 안됩니다.")
    private String title;
}
