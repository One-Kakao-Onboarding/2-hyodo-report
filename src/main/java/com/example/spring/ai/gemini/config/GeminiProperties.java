package com.example.spring.ai.gemini.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gemini API 설정 Properties
 */
@Component
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiProperties {

    /**
     * Gemini API Key
     */
    private String apiKey;


    /**
     * 사용할 Gemini 모델 (기본: gemini-1.5-flash)
     */
    private String model = "gemini-1.5-flash";

    /**
     * Gemini API Base URL
     */
    private String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models";

    /**
     * 텍스트 분석 엔드포인트
     */
    public String getGenerateContentUrl() {
        return apiUrl + "/" + model + ":generateContent";
    }
}
