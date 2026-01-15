package com.example.spring.alert.controller;

import com.example.spring.alert.domain.EmergencyAlert;
import com.example.spring.alert.dto.EmergencyAlertResponse;
import com.example.spring.alert.service.EmergencyAlertService;
import com.example.spring.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 긴급 알림 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final EmergencyAlertService emergencyAlertService;

    /**
     * 특정 가족의 미확인 긴급 알림 조회
     * GET /api/alerts/unacknowledged?familyId={familyId}
     */
    @GetMapping("/unacknowledged")
    public ResponseEntity<ApiResponse<List<EmergencyAlertResponse>>> getUnacknowledgedAlerts(
            @RequestParam Long familyId) {

        List<EmergencyAlert> alerts = emergencyAlertService.getUnacknowledgedAlerts(familyId);
        List<EmergencyAlertResponse> response = alerts.stream()
                .map(EmergencyAlertResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 긴급 알림 확인 처리
     * POST /api/alerts/{alertId}/acknowledge
     */
    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<ApiResponse<Void>> acknowledgeAlert(
            @PathVariable Long alertId) {

        emergencyAlertService.acknowledgeAlert(alertId);

        return ResponseEntity.ok(ApiResponse.success(null, "알림을 확인했습니다."));
    }

    /**
     * 특정 가족의 긴급 상황 감지 수동 실행
     * POST /api/alerts/detect
     */
    @PostMapping("/detect")
    public ResponseEntity<ApiResponse<Void>> detectEmergencies(
            @RequestParam Long familyId) {

        log.info("Manual emergency detection requested. familyId: {}", familyId);

        emergencyAlertService.detectEmergencies(familyId);

        return ResponseEntity.ok(ApiResponse.success(null, "긴급 상황 감지가 완료되었습니다."));
    }
}
