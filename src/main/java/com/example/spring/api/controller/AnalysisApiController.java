package com.example.spring.api.controller;

import com.example.spring.api.dto.*;
import com.example.spring.common.dto.ApiResponse;
import com.example.spring.recommendation.dto.ConversationTipResponse;
import com.example.spring.recommendation.dto.ProductSuggestion;
import com.example.spring.recommendation.service.RecommendationService;
import com.example.spring.report.dto.RiskAnalysisResult;
import com.example.spring.report.dto.SentimentAnalysisResult;
import com.example.spring.report.dto.TrendAnalysisResult;
import com.example.spring.report.service.ReportAnalysisService;
import com.example.spring.statistics.dto.ConversationStatsResponse;
import com.example.spring.statistics.dto.MessageStatistics;
import com.example.spring.statistics.service.StatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 인증 없이 사용 가능한 AI 분석 API
 * 프론트엔드가 데이터를 보내면 분석 결과를 반환
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisApiController {

    private final ReportAnalysisService reportAnalysisService;
    private final RecommendationService recommendationService;
    private final StatisticsService statisticsService;

    /**
     * 건강 리스크 분석
     * POST /api/analysis/health-risk
     */
    @PostMapping("/health-risk")
    public ResponseEntity<ApiResponse<RiskAnalysisResult>> analyzeHealthRisk(
            @Valid @RequestBody HealthRiskRequest request) {

        log.info("Health risk analysis request: keywords={}, mentions={}",
                request.keywords(), request.mentionCount());

        RiskAnalysisResult result = reportAnalysisService.analyzeHealthRisk(
                request.keywords(),
                request.mentionCount()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "건강 리스크 분석 완료"));
    }

    /**
     * 감정 분석
     * POST /api/analysis/sentiment
     */
    @PostMapping("/sentiment")
    public ResponseEntity<ApiResponse<SentimentAnalysisResult>> analyzeSentiment(
            @Valid @RequestBody SentimentRequest request) {

        log.info("Sentiment analysis request: positive={}, negative={}, neutral={}",
                request.positiveCount(), request.negativeCount(), request.neutralCount());

        SentimentAnalysisResult result = reportAnalysisService.analyzeSentiment(
                request.positiveCount(),
                request.negativeCount(),
                request.neutralCount(),
                request.previousTotalCount(),
                request.currentTotalCount()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "감정 분석 완료"));
    }

    /**
     * 트렌드 분석
     * POST /api/analysis/trend
     */
    @PostMapping("/trend")
    public ResponseEntity<ApiResponse<TrendAnalysisResult>> analyzeTrend(
            @Valid @RequestBody TrendRequest request) {

        log.info("Trend analysis request: previous={}, current={}",
                request.previousValue(), request.currentValue());

        TrendAnalysisResult result = reportAnalysisService.analyzeTrend(
                request.previousValue(),
                request.currentValue()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "트렌드 분석 완료"));
    }

    /**
     * 키워드 빈도 분석
     * POST /api/analysis/keywords
     */
    @PostMapping("/keywords")
    public ResponseEntity<ApiResponse<List<ReportAnalysisService.KeywordFrequency>>> analyzeKeywords(
            @Valid @RequestBody KeywordAnalysisRequest request) {

        log.info("Keyword analysis request: {} messages", request.messages().size());

        List<ReportAnalysisService.KeywordFrequency> result =
                reportAnalysisService.analyzeKeywordFrequency(request.messages());

        return ResponseEntity.ok(ApiResponse.success(result, "키워드 분석 완료"));
    }

    /**
     * 대화 팁 생성
     * POST /api/analysis/conversation-tips
     */
    @PostMapping("/conversation-tips")
    public ResponseEntity<ApiResponse<ConversationTipResponse>> generateConversationTips(
            @Valid @RequestBody ConversationTipRequest request) {

        log.info("Conversation tips request: keywords={}, emotion={}",
                request.recentKeywords(), request.emotionStatus());

        ConversationTipResponse result = recommendationService.generateConversationTips(
                request.recentKeywords(),
                request.recentTopics(),
                request.emotionStatus()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "대화 팁 생성 완료"));
    }

    /**
     * 제품 추천
     * POST /api/analysis/product-recommendations
     */
    @PostMapping("/product-recommendations")
    public ResponseEntity<ApiResponse<List<ProductSuggestion>>> recommendProducts(
            @Valid @RequestBody ProductRecommendationRequest request) {

        log.info("Product recommendation request: needs={}, keywords={}",
                request.needs(), request.keywords());

        List<ProductSuggestion> result = recommendationService.recommendProducts(
                request.needs(),
                request.keywords()
        );

        return ResponseEntity.ok(ApiResponse.success(result, "제품 추천 완료"));
    }

    /**
     * 대화 통계 계산
     * POST /api/analysis/conversation-stats
     */
    @PostMapping("/conversation-stats")
    public ResponseEntity<ApiResponse<ConversationStatsResponse>> calculateConversationStats(
            @Valid @RequestBody ConversationStatsRequest request) {

        log.info("Conversation stats request: {} current messages, {} previous messages",
                request.currentMessages().size(), request.previousMessages().size());

        // DTO를 Service의 MessageRecord로 변환
        List<StatisticsService.MessageRecord> currentRecords = request.currentMessages().stream()
                .map(msg -> new StatisticsService.MessageRecord(
                        msg.content(), msg.timestamp(), msg.senderId()))
                .toList();

        List<StatisticsService.MessageRecord> previousRecords = request.previousMessages().stream()
                .map(msg -> new StatisticsService.MessageRecord(
                        msg.content(), msg.timestamp(), msg.senderId()))
                .toList();

        ConversationStatsResponse result = statisticsService.calculateConversationStats(
                request.startDate(),
                request.endDate(),
                currentRecords,
                previousRecords
        );

        return ResponseEntity.ok(ApiResponse.success(result, "대화 통계 계산 완료"));
    }

    /**
     * 메시지 통계 계산
     * POST /api/analysis/message-stats
     */
    @PostMapping("/message-stats")
    public ResponseEntity<ApiResponse<MessageStatistics>> calculateMessageStatistics(
            @Valid @RequestBody MessageStatsRequest request) {

        log.info("Message stats request: {} messages", request.messages().size());

        // DTO를 Service의 MessageRecord로 변환
        List<StatisticsService.MessageRecord> records = request.messages().stream()
                .map(msg -> new StatisticsService.MessageRecord(
                        msg.content(), msg.timestamp(), msg.senderId()))
                .toList();

        MessageStatistics result = statisticsService.calculateMessageStatistics(records);

        return ResponseEntity.ok(ApiResponse.success(result, "메시지 통계 계산 완료"));
    }

    /**
     * 종합 분석 (한 번에 여러 분석 수행)
     * POST /api/analysis/comprehensive
     */
    @PostMapping("/comprehensive")
    public ResponseEntity<ApiResponse<ComprehensiveAnalysisResponse>> comprehensiveAnalysis(
            @Valid @RequestBody ComprehensiveAnalysisRequest request) {

        log.info("Comprehensive analysis request received");

        // 건강 리스크 분석
        RiskAnalysisResult riskAnalysis = reportAnalysisService.analyzeHealthRisk(
                request.keywords(),
                request.healthMentionCount()
        );

        // 감정 분석
        SentimentAnalysisResult sentimentAnalysis = reportAnalysisService.analyzeSentiment(
                request.positiveCount(),
                request.negativeCount(),
                request.neutralCount(),
                request.previousMessageCount(),
                request.currentMessageCount()
        );

        // 키워드 분석
        List<ReportAnalysisService.KeywordFrequency> keywords =
                reportAnalysisService.analyzeKeywordFrequency(request.messages());

        // 대화 팁 생성
        ConversationTipResponse conversationTips = recommendationService.generateConversationTips(
                request.keywords(),
                request.recentTopics(),
                sentimentAnalysis.emotionStatus()
        );

        // 제품 추천
        List<ProductSuggestion> products = recommendationService.recommendProducts(
                request.detectedNeeds(),
                request.keywords()
        );

        ComprehensiveAnalysisResponse response = new ComprehensiveAnalysisResponse(
                riskAnalysis,
                sentimentAnalysis,
                keywords,
                conversationTips,
                products
        );

        return ResponseEntity.ok(ApiResponse.success(response, "종합 분석 완료"));
    }
}
