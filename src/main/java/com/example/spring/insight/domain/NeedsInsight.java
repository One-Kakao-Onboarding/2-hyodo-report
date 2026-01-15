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
 * 니즈 인사이트
 * AI가 대화에서 추출한 구매 의도 및 필요 항목
 */
@Entity
@Table(name = "needs_insights", indexes = {
        @Index(name = "idx_family_analyzed_at", columnList = "family_id,analyzed_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NeedsInsight {

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
     * 니즈 카테고리 (예: "건강/의료", "생활용품", "식품", "여가")
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * 추출된 니즈 항목들 (JSON 배열 문자열, 예: ["온열 찜질기", "홍삼 스틱"])
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String items;

    /**
     * 우선순위 (1-10, 높을수록 시급)
     */
    @Column(nullable = false)
    private Integer priority;

    /**
     * AI 분석 근거/맥락
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String context;

    /**
     * 추천 상품/서비스 (JSON 배열 문자열)
     */
    @Column(columnDefinition = "TEXT")
    private String recommendations;

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
    public NeedsInsight(Family family, String category, String items, Integer priority,
                        String context, String recommendations, LocalDateTime analyzedAt) {
        this.family = family;
        this.category = category;
        this.items = items;
        this.priority = priority;
        this.context = context;
        this.recommendations = recommendations;
        this.analyzedAt = analyzedAt != null ? analyzedAt : LocalDateTime.now();
    }

    /**
     * 고우선순위 니즈인지 확인
     */
    public boolean isHighPriority() {
        return this.priority >= 7;
    }
}
