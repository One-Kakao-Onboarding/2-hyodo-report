package com.example.spring.statistics.dto;

import lombok.Builder;

@Builder
public record MessageStatistics(
        double averageLength,
        int minLength,
        int maxLength,
        int totalCharacters,
        int shortAnswerCount,          // 단답형 응답 수
        double shortAnswerRatio        // 단답형 응답 비율 (%)
) {
}
