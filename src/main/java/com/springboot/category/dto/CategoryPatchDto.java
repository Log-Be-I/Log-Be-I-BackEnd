package com.springboot.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryPatchDto {
    private Long categoryId;

    @Schema(name = "카테고리 이름", example = "일상")
    @NotBlank(message = "카테고리 이름을 지정해 주세요.")
    private String name;

    @Schema(name = "카테고리 아이콘", example = "일상.uri")
    @NotBlank
    private String image;

    @Schema(name = "회원 ID", example = "1")
    private Long memberId;
}
