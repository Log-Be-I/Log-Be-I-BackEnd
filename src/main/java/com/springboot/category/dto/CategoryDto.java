package com.springboot.category.dto;

import com.springboot.record.entity.Record;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class CategoryDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {
        @Schema(name = "카테고리 이름", example = "일상")
        @NotBlank(message = "카테고리 이름을 지정해 주세요.")
        private String name;

        @Schema(name = "카테고리 아이콘", example = "일상.uri")
        @NotBlank
        private String image;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch{
        private Long categoryId;

        @Schema(name = "카테고리 이름", example = "일상")
        @NotBlank(message = "카테고리 이름을 지정해 주세요.")
        private String name;

        @Schema(name = "카테고리 아이콘", example = "일상.uri")
        @NotBlank
        private String image;

        private Long memberId;

    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private Long categoryId;
        private String name;
        private String image;
        private Long memberId;
    }

}
