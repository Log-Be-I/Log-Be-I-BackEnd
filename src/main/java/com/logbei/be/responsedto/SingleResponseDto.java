package com.logbei.be.responsedto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "단일 응답데이터 반환")
@AllArgsConstructor
@Getter
public class SingleResponseDto<T> {
    private T data;
}
