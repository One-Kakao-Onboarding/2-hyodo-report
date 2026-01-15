package com.example.spring.report.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RiskAnalysisResult(
        String riskLevel,        // HIGH, MEDIUM, LOW
        int mentionCount,
        List<String> keywords,
        String recommendation,
        String color              // 프론트엔드에서 사용할 색상 코드
) {
}
