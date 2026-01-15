package com.example.spring.insight.dto;

import com.example.spring.insight.domain.EmotionInsight;

import java.time.LocalDateTime;

/**
 * 감정 인사이트 응답
 */
public record EmotionInsightResponse(
        Long id,
        Long familyId,
        String emotionType,
        Integer emotionScore,
        String description,
        String conversationTips,
        LocalDateTime analyzedAt,
        LocalDateTime createdAt
) {
    public static EmotionInsightResponse from(EmotionInsight insight) {
        return new EmotionInsightResponse(
                insight.getId(),
                insight.getFamily().getId(),
                insight.getEmotionType(),
                insight.getEmotionScore(),
                insight.getDescription(),
                insight.getConversationTips(),
                insight.getAnalyzedAt(),
                insight.getCreatedAt()
        );
    }
}
