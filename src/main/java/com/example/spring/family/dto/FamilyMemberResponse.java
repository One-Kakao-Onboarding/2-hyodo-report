package com.example.spring.family.dto;

import com.example.spring.family.domain.FamilyMember;
import com.example.spring.family.domain.FamilyRole;

import java.time.LocalDateTime;

/**
 * 가족 구성원 응답
 */
public record FamilyMemberResponse(
        Long id,
        Long userId,
        String userNickname,
        String userProfileImageUrl,
        FamilyRole role,
        String nickname,
        LocalDateTime joinedAt
) {
    public static FamilyMemberResponse from(FamilyMember member) {
        return new FamilyMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getRole(),
                member.getNickname(),
                member.getJoinedAt()
        );
    }
}
