package com.example.spring.conversation.domain;

import com.example.spring.family.domain.Family;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 대화방
 * 가족 구성원 간의 대화 공간
 */
@Entity
@Table(name = "conversations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 대화방이 속한 가족 그룹
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 대화방 이름 (예: "엄마와의 대화")
     */
    @Column(nullable = false)
    private String name;

    /**
     * 대화 내역
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    /**
     * 생성 시각
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각 (마지막 메시지 시각)
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Conversation(Family family, String name) {
        this.family = family;
        this.name = name;
    }

    /**
     * 메시지 추가
     */
    public void addMessage(Message message) {
        this.messages.add(message);
        message.assignConversation(this);
    }

    /**
     * 대화방 이름 변경
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 최근 N일 이내의 메시지만 조회
     */
    public List<Message> getRecentMessages(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return messages.stream()
                .filter(message -> message.getSentAt().isAfter(cutoffDate))
                .toList();
    }

    /**
     * 특정 기간의 메시지만 조회
     */
    public List<Message> getMessagesBetween(LocalDateTime start, LocalDateTime end) {
        return messages.stream()
                .filter(message -> {
                    LocalDateTime sentAt = message.getSentAt();
                    return sentAt.isAfter(start) && sentAt.isBefore(end);
                })
                .toList();
    }
}
