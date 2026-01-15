package com.example.spring.conversation.domain;

/**
 * 메시지 타입
 */
public enum MessageType {
    /**
     * 텍스트 메시지
     */
    TEXT,

    /**
     * 이미지 메시지 (Gemini Vision API 분석 대상)
     */
    IMAGE
}
