package com.logbei.be.ai.clova;

import lombok.Data;

@Data
public class ClovaSpeechRequestDto {
    private String language = "ko-KR";
    private String completion = "sync";
    private String callback = "";
    private boolean fullText = true;
}
