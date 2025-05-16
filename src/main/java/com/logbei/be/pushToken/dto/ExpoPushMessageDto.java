package com.logbei.be.pushToken.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpoPushMessageDto {
    private String to;
    private String title;
    private String body;
    private Map<String, Object> data;
    private String sound;
    private String priority;

}
