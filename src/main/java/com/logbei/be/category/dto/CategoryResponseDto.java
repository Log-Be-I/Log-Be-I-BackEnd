package com.logbei.be.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class CategoryResponseDto {
    @Schema(name = "카테고리 ID", example = "일상.uri")
    private Long categoryId;

    @Schema(name = "카테고리 이름", example = "일상")
    private String name;

    @Schema(name = "카테고리 아이콘", example = "일상.uri")
    private String image;

    @Schema(name = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-04-11T11:30")
    private LocalDateTime modifiedAt;
}
