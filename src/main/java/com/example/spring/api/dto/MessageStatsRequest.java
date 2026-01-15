package com.example.spring.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MessageStatsRequest(
        @NotNull List<MessageDto> messages
) {
}
