package com.springboot.record.dto;

import com.springboot.record.entity.Record;
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
    private Long recordId;
    private LocalDateTime recordDateTime;
    private String content;
    private Record.RecordStatus recordStatus;
    private Long memberId;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
