package com.example.spring.family.dto;

import com.example.spring.family.domain.FamilyRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 가족 그룹 생성 요청
 */
public record CreateFamilyRequest(
        @NotBlank(message = "가족 그룹명은 필수입니다")
        String name,

        @NotNull(message = "역할(Role)은 필수입니다")
        FamilyRole role,

        String nickname
) {
}
