package com.example.spring.family.repository;

import com.example.spring.family.domain.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 가족 그룹 Repository
 */
@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    /**
     * 초대 코드로 가족 그룹 조회
     */
    Optional<Family> findByInviteCode(String inviteCode);

    /**
     * 초대 코드 존재 여부 확인
     */
    boolean existsByInviteCode(String inviteCode);
}
