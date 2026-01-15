package com.example.spring.report.dto;

import com.example.spring.report.domain.WeeklyReport;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주간 리포트 응답
 */
public record WeeklyReportResponse(
        Long id,
        Long familyId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        String summary,
        String healthSummary,
        String emotionSummary,
        String needsSummary,
        List<ConversationTipResponse> conversationTips,
        LocalDateTime generatedAt,
        LocalDateTime createdAt
) {
    public static WeeklyReportResponse from(WeeklyReport report) {
        List<ConversationTipResponse> tips = report.getConversationTips().stream()
                .map(ConversationTipResponse::from)
                .toList();

        return new WeeklyReportResponse(
                report.getId(),
                report.getFamily().getId(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getSummary(),
                report.getHealthSummary(),
                report.getEmotionSummary(),
                report.getNeedsSummary(),
                tips,
                report.getGeneratedAt(),
                report.getCreatedAt()
        );
    }
}
