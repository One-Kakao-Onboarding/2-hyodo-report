package com.example.spring.report.dto;

import com.example.spring.report.domain.ConversationTip;

/**
 * 대화 치트키 응답
 */
public record ConversationTipResponse(
        Long id,
        String content,
        Integer priority,
        String category
) {
    public static ConversationTipResponse from(ConversationTip tip) {
        return new ConversationTipResponse(
                tip.getId(),
                tip.getContent(),
                tip.getPriority(),
                tip.getCategory()
        );
    }
}
