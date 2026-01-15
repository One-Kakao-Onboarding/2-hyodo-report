package com.example.spring.insight.dto;

import com.example.spring.insight.domain.NeedsInsight;

import java.time.LocalDateTime;

/**
 * 니즈 인사이트 응답
 */
public record NeedsInsightResponse(
        Long id,
        Long familyId,
        String category,
        String items,
        Integer priority,
        String context,
        String recommendations,
        LocalDateTime analyzedAt,
        LocalDateTime createdAt
) {
    public static NeedsInsightResponse from(NeedsInsight insight) {
        return new NeedsInsightResponse(
                insight.getId(),
                insight.getFamily().getId(),
                insight.getCategory(),
                insight.getItems(),
                insight.getPriority(),
                insight.getContext(),
                insight.getRecommendations(),
                insight.getAnalyzedAt(),
                insight.getCreatedAt()
        );
    }
}
