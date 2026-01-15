package com.example.spring.family.dto;

import com.example.spring.family.domain.FamilyRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 가족 그룹 가입 요청
 */
public record JoinFamilyRequest(
        @NotBlank(message = "초대 코드는 필수입니다")
        @Pattern(regexp = "^\\d{6}$", message = "초대 코드는 6자리 숫자여야 합니다")
        String inviteCode,

        @NotNull(message = "역할(Role)은 필수입니다")
        FamilyRole role,

        String nickname
) {
}
