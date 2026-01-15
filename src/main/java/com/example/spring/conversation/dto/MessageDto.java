package com.example.spring.conversation.dto;

import com.example.spring.conversation.domain.MessageType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 메시지 업로드 데이터
 */
public record MessageDto(
        @NotNull(message = "발신자 ID는 필수입니다")
        Long senderId,

        @NotNull(message = "메시지 타입은 필수입니다")
        MessageType type,

        String content,

        String imageUrl,

        @NotNull(message = "전송 시각은 필수입니다")
        LocalDateTime sentAt
) {
    /**
     * 텍스트 메시지 생성
     */
    public static MessageDto text(Long senderId, String content, LocalDateTime sentAt) {
        return new MessageDto(senderId, MessageType.TEXT, content, null, sentAt);
    }

    /**
     * 이미지 메시지 생성
     */
    public static MessageDto image(Long senderId, String imageUrl, LocalDateTime sentAt) {
        return new MessageDto(senderId, MessageType.IMAGE, null, imageUrl, sentAt);
    }
}
