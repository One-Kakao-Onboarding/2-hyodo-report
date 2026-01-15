package com.example.spring.recommendation.dto;

public record ProductSuggestion(
        String name,                 // 제품명
        String detectedNeed,         // 감지된 니즈
        String suggestion,           // 추천 제품 설명
        String link,                 // 쇼핑 링크
        int priority,                // 우선순위
        String category              // 카테고리
) {
}
