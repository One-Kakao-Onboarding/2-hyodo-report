package com.example.spring.api.dto;

import jakarta.validation.constraints.Min;

public record TrendRequest(
        @Min(value = 0) int previousValue,
        @Min(value = 0) int currentValue
) {
}
