package com.example.spring.ai.gemini.service;

import com.example.spring.ai.gemini.config.GeminiProperties;
import com.example.spring.ai.gemini.dto.GeminiRequest;
import com.example.spring.ai.gemini.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Gemini API 클라이언트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient webClient;
    private final GeminiProperties geminiProperties;

    /**
     * 텍스트 프롬프트로 Gemini API 호출
     *
     * @param prompt 프롬프트 텍스트
     * @return Gemini AI의 응답 텍스트
     */
    public String generate(String prompt) {
        log.info("Calling Gemini API with text prompt. promptLength: {}", prompt.length());

        GeminiRequest request = GeminiRequest.of(prompt);

        try {
            GeminiResponse response = webClient.post()
                    .uri(geminiProperties.getGenerateContentUrl() + "?key=" + geminiProperties.getApiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response == null) {
                log.error("Gemini API returned null response");
                throw new RuntimeException("Gemini API 응답이 없습니다");
            }

            String result = response.getFirstText();
            log.info("Gemini API call successful. responseLength: {}", result.length());

            return result;

        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 텍스트 + 이미지로 Gemini Vision API 호출
     *
     * @param prompt 프롬프트 텍스트
     * @param mimeType 이미지 MIME 타입 (예: "image/jpeg", "image/png")
     * @param base64Image Base64로 인코딩된 이미지 데이터
     * @return Gemini AI의 응답 텍스트
     */
    public String generateWithImage(String prompt, String mimeType, String base64Image) {
        log.info("Calling Gemini Vision API. promptLength: {}, mimeType: {}",
                prompt.length(), mimeType);

        GeminiRequest request = GeminiRequest.ofWithImage(prompt, mimeType, base64Image);

        try {
            GeminiResponse response = webClient.post()
                    .uri(geminiProperties.getGenerateContentUrl() + "?key=" + geminiProperties.getApiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response == null) {
                log.error("Gemini Vision API returned null response");
                throw new RuntimeException("Gemini Vision API 응답이 없습니다");
            }

            String result = response.getFirstText();
            log.info("Gemini Vision API call successful. responseLength: {}", result.length());

            return result;

        } catch (Exception e) {
            log.error("Failed to call Gemini Vision API", e);
            throw new RuntimeException("Gemini Vision API 호출 실패: " + e.getMessage(), e);
        }
    }
}
