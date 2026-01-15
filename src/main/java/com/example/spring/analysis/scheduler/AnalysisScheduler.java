package com.example.spring.analysis.scheduler;

import com.example.spring.alert.service.EmergencyAlertService;
import com.example.spring.analysis.service.AnalysisService;
import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 분석 스케줄러
 * 매일 자정에 모든 가족의 대화를 분석하고 긴급 상황 감지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisScheduler {

    private final AnalysisService analysisService;
    private final EmergencyAlertService emergencyAlertService;
    private final FamilyRepository familyRepository;

    /**
     * 매일 자정에 실행
     * 모든 가족 그룹의 최근 7일 대화를 분석
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyAnalysis() {
        log.info("Starting daily analysis scheduler");

        List<Family> families = familyRepository.findAll();

        log.info("Found {} families to analyze", families.size());

        for (Family family : families) {
            try {
                // 최근 7일 대화 분석
                analysisService.analyzeFamily(family.getId(), 7);
                log.info("Analysis completed for family. familyId: {}", family.getId());

                // 긴급 상황 감지
                emergencyAlertService.detectEmergencies(family.getId());
                log.info("Emergency detection completed for family. familyId: {}", family.getId());

            } catch (Exception e) {
                log.error("Failed to analyze family. familyId: {}", family.getId(), e);
                // 한 가족 분석 실패해도 다른 가족 분석은 계속 진행
            }
        }

        log.info("Daily analysis scheduler completed");
    }

    /**
     * 테스트용 수동 실행 메서드
     * 특정 가족의 분석을 즉시 실행
     */
    public void runManualAnalysis(Long familyId, int days) {
        log.info("Running manual analysis. familyId: {}, days: {}", familyId, days);
        analysisService.analyzeFamily(familyId, days);
    }
}
