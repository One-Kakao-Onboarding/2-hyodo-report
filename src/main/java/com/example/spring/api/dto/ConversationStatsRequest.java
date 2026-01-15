package com.example.spring.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationStatsRequest(
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        @NotNull List<MessageDto> currentMessages,
        @NotNull List<MessageDto> previousMessages
) {
}
