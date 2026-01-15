package com.example.spring.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 종합 분석 요청 (한 번에 모든 분석 수행)
 */
public record ComprehensiveAnalysisRequest(
        @NotNull List<String> messages,
        @NotNull List<String> keywords,
        @NotNull List<String> recentTopics,
        @NotNull List<String> detectedNeeds,
        int healthMentionCount,
        int positiveCount,
        int negativeCount,
        int neutralCount,
        int previousMessageCount,
        int currentMessageCount
) {
}
