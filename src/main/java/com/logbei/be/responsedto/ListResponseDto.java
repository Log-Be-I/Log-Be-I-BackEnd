package com.logbei.be.responsedto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Schema(description = "List 응답 데이터 반환 타입")
@Getter
public class ListResponseDto<T> {
    private List<T> data;

    public ListResponseDto(List<T> data) {
        this.data = data;
    }
}
