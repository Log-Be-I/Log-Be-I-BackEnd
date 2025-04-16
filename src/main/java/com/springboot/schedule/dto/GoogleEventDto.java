package com.springboot.schedule.dto;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class GoogleEventDto {
    private String summary;          // 제목
    private String description;      // 설명
    private String location;         // 장소
    private String startDateTime;    // 시작 시간 (ISO 8601 문자열)
    private String endDateTime;      // 종료 시간
    private String calendarId;       // 캘린더 ID (기본: "primary")
}
