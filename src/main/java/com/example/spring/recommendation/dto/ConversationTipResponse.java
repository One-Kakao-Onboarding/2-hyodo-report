package com.example.spring.recommendation.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ConversationTipResponse(
        List<String> questions,      // 추천 질문 목록
        List<String> topics,         // 대화 주제 목록
        int priority,                // 우선순위 (1-10)
        String category              // 카테고리
) {
}
