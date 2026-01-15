package com.example.spring.recommendation.service;

import com.example.spring.recommendation.dto.ConversationTipResponse;
import com.example.spring.recommendation.dto.ProductSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 시스템 서비스
 * 대화 팁, 제품 추천 등 프론트엔드에서 하던 추천 로직을 백엔드로 이동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    /**
     * 대화 주제 및 질문 추천
     * 프론트엔드의 conversationKit 로직을 백엔드로 이동
     */
    public ConversationTipResponse generateConversationTips(
            List<String> recentKeywords,
            List<String> recentTopics,
            String emotionStatus) {

        List<String> suggestedQuestions = new ArrayList<>();
        List<String> topics = new ArrayList<>();
        int priority = calculatePriority(emotionStatus);

        // 감정 상태에 따른 대화 전략
        if ("CONCERNED".equals(emotionStatus)) {
            suggestedQuestions.add("요즘 어떠세요? 걱정되는 일이 있으신가요?");
            suggestedQuestions.add("힘든 일이 있으면 언제든 말씀해주세요");
            priority = 10; // 최우선
        }

        // 키워드 기반 질문 생성
        for (String keyword : recentKeywords) {
            String question = generateQuestionFromKeyword(keyword);
            if (question != null) {
                suggestedQuestions.add(question);
                topics.add(keyword);
            }
        }

        // 최근 화제 기반 질문
        for (String topic : recentTopics) {
            suggestedQuestions.add(String.format("%s 이야기 더 들려주세요", topic));
        }

        // 중복 제거 및 상위 5개만 선택
        List<String> uniqueQuestions = suggestedQuestions.stream()
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        return ConversationTipResponse.builder()
                .questions(uniqueQuestions)
                .topics(topics)
                .priority(priority)
                .category(determinCategory(emotionStatus))
                .build();
    }

    /**
     * 키워드에서 질문 생성
     */
    private String generateQuestionFromKeyword(String keyword) {
        Map<String, String> keywordToQuestion = Map.of(
                "무릎", "무릎은 좀 어떠세요? 많이 불편하신가요?",
                "병원", "병원 다녀오셨어요? 어떻게 되셨나요?",
                "손주", "손주들은 잘 크고 있나요? 보고 싶으시죠?",
                "친구", "친구분들은 요즘 어떻게 지내세요?",
                "날씨", "날씨가 많이 추워졌는데 괜찮으세요?"
        );

        return keywordToQuestion.get(keyword);
    }

    /**
     * 우선순위 계산
     */
    private int calculatePriority(String emotionStatus) {
        return switch (emotionStatus) {
            case "CONCERNED" -> 10;
            case "NEUTRAL" -> 5;
            case "POSITIVE" -> 3;
            default -> 1;
        };
    }

    /**
     * 카테고리 결정
     */
    private String determinCategory(String emotionStatus) {
        return switch (emotionStatus) {
            case "CONCERNED" -> "정서적 지원";
            case "NEUTRAL" -> "일상 대화";
            case "POSITIVE" -> "긍정 강화";
            default -> "일반";
        };
    }

    /**
     * 니즈 기반 제품 추천
     * 프론트엔드의 needsHunter 로직을 백엔드로 이동
     */
    public List<ProductSuggestion> recommendProducts(List<String> needs, List<String> keywords) {
        List<ProductSuggestion> suggestions = new ArrayList<>();

        // 니즈 분석 및 제품 매칭
        for (String need : needs) {
            ProductSuggestion suggestion = matchProductToNeed(need, keywords);
            if (suggestion != null) {
                suggestions.add(suggestion);
            }
        }

        // 키워드 기반 추가 추천
        for (String keyword : keywords) {
            ProductSuggestion suggestion = matchProductToKeyword(keyword);
            if (suggestion != null && !suggestions.contains(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // 우선순위 정렬 및 상위 5개 반환
        return suggestions.stream()
                .sorted(Comparator.comparingInt(ProductSuggestion::priority).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 니즈에 맞는 제품 매칭
     */
    private ProductSuggestion matchProductToNeed(String need, List<String> keywords) {
        String needLower = need.toLowerCase();

        if (needLower.contains("무릎") || needLower.contains("관절")) {
            return new ProductSuggestion(
                    "관절 건강 제품",
                    need,
                    "MSM 관절 영양제",
                    "https://shopping.example.com/joint-supplement",
                    10,
                    "건강"
            );
        }

        if (needLower.contains("잠") || needLower.contains("수면")) {
            return new ProductSuggestion(
                    "숙면 유도 아이템",
                    need,
                    "라벤더 아로마 세트",
                    "https://shopping.example.com/sleep-aid",
                    9,
                    "웰빙"
            );
        }

        if (needLower.contains("밥") || needLower.contains("식사")) {
            return new ProductSuggestion(
                    "식욕 증진 보양식",
                    need,
                    "한방 보양식 세트",
                    "https://shopping.example.com/health-food",
                    8,
                    "식품"
            );
        }

        return null;
    }

    /**
     * 키워드 기반 제품 매칭
     */
    private ProductSuggestion matchProductToKeyword(String keyword) {
        Map<String, ProductSuggestion> keywordProducts = Map.of(
                "등산", new ProductSuggestion(
                        "등산용품",
                        "등산 관련 대화",
                        "기능성 등산화",
                        "https://shopping.example.com/hiking-shoes",
                        7,
                        "레저"
                ),
                "요리", new ProductSuggestion(
                        "주방용품",
                        "요리 관련 대화",
                        "실버세대용 주방기구",
                        "https://shopping.example.com/kitchen",
                        6,
                        "생활"
                ),
                "책", new ProductSuggestion(
                        "독서용품",
                        "독서 관련 대화",
                        "큰 활자 도서 추천",
                        "https://shopping.example.com/books",
                        5,
                        "문화"
                )
        );

        return keywordProducts.get(keyword);
    }
}
