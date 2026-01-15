package com.example.spring.analysis.controller;

import com.example.spring.analysis.scheduler.AnalysisScheduler;
import com.example.spring.analysis.service.AnalysisService;
import com.example.spring.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 분석 관리 API
 * 수동으로 분석을 트리거하거나 상태를 확인
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisScheduler analysisScheduler;

    /**
     * 특정 가족의 AI 분석 수동 실행
     * POST /api/analysis/run
     * @param familyId 가족 ID
     * @param days 분석할 최근 일수 (기본 7일)
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Void>> runAnalysis(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Manual analysis triggered. familyId: {}, days: {}", familyId, days);

        analysisService.analyzeFamily(familyId, days);

        return ResponseEntity.ok(ApiResponse.success(null,
                String.format("가족 ID %d의 최근 %d일 대화 분석이 완료되었습니다.", familyId, days)));
    }

    /**
     * 건강 분석만 수동 실행
     * POST /api/analysis/health
     */
    @PostMapping("/health")
    public ResponseEntity<ApiResponse<Void>> runHealthAnalysis(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "7") int days) {

        log.info("Manual health analysis triggered. familyId: {}, days: {}", familyId, days);

        // 대화 텍스트 수집 후 건강 분석
        analysisScheduler.runManualAnalysis(familyId, days);

        return ResponseEntity.ok(ApiResponse.success(null, "건강 분석이 완료되었습니다."));
    }

    /**
     * 전체 가족 분석 수동 실행 (관리자용)
     * POST /api/analysis/run-all
     */
    @PostMapping("/run-all")
    public ResponseEntity<ApiResponse<Void>> runAllAnalysis() {

        log.info("Manual analysis for all families triggered");

        // 스케줄러의 매일 분석 로직 실행
        new Thread(() -> {
            try {
                analysisScheduler.runDailyAnalysis();
            } catch (Exception e) {
                log.error("Failed to run daily analysis manually", e);
            }
        }).start();

        return ResponseEntity.ok(ApiResponse.success(null,
                "모든 가족의 분석이 백그라운드에서 실행 중입니다."));
    }
}
