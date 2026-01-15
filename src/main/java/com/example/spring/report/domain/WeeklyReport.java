package com.example.spring.report.domain;

import com.example.spring.family.domain.Family;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주간 리포트
 * 매주 금요일에 생성되는 종합 분석 리포트
 */
@Entity
@Table(name = "weekly_reports", indexes = {
        @Index(name = "idx_family_generated_at", columnList = "family_id,generated_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 리포트 대상 가족
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 리포트 기간 시작일
     */
    @Column(nullable = false)
    private LocalDateTime periodStart;

    /**
     * 리포트 기간 종료일
     */
    @Column(nullable = false)
    private LocalDateTime periodEnd;

    /**
     * 전체 요약 (AI 생성)
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    /**
     * 건강 인사이트 요약
     */
    @Column(columnDefinition = "TEXT")
    private String healthSummary;

    /**
     * 감정 인사이트 요약
     */
    @Column(columnDefinition = "TEXT")
    private String emotionSummary;

    /**
     * 니즈 인사이트 요약
     */
    @Column(columnDefinition = "TEXT")
    private String needsSummary;

    /**
     * 대화 치트키 목록
     */
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationTip> conversationTips = new ArrayList<>();

    /**
     * 리포트 생성 시각
     */
    @Column(nullable = false)
    private LocalDateTime generatedAt;

    /**
     * 생성 시각
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public WeeklyReport(Family family, LocalDateTime periodStart, LocalDateTime periodEnd,
                        String summary, String healthSummary, String emotionSummary,
                        String needsSummary, LocalDateTime generatedAt) {
        this.family = family;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.summary = summary;
        this.healthSummary = healthSummary;
        this.emotionSummary = emotionSummary;
        this.needsSummary = needsSummary;
        this.generatedAt = generatedAt != null ? generatedAt : LocalDateTime.now();
    }

    /**
     * 대화 치트키 추가
     */
    public void addConversationTip(ConversationTip tip) {
        this.conversationTips.add(tip);
        tip.assignReport(this);
    }
}
