package com.example.spring.conversation.service;

import com.example.spring.conversation.domain.Conversation;
import com.example.spring.conversation.domain.Message;
import com.example.spring.conversation.dto.*;
import com.example.spring.conversation.repository.ConversationRepository;
import com.example.spring.conversation.repository.MessageRepository;
import com.example.spring.family.domain.Family;
import com.example.spring.family.repository.FamilyRepository;
import com.example.spring.user.domain.User;
import com.example.spring.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 대화 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    /**
     * 대화방 생성
     */
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        log.info("Creating conversation. familyId: {}, name: {}", request.familyId(), request.name());

        Family family = familyRepository.findById(request.familyId())
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + request.familyId()));

        Conversation conversation = Conversation.builder()
                .family(family)
                .name(request.name())
                .build();

        conversationRepository.save(conversation);

        log.info("Conversation created successfully. conversationId: {}", conversation.getId());

        return ConversationResponse.from(conversation);
    }

    /**
     * 메시지 일괄 업로드
     */
    @Transactional
    public ConversationResponse uploadMessages(UploadMessagesRequest request) {
        log.info("Uploading messages. conversationId: {}, messageCount: {}",
                request.conversationId(), request.messages().size());

        Conversation conversation = conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다. conversationId: " + request.conversationId()));

        List<Message> messages = request.messages().stream()
                .map(dto -> createMessage(conversation, dto))
                .toList();

        messageRepository.saveAll(messages);

        log.info("Messages uploaded successfully. conversationId: {}, uploadedCount: {}",
                conversation.getId(), messages.size());

        return ConversationResponse.from(conversation);
    }

    /**
     * MessageDto를 Message 엔티티로 변환
     */
    private Message createMessage(Conversation conversation, MessageDto dto) {
        User sender = userRepository.findById(dto.senderId())
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다. userId: " + dto.senderId()));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .type(dto.type())
                .content(dto.content())
                .imageUrl(dto.imageUrl())
                .sentAt(dto.sentAt())
                .build();

        conversation.addMessage(message);

        return message;
    }

    /**
     * 특정 가족의 모든 대화방 조회
     */
    public List<ConversationResponse> getConversationsByFamily(Long familyId) {
        log.info("Getting conversations by family. familyId: {}", familyId);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("가족 그룹을 찾을 수 없습니다. familyId: " + familyId));

        List<Conversation> conversations = conversationRepository.findByFamilyOrderByUpdatedAtDesc(family);

        return conversations.stream()
                .map(ConversationResponse::fromWithoutMessages)
                .toList();
    }

    /**
     * 특정 대화방 상세 조회 (메시지 포함)
     */
    public ConversationResponse getConversation(Long conversationId) {
        log.info("Getting conversation detail. conversationId: {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다. conversationId: " + conversationId));

        return ConversationResponse.from(conversation);
    }

    /**
     * 특정 대화방의 특정 기간 메시지 조회
     */
    public List<MessageResponse> getMessagesByPeriod(Long conversationId, LocalDateTime start, LocalDateTime end) {
        log.info("Getting messages by period. conversationId: {}, start: {}, end: {}",
                conversationId, start, end);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다. conversationId: " + conversationId));

        List<Message> messages = messageRepository.findByConversationAndSentAtBetween(conversation, start, end);

        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }

    /**
     * 특정 가족의 최근 N일 메시지 조회 (AI 분석용)
     */
    public List<MessageResponse> getRecentMessagesByFamily(Long familyId, int days) {
        log.info("Getting recent messages by family. familyId: {}, days: {}", familyId, days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Message> messages = messageRepository.findRecentMessagesByFamilyId(familyId, since);

        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }

    /**
     * 이미지 분석이 안 된 메시지 조회
     */
    public List<MessageResponse> getUnanalyzedImageMessages() {
        log.info("Getting unanalyzed image messages");

        List<Message> messages = messageRepository.findUnanalyzedImageMessages();

        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }

    /**
     * 메시지에 이미지 설명 추가 (AI 분석 후)
     */
    @Transactional
    public void updateImageDescription(Long messageId, String description) {
        log.info("Updating image description. messageId: {}", messageId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다. messageId: " + messageId));

        message.updateImageDescription(description);

        log.info("Image description updated. messageId: {}", messageId);
    }
}
