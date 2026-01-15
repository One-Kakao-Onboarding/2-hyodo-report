package com.example.spring.insight.controller;

import com.example.spring.common.dto.ApiResponse;
import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import com.example.spring.insight.domain.EmotionInsight;
import com.example.spring.insight.domain.HealthInsight;
import com.example.spring.insight.domain.NeedsInsight;
import com.example.spring.insight.dto.EmotionInsightResponse;
import com.example.spring.insight.dto.HealthInsightResponse;
import com.example.spring.insight.dto.NeedsInsightResponse;
import com.example.spring.insight.repository.EmotionInsightRepository;
import com.example.spring.insight.repository.HealthInsightRepository;
import com.example.spring.insight.repository.NeedsInsightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인사이트 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final FamilyRepository familyRepository;
    private final HealthInsightRepository healthInsightRepository;
    private final EmotionInsightRepository emotionInsightRepository;
    private final NeedsInsightRepository needsInsightRepository;

    /**
     * 특정 가족의 건강 인사이트 조회
     * GET /api/insights/health?familyId={familyId}&days={days}
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<List<HealthInsightResponse>>> getHealthInsights(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "7") int days) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다."));

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<HealthInsight> insights = healthInsightRepository.findRecentByFamilyId(familyId, since);

        List<HealthInsightResponse> response = insights.stream()
                .map(HealthInsightResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 감정 인사이트 조회
     * GET /api/insights/emotion?familyId={familyId}&days={days}
     */
    @GetMapping("/emotion")
    public ResponseEntity<ApiResponse<List<EmotionInsightResponse>>> getEmotionInsights(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "7") int days) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다."));

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<EmotionInsight> insights = emotionInsightRepository.findRecentByFamilyId(familyId, since);

        List<EmotionInsightResponse> response = insights.stream()
                .map(EmotionInsightResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 니즈 인사이트 조회
     * GET /api/insights/needs?familyId={familyId}&days={days}
     */
    @GetMapping("/needs")
    public ResponseEntity<ApiResponse<List<NeedsInsightResponse>>> getNeedsInsights(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "7") int days) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다."));

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<NeedsInsight> insights = needsInsightRepository.findRecentByFamilyId(familyId, since);

        List<NeedsInsightResponse> response = insights.stream()
                .map(NeedsInsightResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 고위험 건강 인사이트 조회
     * GET /api/insights/health/high-risk?familyId={familyId}
     */
    @GetMapping("/health/high-risk")
    public ResponseEntity<ApiResponse<List<HealthInsightResponse>>> getHighRiskHealthInsights(
            @RequestParam Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다."));

        List<HealthInsight> insights = healthInsightRepository.findHighRiskByFamily(family);

        List<HealthInsightResponse> response = insights.stream()
                .map(HealthInsightResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 부정적 감정 인사이트 조회
     * GET /api/insights/emotion/negative?familyId={familyId}
     */
    @GetMapping("/emotion/negative")
    public ResponseEntity<ApiResponse<List<EmotionInsightResponse>>> getNegativeEmotionInsights(
            @RequestParam Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다."));

        List<EmotionInsight> insights = emotionInsightRepository.findNegativeByFamily(family);

        List<EmotionInsightResponse> response = insights.stream()
                .map(EmotionInsightResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 고우선순위 니즈 인사이트 조회
     * GET /api/insights/needs/high-priority?familyId={familyId}
     */
    @GetMapping("/needs/high-priority")
    public ResponseEntity<ApiResponse<List<NeedsInsightResponse>>> getHighPriorityNeedsInsights(
            @RequestParam Long familyId) {

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다."));

        List<NeedsInsight> insights = needsInsightRepository.findHighPriorityByFamily(family);

        List<NeedsInsightResponse> response = insights.stream()
                .map(NeedsInsightResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
