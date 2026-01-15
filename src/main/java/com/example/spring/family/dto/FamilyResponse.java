package com.example.spring.family.dto;

import com.example.spring.family.domain.Family;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 가족 그룹 응답
 */
public record FamilyResponse(
        Long id,
        String name,
        String inviteCode,
        List<FamilyMemberResponse> members,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FamilyResponse from(Family family) {
        List<FamilyMemberResponse> memberResponses = family.getMembers().stream()
                .map(FamilyMemberResponse::from)
                .toList();

        return new FamilyResponse(
                family.getId(),
                family.getName(),
                family.getInviteCode(),
                memberResponses,
                family.getCreatedAt(),
                family.getUpdatedAt()
        );
    }

    /**
     * 초대 코드 없이 응답 생성 (보안상 필요한 경우)
     */
    public static FamilyResponse fromWithoutInviteCode(Family family) {
        List<FamilyMemberResponse> memberResponses = family.getMembers().stream()
                .map(FamilyMemberResponse::from)
                .toList();

        return new FamilyResponse(
                family.getId(),
                family.getName(),
                null, // 초대 코드 숨김
                memberResponses,
                family.getCreatedAt(),
                family.getUpdatedAt()
        );
    }
}
