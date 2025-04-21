package com.springboot.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiMessage {
    //List<message> 중 하나, gpt에 보낼 요청 메세지 히스토리 역할
    //Gpt 메세지 하나를 의미하는 클래스
    private String role; //user or system
    private String content;//해당 역할의 메세지 내용
}
