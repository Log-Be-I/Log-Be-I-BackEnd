package com.springboot.keyword.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(name = "keyword Dto", description = "키워드 요청")
public class KeywordPostDto {

    @Schema(description = "키워드 이름", example = "봄.")
    private String name;
}
