package com.example.spring.report.service;

import com.example.spring.report.dto.RiskAnalysisResult;
import com.example.spring.report.dto.SentimentAnalysisResult;
import com.example.spring.report.dto.TrendAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 리포트 분석 서비스
 * 프론트엔드에서 하던 비즈니스 로직을 백엔드로 이동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAnalysisService {

    /**
     * 건강 리스크 레벨 계산
     * 프론트엔드의 getRiskColor 로직을 백엔드로 이동
     */
    public RiskAnalysisResult analyzeHealthRisk(List<String> keywords, int mentionCount) {
        String riskLevel;
        String recommendation;
        String color;

        if (mentionCount >= 10 || hasHighRiskKeywords(keywords)) {
            riskLevel = "HIGH";
            recommendation = "즉시 병원 방문을 권장합니다";
            color = "#DC2626"; // red-600
        } else if (mentionCount >= 5) {
            riskLevel = "MEDIUM";
            recommendation = "정기 검진을 권유해보세요";
            color = "#F59E0B"; // amber-500
        } else {
            riskLevel = "LOW";
            recommendation = "건강 상태가 양호합니다";
            color = "#10B981"; // green-500
        }

        return RiskAnalysisResult.builder()
                .riskLevel(riskLevel)
                .mentionCount(mentionCount)
                .keywords(keywords)
                .recommendation(recommendation)
                .color(color)
                .build();
    }

    /**
     * 고위험 키워드 체크
     */
    private boolean hasHighRiskKeywords(List<String> keywords) {
        Set<String> highRiskWords = Set.of(
                "응급", "119", "통증", "쓰러", "어지러",
                "숨쉬기", "가슴", "심장", "구토", "피"
        );

        return keywords.stream()
                .anyMatch(keyword -> highRiskWords.stream()
                        .anyMatch(keyword::contains));
    }

    /**
     * 감정 분석 및 상태 판단
     * 프론트엔드의 sentiment 계산 로직을 백엔드로 이동
     */
    public SentimentAnalysisResult analyzeSentiment(
            int positiveCount,
            int negativeCount,
            int neutralCount,
            int previousTotalCount,
            int currentTotalCount) {

        int totalCount = positiveCount + negativeCount + neutralCount;

        // 감정 비율 계산
        double positiveRatio = totalCount > 0 ? (double) positiveCount / totalCount * 100 : 0;
        double negativeRatio = totalCount > 0 ? (double) negativeCount / totalCount * 100 : 0;
        double neutralRatio = totalCount > 0 ? (double) neutralCount / totalCount * 100 : 0;

        // 대화량 변화율 계산
        double conversationChange = previousTotalCount > 0
                ? ((double) (currentTotalCount - previousTotalCount) / previousTotalCount) * 100
                : 0;

        // 감정 상태 판단
        String emotionStatus;
        String emoji;
        String summary;

        if (positiveRatio > 60) {
            emotionStatus = "POSITIVE";
            emoji = "😊";
            summary = "긍정적인 대화가 많습니다";
        } else if (negativeRatio > 40) {
            emotionStatus = "CONCERNED";
            emoji = "😟";
            summary = "부정적인 감정이 감지됩니다";
        } else {
            emotionStatus = "NEUTRAL";
            emoji = "😐";
            summary = "평범한 감정 상태입니다";
        }

        // 대화량 변화에 따른 추가 판단
        if (conversationChange < -20) {
            summary += ". 대화량이 크게 감소했습니다";
            emotionStatus = "CONCERNED";
        }

        return SentimentAnalysisResult.builder()
                .emotionStatus(emotionStatus)
                .emoji(emoji)
                .summary(summary)
                .positiveRatio(positiveRatio)
                .negativeRatio(negativeRatio)
                .neutralRatio(neutralRatio)
                .conversationChange(conversationChange)
                .totalMessages(totalCount)
                .build();
    }

    /**
     * 트렌드 분석
     * 프론트엔드의 trend 계산 로직을 백엔드로 이동
     */
    public TrendAnalysisResult analyzeTrend(int previousValue, int currentValue) {
        double changePercent = previousValue > 0
                ? ((double) (currentValue - previousValue) / previousValue) * 100
                : 0;

        String direction;
        String icon;
        String description;

        if (changePercent > 10) {
            direction = "UP";
            icon = "↑";
            description = String.format("%.1f%% 증가", changePercent);
        } else if (changePercent < -10) {
            direction = "DOWN";
            icon = "↓";
            description = String.format("%.1f%% 감소", Math.abs(changePercent));
        } else {
            direction = "STABLE";
            icon = "→";
            description = "변화 없음";
        }

        return TrendAnalysisResult.builder()
                .direction(direction)
                .icon(icon)
                .description(description)
                .changePercent(changePercent)
                .previousValue(previousValue)
                .currentValue(currentValue)
                .build();
    }

    /**
     * 키워드 빈도 분석 및 정렬
     * 프론트엔드의 keyword counting 로직을 백엔드로 이동
     */
    public List<KeywordFrequency> analyzeKeywordFrequency(List<String> messages) {
        Map<String, Integer> keywordCount = new HashMap<>();

        // 메시지에서 키워드 추출 및 카운팅
        for (String message : messages) {
            String[] words = message.split("\\s+");
            for (String word : words) {
                if (word.length() >= 2) { // 2글자 이상만
                    keywordCount.merge(word, 1, Integer::sum);
                }
            }
        }

        // 빈도순으로 정렬하여 상위 20개 반환
        return keywordCount.entrySet().stream()
                .map(entry -> new KeywordFrequency(
                        entry.getKey(),
                        entry.getValue(),
                        calculateTrendForKeyword(entry.getKey(), entry.getValue())
                ))
                .sorted(Comparator.comparingInt(KeywordFrequency::count).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 키워드별 트렌드 계산 (간단한 버전)
     */
    private String calculateTrendForKeyword(String keyword, int currentCount) {
        // 실제로는 이전 기간 데이터와 비교해야 하지만,
        // 여기서는 간단히 빈도수 기반으로 판단
        if (currentCount > 15) return "UP";
        if (currentCount < 5) return "DOWN";
        return "STABLE";
    }

    /**
     * 키워드 빈도 DTO
     */
    public record KeywordFrequency(
            String keyword,
            int count,
            String trend
    ) {}
}
