package com.example.spring.conversation.controller;

import com.example.spring.common.dto.ApiResponse;
import com.example.spring.conversation.dto.ConversationResponse;
import com.example.spring.conversation.dto.CreateConversationRequest;
import com.example.spring.conversation.dto.MessageResponse;
import com.example.spring.conversation.dto.UploadMessagesRequest;
import com.example.spring.conversation.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 대화 데이터 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 대화방 생성
     * POST /api/conversations
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(
            @Valid @RequestBody CreateConversationRequest request) {

        ConversationResponse response = conversationService.createConversation(request);

        return ResponseEntity.ok(ApiResponse.success(response, "대화방이 생성되었습니다."));
    }

    /**
     * 메시지 일괄 업로드
     * POST /api/conversations/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ConversationResponse>> uploadMessages(
            @Valid @RequestBody UploadMessagesRequest request) {

        ConversationResponse response = conversationService.uploadMessages(request);

        return ResponseEntity.ok(ApiResponse.success(response,
                String.format("%d개의 메시지가 업로드되었습니다.", request.messages().size())));
    }

    /**
     * 특정 가족의 모든 대화방 조회
     * GET /api/conversations?familyId={familyId}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversationsByFamily(
            @RequestParam Long familyId) {

        List<ConversationResponse> response = conversationService.getConversationsByFamily(familyId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 대화방 상세 조회 (메시지 포함)
     * GET /api/conversations/{conversationId}
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable Long conversationId) {

        ConversationResponse response = conversationService.getConversation(conversationId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 대화방의 특정 기간 메시지 조회
     * GET /api/conversations/{conversationId}/messages?start={start}&end={end}
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessagesByPeriod(
            @PathVariable Long conversationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<MessageResponse> response = conversationService.getMessagesByPeriod(conversationId, start, end);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 가족의 최근 N일 메시지 조회
     * GET /api/conversations/family/{familyId}/recent?days={days}
     */
    @GetMapping("/family/{familyId}/recent")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getRecentMessages(
            @PathVariable Long familyId,
            @RequestParam(defaultValue = "7") int days) {

        List<MessageResponse> response = conversationService.getRecentMessagesByFamily(familyId, days);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 이미지 분석이 안 된 메시지 조회 (관리자용)
     * GET /api/conversations/unanalyzed-images
     */
    @GetMapping("/unanalyzed-images")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getUnanalyzedImageMessages() {

        List<MessageResponse> response = conversationService.getUnanalyzedImageMessages();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
