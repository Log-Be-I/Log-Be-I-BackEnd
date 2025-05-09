package com.springboot.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.springboot.audit.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "일정 응답 DTO")
public class ScheduleResponseDto {

    @Schema(description = "일정 Id", example = "1")
    private Long scheduleId;

    @NotBlank
    @Schema(description = "제목", example = "오늘 점심은 한식")
    private String title;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startDateTime;

    @NotBlank
<<<<<<< Updated upstream
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "종료 날짜", example = "2025-04-12T14:30")
=======
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
>>>>>>> Stashed changes
    private LocalDateTime endDateTime;

    @Schema(description = "생성일", example = "2025-04-11T11:30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-04-11T11:30")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifiedAt;
}
