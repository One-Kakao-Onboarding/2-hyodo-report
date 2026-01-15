package com.example.spring.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 대화방 생성 요청
 */
public record CreateConversationRequest(
        @NotNull(message = "가족 ID는 필수입니다")
        Long familyId,

        @NotBlank(message = "대화방 이름은 필수입니다")
        String name
) {
}
