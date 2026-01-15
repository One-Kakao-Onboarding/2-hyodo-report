package com.example.spring.insight.repository;

import com.example.spring.family.domain.Family;
import com.example.spring.insight.domain.NeedsInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 니즈 인사이트 Repository
 */
@Repository
public interface NeedsInsightRepository extends JpaRepository<NeedsInsight, Long> {

    /**
     * 특정 가족의 모든 니즈 인사이트 조회
     */
    List<NeedsInsight> findByFamilyOrderByAnalyzedAtDesc(Family family);

    /**
     * 특정 가족의 특정 기간 니즈 인사이트 조회
     */
    @Query("SELECT n FROM NeedsInsight n " +
            "WHERE n.family = :family " +
            "AND n.analyzedAt BETWEEN :start AND :end " +
            "ORDER BY n.analyzedAt DESC")
    List<NeedsInsight> findByFamilyAndAnalyzedAtBetween(
            @Param("family") Family family,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 가족의 고우선순위 니즈 인사이트 조회
     */
    @Query("SELECT n FROM NeedsInsight n " +
            "WHERE n.family = :family " +
            "AND n.priority >= 7 " +
            "ORDER BY n.priority DESC, n.analyzedAt DESC")
    List<NeedsInsight> findHighPriorityByFamily(@Param("family") Family family);

    /**
     * 특정 가족의 최근 N일 니즈 인사이트 조회
     */
    @Query("SELECT n FROM NeedsInsight n " +
            "WHERE n.family.id = :familyId " +
            "AND n.analyzedAt >= :since " +
            "ORDER BY n.analyzedAt DESC")
    List<NeedsInsight> findRecentByFamilyId(
            @Param("familyId") Long familyId,
            @Param("since") LocalDateTime since
    );

    /**
     * 특정 카테고리의 니즈 인사이트 조회
     */
    List<NeedsInsight> findByFamilyAndCategory(Family family, String category);
}
