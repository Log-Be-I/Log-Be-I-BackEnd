package com.logbei.be.category.dto;

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
public class CategoryPostDto {
    @Schema(name = "카테고리 이름", example = "일상")
    @NotBlank(message = "카테고리 이름을 지정해 주세요.")
    private String name;

    @NotBlank
    @Schema(name = "카테고리 아이콘", example = "일상.uri")
    private String image;
}
