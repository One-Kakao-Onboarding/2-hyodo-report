package com.example.spring.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HealthRiskRequest(
        @NotNull(message = "키워드 목록은 필수입니다")
        List<String> keywords,

        @Min(value = 0, message = "언급 횟수는 0 이상이어야 합니다")
        int mentionCount
) {
}
