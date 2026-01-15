package com.example.spring.auth.service;

import com.example.spring.user.domain.Role;
import com.example.spring.user.domain.User;
import com.example.spring.auth.dto.JwtToken;
import com.example.spring.auth.dto.LoginResponse;
import com.example.spring.common.exception.InvalidTokenException;
import com.example.spring.common.exception.UserNotFoundException;
import com.example.spring.user.repository.UserRepository;
import com.example.spring.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 간단한 Mock 로그인 (프로토타입용)
     * username으로 사용자를 찾고, 없으면 자동 생성
     */
    @Transactional
    public LoginResponse login(String username, Role role) {
        // 1. 사용자 조회 또는 생성 (kakaoId 필드를 username으로 사용)
        User user = userRepository.findByKakaoId(username)
                .orElseGet(() -> createUser(username, role));

        boolean isNewUser = user.getRefreshToken() == null;

        // 2. JWT 토큰 생성
        JwtToken jwtToken = generateJwtToken(user);

        // 3. Refresh Token 저장
        user.updateRefreshToken(jwtToken.refreshToken());
        userRepository.save(user);

        log.info("User logged in successfully. userId: {}, username: {}, isNewUser: {}",
                user.getId(), username, isNewUser);

        return LoginResponse.of(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole(),
                jwtToken,
                isNewUser
        );
    }

    /**
     * Refresh Token으로 Access Token 갱신
     */
    @Transactional
    public JwtToken refreshToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token입니다");
        }

        // 2. Refresh Token으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        // 3. 새로운 JWT 토큰 생성
        JwtToken newJwtToken = generateJwtToken(user);

        // 4. 새로운 Refresh Token 저장
        user.updateRefreshToken(newJwtToken.refreshToken());
        userRepository.save(user);

        log.info("Token refreshed successfully. userId: {}", user.getId());

        return newJwtToken;
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        // Refresh Token 제거
        user.updateRefreshToken(null);
        userRepository.save(user);

        log.info("User logged out successfully. userId: {}", userId);
    }

    /**
     * Mock 사용자 생성 (프로토타입용)
     */
    private User createUser(String username, Role role) {
        User newUser = User.builder()
                .kakaoId(username)  // kakaoId 필드를 username으로 사용
                .email(username + "@example.com")
                .nickname(username)
                .profileImageUrl("https://via.placeholder.com/150")
                .role(role)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("New user created. userId: {}, username: {}, role: {}",
                savedUser.getId(), username, savedUser.getRole());

        return savedUser;
    }

    /**
     * JWT 토큰 생성
     */
    private JwtToken generateJwtToken(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return JwtToken.of(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    /**
     * 사용자 조회 by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 사용자 조회 by Kakao ID
     */
    @Transactional(readOnly = true)
    public User getUserByKakaoId(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 사용자 역할 업데이트
     */
    @Transactional
    public void updateUserRole(Long userId, Role role) {
        User user = getUserById(userId);
        user.updateRole(role);
        userRepository.save(user);

        log.info("User role updated. userId: {}, newRole: {}", userId, role);
    }
}
