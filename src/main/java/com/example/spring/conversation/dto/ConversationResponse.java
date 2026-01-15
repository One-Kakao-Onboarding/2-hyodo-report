package com.example.spring.conversation.dto;

import com.example.spring.conversation.domain.Conversation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 대화방 응답
 */
public record ConversationResponse(
        Long id,
        Long familyId,
        String name,
        int messageCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MessageResponse> messages
) {
    public static ConversationResponse from(Conversation conversation) {
        List<MessageResponse> messageResponses = conversation.getMessages().stream()
                .map(MessageResponse::from)
                .toList();

        return new ConversationResponse(
                conversation.getId(),
                conversation.getFamily().getId(),
                conversation.getName(),
                messageResponses.size(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messageResponses
        );
    }

    /**
     * 메시지 없이 응답 생성 (목록 조회 시)
     */
    public static ConversationResponse fromWithoutMessages(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getFamily().getId(),
                conversation.getName(),
                conversation.getMessages().size(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                List.of()
        );
    }
}
