package com.example.spring.api.dto;

import java.time.LocalDateTime;

public record MessageDto(
        String content,
        LocalDateTime timestamp,
        String senderId
) {
}
