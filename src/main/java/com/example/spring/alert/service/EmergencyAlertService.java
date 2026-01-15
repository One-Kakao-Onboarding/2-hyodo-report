package com.example.spring.alert.service;

import com.example.spring.ai.gemini.service.GeminiClient;
import com.example.spring.alert.domain.AlertType;
import com.example.spring.alert.domain.EmergencyAlert;
import com.example.spring.alert.repository.EmergencyAlertRepository;
import com.example.spring.conversation.domain.Message;
import com.example.spring.conversation.repository.ConversationRepository;
import com.example.spring.conversation.repository.MessageRepository;
import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 긴급 알림 서비스
 * 고위험 상황을 감지하고 즉시 알림 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyAlertService {

    private final GeminiClient geminiClient;
    private final FamilyRepository familyRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;

    // 고위험 키워드 정의
    private static final Map<AlertType, List<String>> RISK_KEYWORDS = Map.of(
            AlertType.HEALTH_EMERGENCY, Arrays.asList(
                    "응급실", "입원", "119", "구급차", "쓰러졌", "낙상", "넘어졌",
                    "호흡곤란", "가슴통증", "의식불명", "골절"
            ),
            AlertType.SAFETY_RISK, Arrays.asList(
                    "도둑", "사고", "화재", "가스", "도난", "위험", "112"
            ),
            AlertType.MENTAL_CRISIS, Arrays.asList(
                    "죽고싶", "자살", "포기", "살기싫", "외롭", "우울", "힘들"
            )
    );

    /**
     * 특정 가족의 최근 메시지를 분석하여 긴급 상황 감지
     */
    @Transactional
    public void detectEmergencies(Long familyId) {
        log.info("Detecting emergencies for family. familyId: {}", familyId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        // 최근 24시간 메시지 조회
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Message> recentMessages = messageRepository.findRecentMessagesByFamilyId(familyId, since);

        // 고위험 키워드 감지
        detectHighRiskKeywords(family, recentMessages);

        // 무응답 감지 (48시간)
        detectNoResponse(family);

        log.info("Emergency detection completed. familyId: {}", familyId);
    }

    /**
     * 고위험 키워드 감지
     */
    private void detectHighRiskKeywords(Family family, List<Message> messages) {
        for (Map.Entry<AlertType, List<String>> entry : RISK_KEYWORDS.entrySet()) {
            AlertType alertType = entry.getKey();
            List<String> keywords = entry.getValue();

            List<String> detectedKeywords = new ArrayList<>();
            List<String> matchedMessages = new ArrayList<>();

            // 키워드 매칭
            for (Message message : messages) {
                if (message.getContent() == null) continue;

                String content = message.getContent().toLowerCase();

                for (String keyword : keywords) {
                    if (content.contains(keyword.toLowerCase())) {
                        detectedKeywords.add(keyword);
                        matchedMessages.add(String.format("[%s] %s",
                                message.getSentAt().toLocalDate(),
                                message.getContent()));
                        break; // 한 메시지당 하나의 키워드만 카운트
                    }
                }
            }

            // 키워드가 감지된 경우
            if (!detectedKeywords.isEmpty()) {
                // 최근 1시간 내 동일 타입 알림이 없는 경우만 생성 (중복 방지)
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                if (!emergencyAlertRepository.existsByFamilyAndAlertTypeAndCreatedAtAfter(
                        family, alertType, oneHourAgo)) {

                    createEmergencyAlert(family, alertType, detectedKeywords, matchedMessages);
                }
            }
        }
    }

    /**
     * 무응답 감지 (48시간)
     */
    private void detectNoResponse(Family family) {
        LocalDateTime fortyEightHoursAgo = LocalDateTime.now().minusHours(48);

        // 가족의 모든 대화방에서 마지막 메시지 확인
        conversationRepository.findByFamily(family).forEach(conversation -> {
            Message lastMessage = messageRepository.findLastMessageByConversation(conversation);

            if (lastMessage != null && lastMessage.getSentAt().isBefore(fortyEightHoursAgo)) {
                // 최근 24시간 내 동일 타입 알림이 없는 경우만 생성
                LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
                if (!emergencyAlertRepository.existsByFamilyAndAlertTypeAndCreatedAtAfter(
                        family, AlertType.NO_RESPONSE, oneDayAgo)) {

                    createNoResponseAlert(family, lastMessage.getSentAt());
                }
            }
        });
    }

    /**
     * 긴급 알림 생성 (고위험 키워드)
     */
    private void createEmergencyAlert(Family family, AlertType alertType,
                                       List<String> detectedKeywords,
                                       List<String> matchedMessages) {
        log.warn("Emergency detected! familyId: {}, alertType: {}, keywords: {}",
                family.getId(), alertType, detectedKeywords);

        // AI로 상황 분석 (오탐 방지)
        String aiAnalysis = analyzeEmergencyContext(alertType, matchedMessages);

        String title = generateAlertTitle(alertType);
        String content = generateAlertContent(alertType, detectedKeywords, matchedMessages.size());

        EmergencyAlert alert = EmergencyAlert.builder()
                .family(family)
                .alertType(alertType)
                .title(title)
                .content(content)
                .severity(calculateSeverity(alertType, detectedKeywords.size()))
                .detectedKeywords(String.join(", ", detectedKeywords))
                .aiAnalysis(aiAnalysis)
                .build();

        emergencyAlertRepository.save(alert);

        log.info("Emergency alert created. alertId: {}, familyId: {}", alert.getId(), family.getId());

        // TODO: 알림톡 발송 (추후 구현)
        // notificationService.sendEmergencyNotification(alert);
    }

    /**
     * 무응답 알림 생성
     */
    private void createNoResponseAlert(Family family, LocalDateTime lastMessageTime) {
        log.warn("No response detected! familyId: {}, lastMessageTime: {}",
                family.getId(), lastMessageTime);

        long hoursSinceLastMessage = java.time.Duration.between(lastMessageTime, LocalDateTime.now()).toHours();

        String title = "🚨 부모님 무응답 알림";
        String content = String.format("부모님과 %d시간 동안 대화가 없었습니다. 안부를 확인해보세요.", hoursSinceLastMessage);

        EmergencyAlert alert = EmergencyAlert.builder()
                .family(family)
                .alertType(AlertType.NO_RESPONSE)
                .title(title)
                .content(content)
                .severity(7)
                .detectedKeywords("무응답")
                .aiAnalysis("48시간 이상 대화 기록이 없음")
                .build();

        emergencyAlertRepository.save(alert);

        log.info("No response alert created. alertId: {}, familyId: {}", alert.getId(), family.getId());
    }

    /**
     * AI로 긴급 상황 맥락 분석 (오탐 방지)
     */
    private String analyzeEmergencyContext(AlertType alertType, List<String> messages) {
        String prompt = String.format("""
                다음 대화에서 '%s' 타입의 긴급 상황이 감지되었습니다.
                이것이 실제 긴급 상황인지 분석해주세요.

                대화 내용:
                %s

                다음 형식으로 2-3문장으로 분석해주세요:
                - 실제 긴급 상황인지 여부
                - 상황의 심각도
                - 권장 조치사항
                """, alertType.name(), String.join("\n", messages));

        try {
            return geminiClient.generate(prompt);
        } catch (Exception e) {
            log.error("Failed to analyze emergency context", e);
            return "AI 분석 실패. 수동 확인 필요.";
        }
    }

    /**
     * 알림 제목 생성
     */
    private String generateAlertTitle(AlertType alertType) {
        return switch (alertType) {
            case HEALTH_EMERGENCY -> "🚨 건강 긴급 상황 감지";
            case SAFETY_RISK -> "⚠️ 안전 위험 감지";
            case MENTAL_CRISIS -> "💔 심리적 위기 감지";
            case NO_RESPONSE -> "🚨 부모님 무응답 알림";
        };
    }

    /**
     * 알림 내용 생성
     */
    private String generateAlertContent(AlertType alertType, List<String> keywords, int messageCount) {
        String keywordList = String.join(", ", keywords.stream().limit(3).toList());

        return switch (alertType) {
            case HEALTH_EMERGENCY -> String.format(
                    "최근 대화에서 건강 관련 긴급 키워드가 감지되었습니다.\n감지된 키워드: %s\n관련 메시지: %d건\n\n즉시 부모님께 연락하여 상황을 확인해주세요.",
                    keywordList, messageCount
            );
            case SAFETY_RISK -> String.format(
                    "최근 대화에서 안전 위험 키워드가 감지되었습니다.\n감지된 키워드: %s\n관련 메시지: %d건\n\n즉시 부모님께 연락하여 안전을 확인해주세요.",
                    keywordList, messageCount
            );
            case MENTAL_CRISIS -> String.format(
                    "최근 대화에서 심리적 어려움을 나타내는 표현이 감지되었습니다.\n감지된 키워드: %s\n관련 메시지: %d건\n\n부모님의 마음 상태를 확인하고 위로해주세요.",
                    keywordList, messageCount
            );
            default -> "긴급 상황이 감지되었습니다. 부모님께 연락해주세요.";
        };
    }

    /**
     * 심각도 계산
     */
    private Integer calculateSeverity(AlertType alertType, int keywordCount) {
        int baseSeverity = switch (alertType) {
            case HEALTH_EMERGENCY -> 9;
            case SAFETY_RISK -> 8;
            case MENTAL_CRISIS -> 7;
            case NO_RESPONSE -> 7;
        };

        // 키워드가 많을수록 심각도 증가 (최대 10)
        return Math.min(10, baseSeverity + (keywordCount / 2));
    }

    /**
     * 특정 가족의 미확인 알림 조회
     */
    public List<EmergencyAlert> getUnacknowledgedAlerts(Long familyId) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        return emergencyAlertRepository.findByFamilyAndAcknowledgedFalseOrderByCreatedAtDesc(family);
    }

    /**
     * 알림 확인 처리
     */
    @Transactional
    public void acknowledgeAlert(Long alertId) {
        EmergencyAlert alert = emergencyAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다. alertId: " + alertId));

        alert.acknowledge();
        log.info("Alert acknowledged. alertId: {}", alertId);
    }
}
