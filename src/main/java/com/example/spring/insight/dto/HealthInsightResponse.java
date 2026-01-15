package com.example.spring.insight.dto;

import com.example.spring.insight.domain.HealthInsight;

import java.time.LocalDateTime;

/**
 * 건강 인사이트 응답
 */
public record HealthInsightResponse(
        Long id,
        Long familyId,
        String keywords,
        Integer severity,
        String summary,
        String recommendation,
        LocalDateTime analyzedAt,
        LocalDateTime createdAt
) {
    public static HealthInsightResponse from(HealthInsight insight) {
        return new HealthInsightResponse(
                insight.getId(),
                insight.getFamily().getId(),
                insight.getKeywords(),
                insight.getSeverity(),
                insight.getSummary(),
                insight.getRecommendation(),
                insight.getAnalyzedAt(),
                insight.getCreatedAt()
        );
    }
}
