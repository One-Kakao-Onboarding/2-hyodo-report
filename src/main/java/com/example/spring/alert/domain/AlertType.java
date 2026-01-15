package com.example.spring.alert.domain;

/**
 * 긴급 알림 타입
 */
public enum AlertType {
    /**
     * 건강 긴급 (응급실, 입원, 낙상 등)
     */
    HEALTH_EMERGENCY,

    /**
     * 안전 위험 (도둑, 사고 등)
     */
    SAFETY_RISK,

    /**
     * 심리적 위기 (우울, 외로움 등)
     */
    MENTAL_CRISIS,

    /**
     * 무응답 (48시간 이상 대화 없음)
     */
    NO_RESPONSE
}
