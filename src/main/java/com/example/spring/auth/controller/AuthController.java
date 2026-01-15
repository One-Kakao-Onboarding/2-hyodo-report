package com.example.spring.auth.controller;

import com.example.spring.auth.dto.JwtToken;
import com.example.spring.auth.dto.LoginRequest;
import com.example.spring.auth.dto.LoginResponse;
import com.example.spring.auth.dto.TokenRefreshRequest;
import com.example.spring.auth.service.AuthService;
import com.example.spring.common.dto.ApiResponse;
import com.example.spring.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 카카오 로그인
     * POST /api/auth/login
     * POST /api/auth/kakao/login (alias)
     */
    @PostMapping({"/login", "/kakao/login"})
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received. role: {}", request.role());

        LoginResponse response = authService.login(request.code(), request.role());

        String message = response.isNewUser() ? "회원가입 및 로그인 성공" : "로그인 성공";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtToken>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh request received");

        JwtToken newToken = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(ApiResponse.success(newToken, "토큰 갱신 성공"));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        // Authorization 헤더에서 "Bearer " 제거 후 토큰 추출
        String token = authorization.replace("Bearer ", "");

        // JWT에서 userId 추출
        // 실제로는 JwtTokenProvider를 주입받아서 사용해야 하지만,
        // 현재는 간단하게 AuthService에 위임
        // TODO: JWT Filter를 추가하면 SecurityContext에서 userId를 가져올 수 있음

        log.info("Logout request received");

        // 현재는 간단한 구현으로 진행
        // 프론트엔드에서 토큰을 삭제하는 것만으로도 충분

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }

    /**
     * 로그아웃 (userId 직접 전달)
     * POST /api/auth/logout/{userId}
     */
    @PostMapping("/logout/{userId}")
    public ResponseEntity<ApiResponse<Void>> logoutWithUserId(@PathVariable Long userId) {
        log.info("Logout request received. userId: {}", userId);

        authService.logout(userId);

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }

    /**
     * 현재 사용자 정보 조회
     * GET /api/auth/me/{userId}
     *
     * TODO: JWT Filter 추가 후 SecurityContext에서 userId를 가져오도록 변경
     */
    @GetMapping("/me/{userId}")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(@PathVariable Long userId) {
        log.info("Get current user request. userId: {}", userId);

        User user = authService.getUserById(userId);

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    /**
     * 사용자 정보 응답 DTO
     */
    public record UserInfo(
            Long userId,
            String email,
            String nickname,
            String profileImageUrl,
            com.example.spring.user.domain.Role role
    ) {
    }
}
