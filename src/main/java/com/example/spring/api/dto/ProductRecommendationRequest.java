package com.example.spring.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductRecommendationRequest(
        @NotNull List<String> needs,
        @NotNull List<String> keywords
) {
}
