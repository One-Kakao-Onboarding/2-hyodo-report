package com.example.spring.insight.repository;

import com.example.spring.family.domain.Family;
import com.example.spring.insight.domain.HealthInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 건강 인사이트 Repository
 */
@Repository
public interface HealthInsightRepository extends JpaRepository<HealthInsight, Long> {

    /**
     * 특정 가족의 모든 건강 인사이트 조회
     */
    List<HealthInsight> findByFamilyOrderByAnalyzedAtDesc(Family family);

    /**
     * 특정 가족의 특정 기간 건강 인사이트 조회
     */
    @Query("SELECT h FROM HealthInsight h " +
            "WHERE h.family = :family " +
            "AND h.analyzedAt BETWEEN :start AND :end " +
            "ORDER BY h.analyzedAt DESC")
    List<HealthInsight> findByFamilyAndAnalyzedAtBetween(
            @Param("family") Family family,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 가족의 고위험 건강 인사이트 조회
     */
    @Query("SELECT h FROM HealthInsight h " +
            "WHERE h.family = :family " +
            "AND h.severity >= 7 " +
            "ORDER BY h.analyzedAt DESC")
    List<HealthInsight> findHighRiskByFamily(@Param("family") Family family);

    /**
     * 특정 가족의 최근 N일 건강 인사이트 조회
     */
    @Query("SELECT h FROM HealthInsight h " +
            "WHERE h.family.id = :familyId " +
            "AND h.analyzedAt >= :since " +
            "ORDER BY h.analyzedAt DESC")
    List<HealthInsight> findRecentByFamilyId(
            @Param("familyId") Long familyId,
            @Param("since") LocalDateTime since
    );
}
