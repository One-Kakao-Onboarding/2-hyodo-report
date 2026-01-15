package com.example.spring.api.dto;

import jakarta.validation.constraints.Min;

public record SentimentRequest(
        @Min(value = 0) int positiveCount,
        @Min(value = 0) int negativeCount,
        @Min(value = 0) int neutralCount,
        @Min(value = 0) int previousTotalCount,
        @Min(value = 0) int currentTotalCount
) {
}
