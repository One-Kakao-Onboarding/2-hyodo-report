package com.example.spring.conversation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 대화 메시지 일괄 업로드 요청
 */
public record UploadMessagesRequest(
        @NotNull(message = "대화방 ID는 필수입니다")
        Long conversationId,

        @NotEmpty(message = "메시지 목록은 비어있을 수 없습니다")
        @Valid
        List<MessageDto> messages
) {
}
