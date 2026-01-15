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
 * 건강 인사이트
 * AI가 대화에서 추출한 건강 관련 정보
 */
@Entity
@Table(name = "health_insights", indexes = {
        @Index(name = "idx_family_analyzed_at", columnList = "family_id,analyzed_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthInsight {

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
     * 추출된 건강 키워드 (JSON 배열 문자열, 예: ["무릎 통증", "당뇨"])
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String keywords;

    /**
     * 빈도/심각도 점수 (1-10)
     */
    @Column(nullable = false)
    private Integer severity;

    /**
     * AI 분석 요약
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    /**
     * 권장 조치사항
     */
    @Column(columnDefinition = "TEXT")
    private String recommendation;

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
    public HealthInsight(Family family, String keywords, Integer severity,
                         String summary, String recommendation, LocalDateTime analyzedAt) {
        this.family = family;
        this.keywords = keywords;
        this.severity = severity;
        this.summary = summary;
        this.recommendation = recommendation;
        this.analyzedAt = analyzedAt != null ? analyzedAt : LocalDateTime.now();
    }

    /**
     * 고위험 건강 이슈인지 확인
     */
    public boolean isHighRisk() {
        return this.severity >= 7;
    }
}
