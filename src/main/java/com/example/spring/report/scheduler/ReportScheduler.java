package com.example.spring.report.scheduler;

import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import com.example.spring.report.domain.WeeklyReport;
import com.example.spring.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주간 리포트 생성 스케줄러
 * 매주 금요일 오후 3시에 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportService reportService;
    private final FamilyRepository familyRepository;

    /**
     * 매주 금요일 오후 3시에 실행
     * 모든 가족 그룹의 주간 리포트 생성
     */
    @Scheduled(cron = "0 0 15 * * FRI")
    public void generateWeeklyReports() {
        log.info("Starting weekly report generation scheduler");

        List<Family> families = familyRepository.findAll();

        log.info("Found {} families to generate reports", families.size());

        int successCount = 0;
        int failCount = 0;

        for (Family family : families) {
            try {
                WeeklyReport report = reportService.generateWeeklyReport(family.getId());
                log.info("Weekly report generated. reportId: {}, familyId: {}", report.getId(), family.getId());
                successCount++;

                // TODO: 알림톡 전송 (추후 구현)
                // notificationService.sendWeeklyReportNotification(report);

            } catch (IllegalStateException e) {
                // 이미 생성되었거나 인사이트가 없는 경우
                log.warn("Skipped report generation. familyId: {}, reason: {}", family.getId(), e.getMessage());

            } catch (Exception e) {
                log.error("Failed to generate weekly report. familyId: {}", family.getId(), e);
                failCount++;
            }
        }

        log.info("Weekly report generation completed. success: {}, failed: {}", successCount, failCount);
    }

    /**
     * 테스트용 수동 실행 메서드
     */
    public WeeklyReport runManualGeneration(Long familyId) {
        log.info("Running manual report generation. familyId: {}", familyId);
        return reportService.generateWeeklyReport(familyId);
    }
}
