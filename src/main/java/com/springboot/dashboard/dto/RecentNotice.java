package com.springboot.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentNotice {
    private String title;
    private LocalDateTime createdAt;
}
