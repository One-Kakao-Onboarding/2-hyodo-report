package com.example.spring.conversation.repository;

import com.example.spring.conversation.domain.Conversation;
import com.example.spring.family.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 대화방 Repository
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * 특정 가족의 모든 대화방 조회
     */
    List<Conversation> findByFamily(Family family);

    /**
     * 특정 가족의 대화방을 최근 메시지 순으로 조회
     */
    @Query("SELECT c FROM Conversation c WHERE c.family = :family ORDER BY c.updatedAt DESC")
    List<Conversation> findByFamilyOrderByUpdatedAtDesc(@Param("family") Family family);

    /**
     * 특정 가족의 특정 이름을 가진 대화방 조회
     */
    Optional<Conversation> findByFamilyAndName(Family family, String name);
}
