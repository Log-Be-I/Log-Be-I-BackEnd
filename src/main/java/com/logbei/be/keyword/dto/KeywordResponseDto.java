package com.logbei.be.keyword.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
//@Schema(name = "keyword Dto", description = "키워드 응답")
public class KeywordResponseDto {
    @Schema(description = "키워드 번호", example = "1")
    private Long keywordId;
    @Schema(description = "키워드 이름", example = "봄.")
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
