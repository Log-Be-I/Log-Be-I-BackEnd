package com.springboot.ai.openai.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
public class OpenAiRequest {
    //GPT 응답을 파싱하기 위한 클래스
    //OpenAiService 에서 요청 만들 때 사용
    //JSON 으로 변환되어 GPT 서버에 전송
    private String model;    //사용할 GPT 모델 (gpt-4-turbo 등)
    private List<OpenAiMessage> messages;     //메시지 히스토리
    private double temperature = 0.7;   //창의성 정도 (기본갑 0.7%)
}
