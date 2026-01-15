package com.example.spring.alert.domain;

import com.example.spring.family.domain.Family;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 긴급 알림
 * 고위험 상황 감지 시 즉시 생성되는 알림
 */
@Entity
@Table(name = "emergency_alerts", indexes = {
        @Index(name = "idx_family_created_at", columnList = "family_id,created_at"),
        @Index(name = "idx_family_acknowledged", columnList = "family_id,acknowledged")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 대상 가족
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 알림 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    /**
     * 알림 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 알림 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 심각도 (1-10)
     */
    @Column(nullable = false)
    private Integer severity;

    /**
     * 감지된 키워드 (JSON 배열)
     */
    @Column(columnDefinition = "TEXT")
    private String detectedKeywords;

    /**
     * AI 분석 근거
     */
    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    /**
     * 알림 확인 여부
     */
    @Column(nullable = false)
    private boolean acknowledged = false;

    /**
     * 알림 확인 시각
     */
    @Column
    private LocalDateTime acknowledgedAt;

    /**
     * 생성 시각 (알림 발생 시각)
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public EmergencyAlert(Family family, AlertType alertType, String title, String content,
                          Integer severity, String detectedKeywords, String aiAnalysis) {
        this.family = family;
        this.alertType = alertType;
        this.title = title;
        this.content = content;
        this.severity = severity;
        this.detectedKeywords = detectedKeywords;
        this.aiAnalysis = aiAnalysis;
    }

    /**
     * 알림 확인 처리
     */
    public void acknowledge() {
        this.acknowledged = true;
        this.acknowledgedAt = LocalDateTime.now();
    }

    /**
     * 고위험 알림인지 확인
     */
    public boolean isHighSeverity() {
        return this.severity >= 8;
    }
}
