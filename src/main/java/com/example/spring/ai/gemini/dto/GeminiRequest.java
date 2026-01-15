package com.example.spring.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Gemini API 요청
 */
public record GeminiRequest(
        @JsonProperty("contents")
        List<Content> contents
) {
    public record Content(
            @JsonProperty("parts")
            List<Part> parts
    ) {}

    public record Part(
            @JsonProperty("text")
            String text,

            @JsonProperty("inline_data")
            InlineData inlineData
    ) {
        /**
         * 텍스트 전용 Part 생성
         */
        public static Part text(String text) {
            return new Part(text, null);
        }

        /**
         * 이미지 전용 Part 생성
         */
        public static Part image(InlineData inlineData) {
            return new Part(null, inlineData);
        }
    }

    public record InlineData(
            @JsonProperty("mime_type")
            String mimeType,

            @JsonProperty("data")
            String data // Base64 encoded image
    ) {}

    /**
     * 텍스트 프롬프트로 요청 생성
     */
    public static GeminiRequest of(String prompt) {
        Part part = Part.text(prompt);
        Content content = new Content(List.of(part));
        return new GeminiRequest(List.of(content));
    }

    /**
     * 텍스트 + 이미지로 요청 생성 (Vision API)
     */
    public static GeminiRequest ofWithImage(String prompt, String mimeType, String base64Image) {
        Part textPart = Part.text(prompt);
        Part imagePart = Part.image(new InlineData(mimeType, base64Image));
        Content content = new Content(List.of(textPart, imagePart));
        return new GeminiRequest(List.of(content));
    }
}
