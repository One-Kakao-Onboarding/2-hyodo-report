package com.example.spring.statistics.service;

import com.example.spring.statistics.dto.ConversationStatsResponse;
import com.example.spring.statistics.dto.MessageStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 통계 계산 서비스
 * 프론트엔드에서 하던 통계 계산 로직을 백엔드로 이동
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    /**
     * 대화 통계 계산
     * 프론트엔드의 conversation statistics 로직을 백엔드로 이동
     */
    public ConversationStatsResponse calculateConversationStats(
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<MessageRecord> messages,
            List<MessageRecord> previousPeriodMessages) {

        // 기간 계산
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 현재 기간 통계
        int totalMessages = messages.size();
        double averagePerDay = daysBetween > 0 ? (double) totalMessages / daysBetween : 0;

        // 이전 기간 통계
        int previousTotalMessages = previousPeriodMessages.size();
        double previousAveragePerDay = daysBetween > 0
                ? (double) previousTotalMessages / daysBetween
                : 0;

        // 트렌드 계산
        double changePercent = previousTotalMessages > 0
                ? ((double) (totalMessages - previousTotalMessages) / previousTotalMessages) * 100
                : 0;

        String trend = determineTrend(changePercent);
        String trendDescription = generateTrendDescription(changePercent);

        // 일별 메시지 분포
        Map<LocalDate, Integer> dailyDistribution = calculateDailyDistribution(messages);

        // 시간대별 활동 패턴
        Map<String, Integer> hourlyPattern = calculateHourlyPattern(messages);

        // 가장 활발한 대화 시간
        String peakHour = findPeakHour(hourlyPattern);

        return ConversationStatsResponse.builder()
                .totalMessages(totalMessages)
                .averagePerDay(Math.round(averagePerDay * 10.0) / 10.0)
                .trend(trend)
                .trendDescription(trendDescription)
                .changePercent(Math.round(changePercent * 10.0) / 10.0)
                .dailyDistribution(dailyDistribution)
                .hourlyPattern(hourlyPattern)
                .peakHour(peakHour)
                .periodDays((int) daysBetween)
                .previousTotalMessages(previousTotalMessages)
                .previousAveragePerDay(Math.round(previousAveragePerDay * 10.0) / 10.0)
                .build();
    }

    /**
     * 트렌드 판단
     */
    private String determineTrend(double changePercent) {
        if (changePercent > 10) return "UP";
        if (changePercent < -10) return "DOWN";
        return "STABLE";
    }

    /**
     * 트렌드 설명 생성
     */
    private String generateTrendDescription(double changePercent) {
        if (changePercent > 20) {
            return String.format("대화량이 %.1f%% 크게 증가했습니다", changePercent);
        } else if (changePercent > 10) {
            return String.format("대화량이 %.1f%% 증가했습니다", changePercent);
        } else if (changePercent < -20) {
            return String.format("대화량이 %.1f%% 크게 감소했습니다", Math.abs(changePercent));
        } else if (changePercent < -10) {
            return String.format("대화량이 %.1f%% 감소했습니다", Math.abs(changePercent));
        } else {
            return "대화량이 평소와 비슷합니다";
        }
    }

    /**
     * 일별 메시지 분포 계산
     */
    private Map<LocalDate, Integer> calculateDailyDistribution(List<MessageRecord> messages) {
        return messages.stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.timestamp().toLocalDate(),
                        Collectors.summingInt(msg -> 1)
                ));
    }

    /**
     * 시간대별 활동 패턴 계산
     */
    private Map<String, Integer> calculateHourlyPattern(List<MessageRecord> messages) {
        Map<String, Integer> pattern = new LinkedHashMap<>();

        // 시간대 초기화 (00:00-23:59)
        for (int i = 0; i < 24; i++) {
            pattern.put(String.format("%02d:00", i), 0);
        }

        // 메시지 시간별 카운팅
        for (MessageRecord message : messages) {
            int hour = message.timestamp().getHour();
            String hourKey = String.format("%02d:00", hour);
            pattern.merge(hourKey, 1, Integer::sum);
        }

        return pattern;
    }

    /**
     * 가장 활발한 시간 찾기
     */
    private String findPeakHour(Map<String, Integer> hourlyPattern) {
        return hourlyPattern.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey())
                .orElse("알 수 없음");
    }

    /**
     * 메시지 길이 통계
     */
    public MessageStatistics calculateMessageStatistics(List<MessageRecord> messages) {
        if (messages.isEmpty()) {
            return MessageStatistics.builder()
                    .averageLength(0.0)
                    .minLength(0)
                    .maxLength(0)
                    .totalCharacters(0)
                    .build();
        }

        int totalChars = messages.stream()
                .mapToInt(msg -> msg.content().length())
                .sum();

        int minLength = messages.stream()
                .mapToInt(msg -> msg.content().length())
                .min()
                .orElse(0);

        int maxLength = messages.stream()
                .mapToInt(msg -> msg.content().length())
                .max()
                .orElse(0);

        double averageLength = (double) totalChars / messages.size();

        // 단답형 응답 비율 (10자 이하)
        long shortAnswers = messages.stream()
                .filter(msg -> msg.content().length() <= 10)
                .count();

        double shortAnswerRatio = (double) shortAnswers / messages.size() * 100;

        return MessageStatistics.builder()
                .averageLength(Math.round(averageLength * 10.0) / 10.0)
                .minLength(minLength)
                .maxLength(maxLength)
                .totalCharacters(totalChars)
                .shortAnswerCount((int) shortAnswers)
                .shortAnswerRatio(Math.round(shortAnswerRatio * 10.0) / 10.0)
                .build();
    }

    /**
     * 메시지 레코드 (간단한 DTO)
     */
    public record MessageRecord(
            String content,
            LocalDateTime timestamp,
            String senderId
    ) {}
}
