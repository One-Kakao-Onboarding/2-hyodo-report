package com.example.spring.auth.dto;

import com.example.spring.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "인증 코드는 필수입니다")
        String code,

        @NotNull(message = "역할(Role)은 필수입니다")
        Role role
) {
}
