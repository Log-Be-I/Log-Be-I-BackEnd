package com.springboot.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiResponse {
    //GPT 응답을 파싱하기 위한 클래스 : GPT 서버로부터 받은 응답(JSON)을 이 구조로 파싱
    //GPT가 주는 답변 리스트(보통 1개)
    private List<Choice> choices;

//    choices[0].message.content가 우리가 원하는 답변

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        //GPT 응답메세지 (role : assistant, content: 답변)
        private OpenAiMessage message;
    }
}
