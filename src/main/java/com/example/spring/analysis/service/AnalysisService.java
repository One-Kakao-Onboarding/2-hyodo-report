package com.example.spring.analysis.service;

import com.example.spring.ai.gemini.service.GeminiClient;
import com.example.spring.conversation.dto.MessageResponse;
import com.example.spring.conversation.service.ConversationService;
import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import com.example.spring.insight.domain.EmotionInsight;
import com.example.spring.insight.domain.HealthInsight;
import com.example.spring.insight.domain.NeedsInsight;
import com.example.spring.insight.repository.EmotionInsightRepository;
import com.example.spring.insight.repository.HealthInsightRepository;
import com.example.spring.insight.repository.NeedsInsightRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
 * AI 분석 서비스
 * Gemini AI를 사용하여 대화 데이터를 분석하고 인사이트 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final GeminiClient geminiClient;
    private final ConversationService conversationService;
    private final FamilyRepository familyRepository;
    private final HealthInsightRepository healthInsightRepository;
    private final EmotionInsightRepository emotionInsightRepository;
    private final NeedsInsightRepository needsInsightRepository;
    private final ObjectMapper objectMapper;

    /**
     * 특정 가족의 최근 N일 대화를 전체 분석 (3가지 분석 수행)
     */
    @Transactional
    public void analyzeFamily(Long familyId, int days) {
        log.info("Starting comprehensive analysis for family. familyId: {}, days: {}", familyId, days);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        // 최근 N일 메시지 조회
        List<MessageResponse> messages = conversationService.getRecentMessagesByFamily(familyId, days);

        if (messages.isEmpty()) {
            log.warn("No messages found for analysis. familyId: {}", familyId);
            return;
        }

        // 대화 내용 텍스트로 변환
        String conversationText = buildConversationText(messages);

        // 3가지 분석 병렬 수행
        analyzeHealth(family, conversationText);
        analyzeEmotion(family, conversationText);
        analyzeNeeds(family, conversationText);

        log.info("Comprehensive analysis completed. familyId: {}", familyId);
    }

    /**
     * 건강 스캐너 분석
     */
    @Transactional
    public void analyzeHealth(Family family, String conversationText) {
        log.info("Analyzing health for family. familyId: {}", family.getId());

        String prompt = buildHealthAnalysisPrompt(conversationText);

        try {
            String aiResponse = geminiClient.generate(prompt);
            JsonNode jsonResponse = objectMapper.readTree(aiResponse);

            HealthInsight insight = HealthInsight.builder()
                    .family(family)
                    .keywords(jsonResponse.get("keywords").toString())
                    .severity(jsonResponse.get("severity").asInt())
                    .summary(jsonResponse.get("summary").asText())
                    .recommendation(jsonResponse.get("recommendation").asText())
                    .analyzedAt(LocalDateTime.now())
                    .build();

            healthInsightRepository.save(insight);

            log.info("Health analysis saved. familyId: {}, severity: {}", family.getId(), insight.getSeverity());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse health analysis JSON response", e);
            throw new RuntimeException("건강 분석 JSON 파싱 실패", e);
        }
    }

    /**
     * 감정 분석
     */
    @Transactional
    public void analyzeEmotion(Family family, String conversationText) {
        log.info("Analyzing emotion for family. familyId: {}", family.getId());

        String prompt = buildEmotionAnalysisPrompt(conversationText);

        try {
            String aiResponse = geminiClient.generate(prompt);
            JsonNode jsonResponse = objectMapper.readTree(aiResponse);

            EmotionInsight insight = EmotionInsight.builder()
                    .family(family)
                    .emotionType(jsonResponse.get("emotionType").asText())
                    .emotionScore(jsonResponse.get("emotionScore").asInt())
                    .description(jsonResponse.get("description").asText())
                    .conversationTips(jsonResponse.get("conversationTips").toString())
                    .analyzedAt(LocalDateTime.now())
                    .build();

            emotionInsightRepository.save(insight);

            log.info("Emotion analysis saved. familyId: {}, emotionType: {}, score: {}",
                    family.getId(), insight.getEmotionType(), insight.getEmotionScore());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse emotion analysis JSON response", e);
            throw new RuntimeException("감정 분석 JSON 파싱 실패", e);
        }
    }

    /**
     * 니즈 발굴 분석
     */
    @Transactional
    public void analyzeNeeds(Family family, String conversationText) {
        log.info("Analyzing needs for family. familyId: {}", family.getId());

        String prompt = buildNeedsAnalysisPrompt(conversationText);

        try {
            String aiResponse = geminiClient.generate(prompt);
            JsonNode jsonResponse = objectMapper.readTree(aiResponse);

            NeedsInsight insight = NeedsInsight.builder()
                    .family(family)
                    .category(jsonResponse.get("category").asText())
                    .items(jsonResponse.get("items").toString())
                    .priority(jsonResponse.get("priority").asInt())
                    .context(jsonResponse.get("context").asText())
                    .recommendations(jsonResponse.get("recommendations").toString())
                    .analyzedAt(LocalDateTime.now())
                    .build();

            needsInsightRepository.save(insight);

            log.info("Needs analysis saved. familyId: {}, category: {}, priority: {}",
                    family.getId(), insight.getCategory(), insight.getPriority());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse needs analysis JSON response", e);
            throw new RuntimeException("니즈 분석 JSON 파싱 실패", e);
        }
    }

    /**
     * 메시지 목록을 텍스트로 변환
     */
    private String buildConversationText(List<MessageResponse> messages) {
        return messages.stream()
                .filter(msg -> msg.type().name().equals("TEXT"))
                .map(msg -> String.format("[%s] %s: %s",
                        msg.sentAt().toLocalDate(),
                        msg.senderNickname(),
                        msg.content()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 건강 분석 프롬프트 생성
     */
    private String buildHealthAnalysisPrompt(String conversationText) {
        return String.format("""
                다음은 부모님과 자녀 간의 대화 내용입니다.
                대화에서 건강 관련 키워드와 이슈를 추출하여 JSON 형식으로 분석해주세요.

                대화 내용:
                %s

                다음 JSON 형식으로만 응답해주세요 (다른 텍스트 없이):
                {
                  "keywords": ["키워드1", "키워드2", ...],
                  "severity": 1-10 사이의 정수 (높을수록 심각),
                  "summary": "건강 상태 요약 (2-3문장)",
                  "recommendation": "권장 조치사항 (2-3문장)"
                }

                분석 기준:
                - 통증, 질병, 불편함 언급 파악
                - "무릎 아파", "당뇨", "혈압" 등 건강 키워드 추출
                - severity: 응급실/입원(9-10), 심각한 통증(7-8), 중간 통증(4-6), 경미(1-3)
                """, conversationText);
    }

    /**
     * 감정 분석 프롬프트 생성
     */
    private String buildEmotionAnalysisPrompt(String conversationText) {
        return String.format("""
                다음은 부모님과 자녀 간의 대화 내용입니다.
                부모님의 감정 상태를 분석하여 JSON 형식으로 응답해주세요.

                대화 내용:
                %s

                다음 JSON 형식으로만 응답해주세요 (다른 텍스트 없이):
                {
                  "emotionType": "주요 감정 (예: 긍정, 우울, 외로움, 불안, 평온)",
                  "emotionScore": -10 ~ +10 사이의 정수 (음수는 부정, 양수는 긍정),
                  "description": "감정 상태 설명 (2-3문장)",
                  "conversationTips": ["대화 소재 제안1", "대화 소재 제안2", "대화 소재 제안3"]
                }

                분석 기준:
                - "외롭다", "죽고싶어", "포기" 등 부정 키워드 → 낮은 점수
                - "좋아", "행복해", "즐거워" 등 긍정 키워드 → 높은 점수
                - conversationTips: 부모님과 대화할 수 있는 소재 3가지 제안
                """, conversationText);
    }

    /**
     * 니즈 분석 프롬프트 생성
     */
    private String buildNeedsAnalysisPrompt(String conversationText) {
        return String.format("""
                다음은 부모님과 자녀 간의 대화 내용입니다.
                부모님의 숨은 니즈(필요 물품, 서비스)를 파악하여 JSON 형식으로 응답해주세요.

                대화 내용:
                %s

                다음 JSON 형식으로만 응답해주세요 (다른 텍스트 없이):
                {
                  "category": "카테고리 (건강/의료, 생활용품, 식품, 여가, 기타 중 하나)",
                  "items": ["항목1", "항목2", ...],
                  "priority": 1-10 사이의 정수 (높을수록 시급),
                  "context": "니즈 발생 맥락 (2-3문장)",
                  "recommendations": ["추천 상품/서비스1", "추천 상품/서비스2", "추천 상품/서비스3"]
                }

                분석 기준:
                - "필요해", "사고 싶어", "있으면 좋겠어" 등 구매 의도 파악
                - "무릎 아파" → 온열 찜질기, 파스 추천
                - "입맛 없어" → 영양제, 소화제, 맛있는 음식 추천
                - priority: 건강 관련(8-10), 생활 불편(5-7), 선호(1-4)
                """, conversationText);
    }
}
