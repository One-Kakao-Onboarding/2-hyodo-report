package com.example.spring.conversation.repository;

import com.example.spring.conversation.domain.Conversation;
import com.example.spring.conversation.domain.Message;
import com.example.spring.conversation.domain.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메시지 Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 특정 대화방의 모든 메시지를 시간 순으로 조회
     */
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

    /**
     * 특정 대화방의 특정 기간 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation " +
            "AND m.sentAt BETWEEN :start AND :end ORDER BY m.sentAt ASC")
    List<Message> findByConversationAndSentAtBetween(
            @Param("conversation") Conversation conversation,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 가족의 특정 기간 모든 메시지 조회 (분석용)
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.family.id = :familyId " +
            "AND m.sentAt BETWEEN :start AND :end " +
            "ORDER BY m.sentAt ASC")
    List<Message> findByFamilyIdAndSentAtBetween(
            @Param("familyId") Long familyId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 가족의 최근 N일 메시지 조회
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.family.id = :familyId " +
            "AND m.sentAt >= :since " +
            "ORDER BY m.sentAt ASC")
    List<Message> findRecentMessagesByFamilyId(
            @Param("familyId") Long familyId,
            @Param("since") LocalDateTime since
    );

    /**
     * 이미지 분석이 안 된 이미지 메시지 조회
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.type = 'IMAGE' " +
            "AND m.imageDescription IS NULL " +
            "ORDER BY m.sentAt ASC")
    List<Message> findUnanalyzedImageMessages();

    /**
     * 특정 대화방의 마지막 메시지 조회
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation = :conversation " +
            "ORDER BY m.sentAt DESC " +
            "LIMIT 1")
    Message findLastMessageByConversation(@Param("conversation") Conversation conversation);

    /**
     * 특정 가족의 특정 타입 메시지 조회
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.family.id = :familyId " +
            "AND m.type = :type " +
            "AND m.sentAt BETWEEN :start AND :end " +
            "ORDER BY m.sentAt ASC")
    List<Message> findByFamilyIdAndTypeAndSentAtBetween(
            @Param("familyId") Long familyId,
            @Param("type") MessageType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
