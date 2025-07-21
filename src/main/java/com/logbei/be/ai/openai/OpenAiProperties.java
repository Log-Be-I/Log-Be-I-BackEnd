package com.logbei.be.ai.openai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAiProperties {
    @Value("${openai.api-key}")
    private String apiKey;
    @Value("${openai.base-url}")
    private String baseUrl;
    @Value("${openai.model}")
    private String model;
}
