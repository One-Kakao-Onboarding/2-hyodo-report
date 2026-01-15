package com.example.spring.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record KeywordAnalysisRequest(
        @NotNull(message = "메시지 목록은 필수입니다")
        List<String> messages
) {
}
