package com.example.spring.conversation.domain;

import com.example.spring.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 메시지
 * 대화방 내의 개별 메시지 (텍스트 또는 이미지)
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_conversation_sent_at", columnList = "conversation_id,sent_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 메시지가 속한 대화방
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * 메시지 발신자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * 메시지 타입 (TEXT or IMAGE)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    /**
     * 메시지 내용 (텍스트 메시지인 경우)
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 이미지 URL (이미지 메시지인 경우)
     */
    @Column(length = 500)
    private String imageUrl;

    /**
     * AI가 분석한 이미지 설명 (Gemini Vision API 결과)
     */
    @Column(columnDefinition = "TEXT")
    private String imageDescription;

    /**
     * 메시지 전송 시각
     */
    @Column(nullable = false)
    private LocalDateTime sentAt;

    /**
     * 생성 시각 (DB 저장 시각)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Message(Conversation conversation, User sender, MessageType type,
                   String content, String imageUrl, LocalDateTime sentAt) {
        this.conversation = conversation;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.imageUrl = imageUrl;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
    }

    /**
     * Conversation 연관관계 설정 (Conversation.addMessage()에서 호출)
     */
    void assignConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    /**
     * 이미지 설명 추가 (AI 분석 후)
     */
    public void updateImageDescription(String description) {
        this.imageDescription = description;
    }

    /**
     * 텍스트 메시지인지 확인
     */
    public boolean isText() {
        return this.type == MessageType.TEXT;
    }

    /**
     * 이미지 메시지인지 확인
     */
    public boolean isImage() {
        return this.type == MessageType.IMAGE;
    }

    /**
     * 이미지 분석이 완료되었는지 확인
     */
    public boolean isImageAnalyzed() {
        return this.type == MessageType.IMAGE && this.imageDescription != null;
    }
}
