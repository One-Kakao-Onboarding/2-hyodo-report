package com.example.spring.family.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 가족 그룹
 * 부모와 자녀를 연결하는 단위
 */
@Entity
@Table(name = "families")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 가족 그룹명 (예: "우리 가족")
     */
    @Column(nullable = false)
    private String name;

    /**
     * 초대 코드 (6자리 숫자)
     */
    @Column(nullable = false, unique = true, length = 6)
    private String inviteCode;

    /**
     * 가족 구성원 목록
     */
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FamilyMember> members = new ArrayList<>();

    /**
     * 그룹 생성 시각
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 그룹 수정 시각
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Family(String name) {
        this.name = name;
        this.inviteCode = generateInviteCode();
    }

    /**
     * 6자리 랜덤 초대 코드 생성
     */
    private String generateInviteCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    /**
     * 가족 구성원 추가
     */
    public void addMember(FamilyMember member) {
        this.members.add(member);
        member.assignFamily(this);
    }

    /**
     * 가족 그룹명 변경
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 초대 코드 재생성
     */
    public void regenerateInviteCode() {
        this.inviteCode = generateInviteCode();
    }
}
