package com.example.spring.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConversationTipRequest(
        @NotNull List<String> recentKeywords,
        @NotNull List<String> recentTopics,
        @NotNull String emotionStatus  // POSITIVE, NEUTRAL, CONCERNED
) {
}
