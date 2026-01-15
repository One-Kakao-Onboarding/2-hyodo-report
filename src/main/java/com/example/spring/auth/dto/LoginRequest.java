package com.example.spring.auth.dto;

import com.example.spring.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 간단한 Mock 로그인 요청 (프로토타입용)
 * username으로 사용자를 식별하고, 없으면 자동 생성
 */
public record LoginRequest(
        @NotBlank(message = "사용자 이름은 필수입니다")
        String username,

        @NotNull(message = "역할(Role)은 필수입니다")
        Role role
) {
}
