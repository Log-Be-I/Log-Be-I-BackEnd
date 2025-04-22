package com.springboot.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Choice {
    // GPT 응답에서 하나의 choice(답변)를 나타냄
    // 보통 GPT 는 여러 답변을 줄 수 있고, 그 중 하나가 choice 객체임
    private OpenAiMessage message;
}
