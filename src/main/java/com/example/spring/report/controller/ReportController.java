package com.example.spring.report.controller;

import com.example.spring.common.dto.ApiResponse;
import com.example.spring.report.domain.WeeklyReport;
import com.example.spring.report.dto.WeeklyReportResponse;
import com.example.spring.report.scheduler.ReportScheduler;
import com.example.spring.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주간 리포트 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportScheduler reportScheduler;

    /**
     * 특정 가족의 주간 리포트 수동 생성
     * POST /api/reports/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> generateReport(
            @RequestParam Long familyId) {

        log.info("Manual report generation requested. familyId: {}", familyId);

        WeeklyReport report = reportService.generateWeeklyReport(familyId);
        WeeklyReportResponse response = WeeklyReportResponse.from(report);

        return ResponseEntity.ok(ApiResponse.success(response, "주간 리포트가 생성되었습니다."));
    }

    /**
     * 특정 가족의 최신 주간 리포트 조회
     * GET /api/reports/latest?familyId={familyId}
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getLatestReport(
            @RequestParam Long familyId) {

        WeeklyReport report = reportService.getLatestReport(familyId);
        WeeklyReportResponse response = WeeklyReportResponse.from(report);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 모든 주간 리포트 조회
     * GET /api/reports?familyId={familyId}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WeeklyReportResponse>>> getAllReports(
            @RequestParam Long familyId) {

        List<WeeklyReport> reports = reportService.getAllReports(familyId);
        List<WeeklyReportResponse> response = reports.stream()
                .map(WeeklyReportResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 모든 가족의 주간 리포트 수동 생성 (관리자용)
     * POST /api/reports/generate-all
     */
    @PostMapping("/generate-all")
    public ResponseEntity<ApiResponse<Void>> generateAllReports() {

        log.info("Manual report generation for all families requested");

        // 백그라운드에서 실행
        new Thread(() -> {
            try {
                reportScheduler.generateWeeklyReports();
            } catch (Exception e) {
                log.error("Failed to generate weekly reports manually", e);
            }
        }).start();

        return ResponseEntity.ok(ApiResponse.success(null,
                "모든 가족의 주간 리포트 생성이 백그라운드에서 실행 중입니다."));
    }
}
