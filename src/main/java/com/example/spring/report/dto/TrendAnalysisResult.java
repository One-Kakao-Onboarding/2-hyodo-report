package com.example.spring.report.dto;

import lombok.Builder;

@Builder
public record TrendAnalysisResult(
        String direction,        // UP, DOWN, STABLE
        String icon,             // ↑, ↓, →
        String description,
        double changePercent,
        int previousValue,
        int currentValue
) {
}
