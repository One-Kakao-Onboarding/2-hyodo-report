package com.example.spring.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfo(
        @JsonProperty("id")
        Long id,

        @JsonProperty("connected_at")
        String connectedAt,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount,

        @JsonProperty("properties")
        Properties properties
) {
    public record KakaoAccount(
            @JsonProperty("profile_needs_agreement")
            Boolean profileNeedsAgreement,

            @JsonProperty("profile")
            Profile profile,

            @JsonProperty("has_email")
            Boolean hasEmail,

            @JsonProperty("email_needs_agreement")
            Boolean emailNeedsAgreement,

            @JsonProperty("is_email_valid")
            Boolean isEmailValid,

            @JsonProperty("is_email_verified")
            Boolean isEmailVerified,

            @JsonProperty("email")
            String email
    ) {
    }

    public record Profile(
            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("is_default_image")
            Boolean isDefaultImage
    ) {
    }

    public record Properties(
            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image")
            String profileImage,

            @JsonProperty("thumbnail_image")
            String thumbnailImage
    ) {
    }

    /**
     * 카카오 ID 문자열 반환
     */
    public String getKakaoIdAsString() {
        return String.valueOf(id);
    }

    /**
     * 이메일 추출 (우선순위: kakaoAccount.email)
     */
    public String getEmail() {
        if (kakaoAccount != null && kakaoAccount.email() != null) {
            return kakaoAccount.email();
        }
        return null;
    }

    /**
     * 닉네임 추출 (우선순위: kakaoAccount.profile.nickname > properties.nickname)
     */
    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.profile() != null
                && kakaoAccount.profile().nickname() != null) {
            return kakaoAccount.profile().nickname();
        }
        if (properties != null && properties.nickname() != null) {
            return properties.nickname();
        }
        return "Unknown";
    }

    /**
     * 프로필 이미지 URL 추출
     */
    public String getProfileImageUrl() {
        if (kakaoAccount != null && kakaoAccount.profile() != null
                && kakaoAccount.profile().profileImageUrl() != null) {
            return kakaoAccount.profile().profileImageUrl();
        }
        if (properties != null && properties.profileImage() != null) {
            return properties.profileImage();
        }
        return null;
    }
}
