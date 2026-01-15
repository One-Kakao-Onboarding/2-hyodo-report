package com.example.spring.statistics.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record ConversationStatsResponse(
        int totalMessages,
        double averagePerDay,
        String trend,                           // UP, DOWN, STABLE
        String trendDescription,
        double changePercent,
        Map<LocalDate, Integer> dailyDistribution,
        Map<String, Integer> hourlyPattern,
        String peakHour,
        int periodDays,
        int previousTotalMessages,
        double previousAveragePerDay
) {
}
