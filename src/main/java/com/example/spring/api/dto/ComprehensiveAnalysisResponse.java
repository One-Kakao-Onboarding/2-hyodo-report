package com.example.spring.api.dto;

import com.example.spring.recommendation.dto.ConversationTipResponse;
import com.example.spring.recommendation.dto.ProductSuggestion;
import com.example.spring.report.dto.RiskAnalysisResult;
import com.example.spring.report.dto.SentimentAnalysisResult;
import com.example.spring.report.service.ReportAnalysisService;

import java.util.List;

/**
 * 종합 분석 응답
 */
public record ComprehensiveAnalysisResponse(
        RiskAnalysisResult healthRisk,
        SentimentAnalysisResult sentiment,
        List<ReportAnalysisService.KeywordFrequency> keywords,
        ConversationTipResponse conversationTips,
        List<ProductSuggestion> productRecommendations
) {
}
