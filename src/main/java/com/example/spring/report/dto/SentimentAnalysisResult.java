package com.example.spring.report.dto;

import lombok.Builder;

@Builder
public record SentimentAnalysisResult(
        String emotionStatus,    // POSITIVE, NEUTRAL, CONCERNED
        String emoji,            // 😊, 😐, 😟
        String summary,
        double positiveRatio,
        double negativeRatio,
        double neutralRatio,
        double conversationChange,  // 대화량 변화율
        int totalMessages
) {
}
