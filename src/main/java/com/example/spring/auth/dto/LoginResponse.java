package com.example.spring.auth.dto;

import com.example.spring.user.domain.Role;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        Role role,
        JwtToken token,
        boolean isNewUser
) {
    public static LoginResponse of(
            Long userId,
            String email,
            String nickname,
            String profileImageUrl,
            Role role,
            JwtToken token,
            boolean isNewUser
    ) {
        return new LoginResponse(userId, email, nickname, profileImageUrl, role, token, isNewUser);
    }
}
