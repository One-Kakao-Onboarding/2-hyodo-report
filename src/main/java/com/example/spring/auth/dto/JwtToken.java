package com.example.spring.auth.dto;

public record JwtToken(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
    public static JwtToken of(String accessToken, String refreshToken, String tokenType, Long expiresIn) {
        return new JwtToken(accessToken, refreshToken, tokenType, expiresIn);
    }
}
