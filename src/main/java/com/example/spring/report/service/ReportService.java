package com.example.spring.report.service;

import com.example.spring.ai.gemini.service.GeminiClient;
import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import com.example.spring.insight.domain.EmotionInsight;
import com.example.spring.insight.domain.HealthInsight;
import com.example.spring.insight.domain.NeedsInsight;
import com.example.spring.insight.repository.EmotionInsightRepository;
import com.example.spring.insight.repository.HealthInsightRepository;
import com.example.spring.insight.repository.NeedsInsightRepository;
import com.example.spring.report.domain.ConversationTip;
import com.example.spring.report.domain.WeeklyReport;
import com.example.spring.report.repository.WeeklyReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리포트 생성 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final GeminiClient geminiClient;
    private final FamilyRepository familyRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final HealthInsightRepository healthInsightRepository;
    private final EmotionInsightRepository emotionInsightRepository;
    private final NeedsInsightRepository needsInsightRepository;
    private final ObjectMapper objectMapper;

    /**
     * 특정 가족의 주간 리포트 생성
     */
    @Transactional
    public WeeklyReport generateWeeklyReport(Long familyId) {
        log.info("Generating weekly report for family. familyId: {}", familyId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        // 리포트 기간 설정 (지난 주 월요일 00:00 ~ 일요일 23:59)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodEnd = now.minusDays(now.getDayOfWeek().getValue()).withHour(23).withMinute(59).withSecond(59);
        LocalDateTime periodStart = periodEnd.minusDays(6).withHour(0).withMinute(0).withSecond(0);

        // 이미 해당 기간 리포트가 있는지 확인
        if (weeklyReportRepository.existsByFamilyAndPeriod(family, periodStart, periodEnd)) {
            log.warn("Weekly report already exists for this period. familyId: {}", familyId);
            throw new IllegalStateException("해당 기간의 리포트가 이미 존재합니다.");
        }

        // 지난 주의 인사이트 조회
        List<HealthInsight> healthInsights = healthInsightRepository.findByFamilyAndAnalyzedAtBetween(family, periodStart, periodEnd);
        List<EmotionInsight> emotionInsights = emotionInsightRepository.findByFamilyAndAnalyzedAtBetween(family, periodStart, periodEnd);
        List<NeedsInsight> needsInsights = needsInsightRepository.findByFamilyAndAnalyzedAtBetween(family, periodStart, periodEnd);

        if (healthInsights.isEmpty() && emotionInsights.isEmpty() && needsInsights.isEmpty()) {
            log.warn("No insights found for weekly report. familyId: {}", familyId);
            throw new IllegalStateException("리포트 생성에 필요한 인사이트가 없습니다.");
        }

        // 각 인사이트 요약 생성
        String healthSummary = buildHealthSummary(healthInsights);
        String emotionSummary = buildEmotionSummary(emotionInsights);
        String needsSummary = buildNeedsSummary(needsInsights);

        // AI로 전체 요약 생성
        String overallSummary = generateOverallSummary(healthSummary, emotionSummary, needsSummary);

        // 리포트 생성
        WeeklyReport report = WeeklyReport.builder()
                .family(family)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .summary(overallSummary)
                .healthSummary(healthSummary)
                .emotionSummary(emotionSummary)
                .needsSummary(needsSummary)
                .generatedAt(LocalDateTime.now())
                .build();

        // 대화 치트키 생성 (AI)
        List<ConversationTip> tips = generateConversationTips(report, healthSummary, emotionSummary, needsSummary);
        tips.forEach(report::addConversationTip);

        weeklyReportRepository.save(report);

        log.info("Weekly report generated successfully. reportId: {}, familyId: {}", report.getId(), familyId);

        return report;
    }

    /**
     * 건강 인사이트 요약
     */
    private String buildHealthSummary(List<HealthInsight> insights) {
        if (insights.isEmpty()) {
            return "이번 주 건강 관련 특이사항이 없었습니다.";
        }

        return insights.stream()
                .map(insight -> String.format("• %s (심각도: %d/10)", insight.getSummary(), insight.getSeverity()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 감정 인사이트 요약
     */
    private String buildEmotionSummary(List<EmotionInsight> insights) {
        if (insights.isEmpty()) {
            return "이번 주 감정 상태 분석 결과가 없습니다.";
        }

        return insights.stream()
                .map(insight -> String.format("• %s (감정: %s, 점수: %d/10)",
                        insight.getDescription(), insight.getEmotionType(), insight.getEmotionScore()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 니즈 인사이트 요약
     */
    private String buildNeedsSummary(List<NeedsInsight> insights) {
        if (insights.isEmpty()) {
            return "이번 주 특별한 니즈가 파악되지 않았습니다.";
        }

        return insights.stream()
                .map(insight -> String.format("• [%s] %s (우선순위: %d/10)",
                        insight.getCategory(), insight.getContext(), insight.getPriority()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * AI로 전체 요약 생성
     */
    private String generateOverallSummary(String healthSummary, String emotionSummary, String needsSummary) {
        String prompt = String.format("""
                다음은 부모님의 이번 주 상태 분석 결과입니다.
                이를 종합하여 자녀에게 전달할 따뜻한 요약문을 2-3문장으로 작성해주세요.

                [건강 상태]
                %s

                [감정 상태]
                %s

                [니즈/필요사항]
                %s

                요약문만 작성해주세요 (다른 설명 없이):
                """, healthSummary, emotionSummary, needsSummary);

        return geminiClient.generate(prompt);
    }

    /**
     * 대화 치트키 생성 (AI)
     */
    private List<ConversationTip> generateConversationTips(WeeklyReport report,
                                                            String healthSummary,
                                                            String emotionSummary,
                                                            String needsSummary) {
        String prompt = String.format("""
                다음은 부모님의 이번 주 상태 분석 결과입니다.
                자녀가 부모님과 대화할 때 사용할 수 있는 대화 소재 3가지를 제안해주세요.

                [건강 상태]
                %s

                [감정 상태]
                %s

                [니즈/필요사항]
                %s

                다음 JSON 형식으로만 응답해주세요:
                {
                  "tips": [
                    {
                      "content": "대화 소재 내용 (예: '주말에 가신 등산 사진, 단풍이 참 예쁘네요')",
                      "priority": 1-10 사이의 정수,
                      "category": "카테고리 (건강 관심, 감정 케어, 취미 공유 중 하나)"
                    }
                  ]
                }

                대화 소재는 자연스럽고 따뜻하게 작성해주세요.
                """, healthSummary, emotionSummary, needsSummary);

        try {
            String aiResponse = geminiClient.generate(prompt);
            JsonNode jsonResponse = objectMapper.readTree(aiResponse);
            JsonNode tipsNode = jsonResponse.get("tips");

            List<TipDto> tipDtos = objectMapper.convertValue(tipsNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TipDto.class));

            return tipDtos.stream()
                    .map(dto -> ConversationTip.builder()
                            .report(report)
                            .content(dto.content())
                            .priority(dto.priority())
                            .category(dto.category())
                            .build())
                    .toList();

        } catch (Exception e) {
            log.error("Failed to generate conversation tips", e);
            // 실패 시 기본 팁 반환
            return List.of(
                    ConversationTip.builder()
                            .report(report)
                            .content("요즘 건강은 어떠세요? 불편한 곳은 없으신가요?")
                            .priority(5)
                            .category("건강 관심")
                            .build()
            );
        }
    }

    /**
     * 특정 가족의 최신 리포트 조회
     */
    public WeeklyReport getLatestReport(Long familyId) {
        log.info("Getting latest report. familyId: {}", familyId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        return weeklyReportRepository.findLatestByFamily(family)
                .orElseThrow(() -> new IllegalStateException("생성된 리포트가 없습니다."));
    }

    /**
     * 특정 가족의 모든 리포트 조회
     */
    public List<WeeklyReport> getAllReports(Long familyId) {
        log.info("Getting all reports. familyId: {}", familyId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        return weeklyReportRepository.findByFamilyOrderByGeneratedAtDesc(family);
    }

    /**
     * 대화 치트키 DTO
     */
    private record TipDto(String content, Integer priority, String category) {}
}
