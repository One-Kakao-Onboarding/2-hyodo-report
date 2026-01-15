package com.example.spring.insight.domain;

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
 * 감정 인사이트
 * AI가 대화에서 추출한 감정 상태 정보
 */
@Entity
@Table(name = "emotion_insights", indexes = {
        @Index(name = "idx_family_analyzed_at", columnList = "family_id,analyzed_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 분석 대상 가족
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 주요 감정 타입 (예: "긍정", "우울", "외로움", "불안")
     */
    @Column(nullable = false, length = 50)
    private String emotionType;

    /**
     * 감정 점수 (-10 ~ +10, 음수는 부정, 양수는 긍정)
     */
    @Column(nullable = false)
    private Integer emotionScore;

    /**
     * AI 분석 설명
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * 대화 소재 제안 (JSON 배열 문자열)
     */
    @Column(columnDefinition = "TEXT")
    private String conversationTips;

    /**
     * 분석 시각
     */
    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    /**
     * 생성 시각
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public EmotionInsight(Family family, String emotionType, Integer emotionScore,
                          String description, String conversationTips, LocalDateTime analyzedAt) {
        this.family = family;
        this.emotionType = emotionType;
        this.emotionScore = emotionScore;
        this.description = description;
        this.conversationTips = conversationTips;
        this.analyzedAt = analyzedAt != null ? analyzedAt : LocalDateTime.now();
    }

    /**
     * 부정적 감정인지 확인
     */
    public boolean isNegative() {
        return this.emotionScore < -3;
    }

    /**
     * 고위험 감정 상태인지 확인 (우울, 외로움 등)
     */
    public boolean isHighRisk() {
        return this.emotionScore <= -7 ||
                this.emotionType.contains("우울") ||
                this.emotionType.contains("외로움");
    }
}
