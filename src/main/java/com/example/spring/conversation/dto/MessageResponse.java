package com.example.spring.conversation.dto;

import com.example.spring.conversation.domain.Message;
import com.example.spring.conversation.domain.MessageType;

import java.time.LocalDateTime;

/**
 * 메시지 응답
 */
public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderNickname,
        MessageType type,
        String content,
        String imageUrl,
        String imageDescription,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getType(),
                message.getContent(),
                message.getImageUrl(),
                message.getImageDescription(),
                message.getSentAt(),
                message.getCreatedAt()
        );
    }
}
