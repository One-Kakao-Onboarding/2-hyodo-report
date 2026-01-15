package com.example.spring.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Gemini API 응답
 */
public record GeminiResponse(
        @JsonProperty("candidates")
        List<Candidate> candidates
) {
    public record Candidate(
            @JsonProperty("content")
            Content content,

            @JsonProperty("finishReason")
            String finishReason,

            @JsonProperty("index")
            int index
    ) {}

    public record Content(
            @JsonProperty("parts")
            List<Part> parts,

            @JsonProperty("role")
            String role
    ) {}

    public record Part(
            @JsonProperty("text")
            String text
    ) {}

    /**
     * 첫 번째 응답의 텍스트 추출
     */
    public String getFirstText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Candidate firstCandidate = candidates.get(0);
        if (firstCandidate.content() == null || firstCandidate.content().parts() == null
                || firstCandidate.content().parts().isEmpty()) {
            return "";
        }

        Part firstPart = firstCandidate.content().parts().get(0);
        return firstPart.text() != null ? firstPart.text() : "";
    }
}
