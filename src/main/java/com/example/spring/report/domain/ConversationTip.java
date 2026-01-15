package com.example.spring.report.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대화 치트키
 * AI가 제안하는 부모님과의 대화 소재
 */
@Entity
@Table(name = "conversation_tips")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 리포트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private WeeklyReport report;

    /**
     * 대화 소재 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 우선순위 (1-10, 높을수록 중요)
     */
    @Column(nullable = false)
    private Integer priority;

    /**
     * 카테고리 (예: "건강 관심", "감정 케어", "취미 공유")
     */
    @Column(length = 50)
    private String category;

    @Builder
    public ConversationTip(WeeklyReport report, String content, Integer priority, String category) {
        this.report = report;
        this.content = content;
        this.priority = priority;
        this.category = category;
    }

    /**
     * WeeklyReport 연관관계 설정
     */
    void assignReport(WeeklyReport report) {
        this.report = report;
    }
}
