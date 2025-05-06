package com.springboot.category.dto;

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
    private Long categoryId;
    private String name;
    private String image;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
