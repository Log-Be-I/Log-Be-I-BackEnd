package com.springboot.record.dto;

import com.springboot.record.entity.Record;
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
public class RecordResponseDto {
    @Schema(description = "기록 ID", example = "1")
    private Long recordId;
    @Schema(description = "기록 시간", example = "2025-04-11T11:30")
    private LocalDateTime recordDateTime;
    @Schema(description = "내용", example = "스타벅스 샌드위치는 맛있어")
    private String content;
    @Schema(description = "기록 상태", example = "RECORD_REGISTERED")
    private Record.RecordStatus recordStatus;
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;
    @Schema(description = "생성일", example = "2025-04-11T11:30")
    private LocalDateTime createdAt;
    @Schema(description = "수정일", example = "2025-04-11T11:30")
    private LocalDateTime modifiedAt;
}
