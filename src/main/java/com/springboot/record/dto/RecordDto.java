package com.springboot.record.dto;

import com.springboot.category.dto.CategoryDto;
import com.springboot.record.entity.Record;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

public class RecordDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post {

        @Schema(description = "기록 등록 시간", example = "09시 30분")
        @NotBlank
        private String recordTime;

        @Schema(description = "기록의 내용", example = "아침에 삼각김밥 먹고, 약 먹음")
        @NotBlank(message = "내용을 작성해주세요.")
        private String content;

        @Schema(description = "등록 회원", example = "50")
        private long memberId;

        @Schema(description = "기록 분류", example = "1")
        private long categoryId;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Patch {

        private Long recordId;

        @Schema(description = "기록 등록 시간", example = "08시 30분")
        @NotBlank
        private String recordTime;

        @Schema(description = "기록의 내용", example = "아침에 삼각김밥 먹고, 약 먹음")
        @NotBlank(message = "내용을 작성해주세요.")
        private String content;

        @Schema(description = "기록 분류", example = "1")
        private long categoryId;

    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long recordId;
        private String recordTime;
        private String content;
        private Record.RecordStatus recordStatus;
        private long memberId;
        private CategoryDto.Response category;
    }

}
