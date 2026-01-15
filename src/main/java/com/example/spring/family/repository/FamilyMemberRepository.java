package com.example.spring.family.repository;

import com.example.spring.family.domain.Family;
import com.example.spring.family.domain.FamilyMember;
import com.example.spring.family.domain.FamilyRole;
import com.example.spring.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 가족 구성원 Repository
 */
@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    /**
     * 사용자가 속한 모든 가족 구성원 정보 조회
     */
    List<FamilyMember> findByUser(User user);

    /**
     * 특정 가족의 모든 구성원 조회
     */
    List<FamilyMember> findByFamily(Family family);

    /**
     * 특정 가족의 특정 역할 구성원 조회
     */
    List<FamilyMember> findByFamilyAndRole(Family family, FamilyRole role);

    /**
     * 사용자와 가족으로 구성원 조회
     */
    Optional<FamilyMember> findByUserAndFamily(User user, Family family);

    /**
     * 사용자가 특정 가족에 속해있는지 확인
     */
    boolean existsByUserAndFamily(User user, Family family);

    /**
     * 특정 가족의 부모 구성원 조회
     */
    @Query("SELECT fm FROM FamilyMember fm WHERE fm.family = :family AND fm.role = 'PARENT'")
    List<FamilyMember> findParentsByFamily(@Param("family") Family family);

    /**
     * 특정 가족의 자녀 구성원 조회
     */
    @Query("SELECT fm FROM FamilyMember fm WHERE fm.family = :family AND fm.role = 'CHILD'")
    List<FamilyMember> findChildrenByFamily(@Param("family") Family family);
}
