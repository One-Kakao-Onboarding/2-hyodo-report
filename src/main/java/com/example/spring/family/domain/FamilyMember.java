package com.example.spring.family.domain;

import com.example.spring.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 가족 구성원
 * User와 Family를 연결하는 중간 엔티티
 */
@Entity
@Table(name = "family_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "family_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 가족 그룹
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    /**
     * 가족 내 역할 (부모/자녀)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRole role;

    /**
     * 닉네임 (가족 내에서 부르는 이름, 예: "엄마", "아들")
     */
    @Column(length = 50)
    private String nickname;

    /**
     * 가입 시각
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public FamilyMember(User user, Family family, FamilyRole role, String nickname) {
        this.user = user;
        this.family = family;
        this.role = role;
        this.nickname = nickname;
    }

    /**
     * Family 연관관계 설정 (Family.addMember()에서 호출)
     */
    void assignFamily(Family family) {
        this.family = family;
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 역할 확인 메서드
     */
    public boolean isParent() {
        return this.role == FamilyRole.PARENT;
    }

    public boolean isChild() {
        return this.role == FamilyRole.CHILD;
    }
}
