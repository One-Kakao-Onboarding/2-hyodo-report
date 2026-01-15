package com.example.spring.insight.repository;

import com.example.spring.family.domain.Family;
import com.example.spring.insight.domain.EmotionInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감정 인사이트 Repository
 */
@Repository
public interface EmotionInsightRepository extends JpaRepository<EmotionInsight, Long> {

    /**
     * 특정 가족의 모든 감정 인사이트 조회
     */
    List<EmotionInsight> findByFamilyOrderByAnalyzedAtDesc(Family family);

    /**
     * 특정 가족의 특정 기간 감정 인사이트 조회
     */
    @Query("SELECT e FROM EmotionInsight e " +
            "WHERE e.family = :family " +
            "AND e.analyzedAt BETWEEN :start AND :end " +
            "ORDER BY e.analyzedAt DESC")
    List<EmotionInsight> findByFamilyAndAnalyzedAtBetween(
            @Param("family") Family family,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 가족의 부정적 감정 인사이트 조회
     */
    @Query("SELECT e FROM EmotionInsight e " +
            "WHERE e.family = :family " +
            "AND e.emotionScore < -3 " +
            "ORDER BY e.analyzedAt DESC")
    List<EmotionInsight> findNegativeByFamily(@Param("family") Family family);

    /**
     * 특정 가족의 고위험 감정 인사이트 조회
     */
    @Query("SELECT e FROM EmotionInsight e " +
            "WHERE e.family = :family " +
            "AND (e.emotionScore <= -7 OR e.emotionType LIKE '%우울%' OR e.emotionType LIKE '%외로움%') " +
            "ORDER BY e.analyzedAt DESC")
    List<EmotionInsight> findHighRiskByFamily(@Param("family") Family family);

    /**
     * 특정 가족의 최근 N일 감정 인사이트 조회
     */
    @Query("SELECT e FROM EmotionInsight e " +
            "WHERE e.family.id = :familyId " +
            "AND e.analyzedAt >= :since " +
            "ORDER BY e.analyzedAt DESC")
    List<EmotionInsight> findRecentByFamilyId(
            @Param("familyId") Long familyId,
            @Param("since") LocalDateTime since
    );
}
