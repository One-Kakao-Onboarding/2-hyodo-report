package com.example.spring.alert.repository;

import com.example.spring.alert.domain.AlertType;
import com.example.spring.alert.domain.EmergencyAlert;
import com.example.spring.family.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 긴급 알림 Repository
 */
@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    /**
     * 특정 가족의 모든 긴급 알림 조회 (최신순)
     */
    List<EmergencyAlert> findByFamilyOrderByCreatedAtDesc(Family family);

    /**
     * 특정 가족의 미확인 알림 조회
     */
    List<EmergencyAlert> findByFamilyAndAcknowledgedFalseOrderByCreatedAtDesc(Family family);

    /**
     * 특정 가족의 특정 타입 알림 조회
     */
    List<EmergencyAlert> findByFamilyAndAlertType(Family family, AlertType alertType);

    /**
     * 특정 가족의 고위험 알림 조회
     */
    @Query("SELECT e FROM EmergencyAlert e " +
            "WHERE e.family = :family " +
            "AND e.severity >= 8 " +
            "ORDER BY e.createdAt DESC")
    List<EmergencyAlert> findHighSeverityByFamily(@Param("family") Family family);

    /**
     * 특정 기간 내 생성된 알림 조회
     */
    @Query("SELECT e FROM EmergencyAlert e " +
            "WHERE e.family = :family " +
            "AND e.createdAt BETWEEN :start AND :end " +
            "ORDER BY e.createdAt DESC")
    List<EmergencyAlert> findByFamilyAndCreatedAtBetween(
            @Param("family") Family family,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 특정 시각 이후 동일한 타입의 알림이 있는지 확인 (중복 방지)
     */
    @Query("SELECT COUNT(e) > 0 FROM EmergencyAlert e " +
            "WHERE e.family = :family " +
            "AND e.alertType = :alertType " +
            "AND e.createdAt >= :since")
    boolean existsByFamilyAndAlertTypeAndCreatedAtAfter(
            @Param("family") Family family,
            @Param("alertType") AlertType alertType,
            @Param("since") LocalDateTime since
    );
}
