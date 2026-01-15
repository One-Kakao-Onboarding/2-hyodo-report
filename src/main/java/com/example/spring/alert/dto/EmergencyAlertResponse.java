package com.example.spring.alert.dto;

import com.example.spring.alert.domain.AlertType;
import com.example.spring.alert.domain.EmergencyAlert;

import java.time.LocalDateTime;

/**
 * 긴급 알림 응답
 */
public record EmergencyAlertResponse(
        Long id,
        Long familyId,
        AlertType alertType,
        String title,
        String content,
        Integer severity,
        String detectedKeywords,
        String aiAnalysis,
        boolean acknowledged,
        LocalDateTime acknowledgedAt,
        LocalDateTime createdAt
) {
    public static EmergencyAlertResponse from(EmergencyAlert alert) {
        return new EmergencyAlertResponse(
                alert.getId(),
                alert.getFamily().getId(),
                alert.getAlertType(),
                alert.getTitle(),
                alert.getContent(),
                alert.getSeverity(),
                alert.getDetectedKeywords(),
                alert.getAiAnalysis(),
                alert.isAcknowledged(),
                alert.getAcknowledgedAt(),
                alert.getCreatedAt()
        );
    }
}
