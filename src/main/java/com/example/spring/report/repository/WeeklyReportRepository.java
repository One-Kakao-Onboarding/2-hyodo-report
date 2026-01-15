package com.example.spring.report.repository;

import com.example.spring.family.domain.Family;
import com.example.spring.report.domain.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 주간 리포트 Repository
 */
@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    /**
     * 특정 가족의 모든 리포트 조회 (최신순)
     */
    List<WeeklyReport> findByFamilyOrderByGeneratedAtDesc(Family family);

    /**
     * 특정 가족의 최신 리포트 조회
     */
    @Query("SELECT w FROM WeeklyReport w WHERE w.family = :family ORDER BY w.generatedAt DESC LIMIT 1")
    Optional<WeeklyReport> findLatestByFamily(@Param("family") Family family);

    /**
     * 특정 가족의 특정 기간 리포트 조회
     */
    @Query("SELECT w FROM WeeklyReport w " +
            "WHERE w.family = :family " +
            "AND w.generatedAt BETWEEN :start AND :end " +
            "ORDER BY w.generatedAt DESC")
    List<WeeklyReport> findByFamilyAndGeneratedAtBetween(
            @Param("family") Family family,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 기간에 리포트가 이미 생성되었는지 확인
     */
    @Query("SELECT COUNT(w) > 0 FROM WeeklyReport w " +
            "WHERE w.family = :family " +
            "AND w.periodStart = :periodStart " +
            "AND w.periodEnd = :periodEnd")
    boolean existsByFamilyAndPeriod(
            @Param("family") Family family,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd
    );
}
