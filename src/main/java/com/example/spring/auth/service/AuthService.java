package com.example.spring.auth.service;

import com.example.spring.user.domain.Role;
import com.example.spring.user.domain.User;
import com.example.spring.auth.dto.JwtToken;
import com.example.spring.auth.dto.LoginResponse;
import com.example.spring.oauth.dto.KakaoTokenResponse;
import com.example.spring.oauth.dto.KakaoUserInfo;
import com.example.spring.common.exception.InvalidTokenException;
import com.example.spring.common.exception.UserNotFoundException;
import com.example.spring.user.repository.UserRepository;
import com.example.spring.auth.security.JwtTokenProvider;
import com.example.spring.oauth.service.KakaoOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 카카오 OAuth 로그인 및 회원가입
     */
    @Transactional
    public LoginResponse login(String authorizationCode, Role role) {
        // 1. 카카오 인증 코드로 액세스 토큰 요청
        KakaoTokenResponse kakaoToken = kakaoOAuthService.getAccessToken(authorizationCode);

        // 2. 카카오 액세스 토큰으로 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoOAuthService.getUserInfo(kakaoToken.accessToken());

        // 3. 사용자 조회 또는 생성
        User user = userRepository.findByKakaoId(kakaoUserInfo.getKakaoIdAsString())
                .orElseGet(() -> createUser(kakaoUserInfo, role));

        boolean isNewUser = user.getRefreshToken() == null;

        // 4. JWT 토큰 생성
        JwtToken jwtToken = generateJwtToken(user);

        // 5. Refresh Token 저장
        user.updateRefreshToken(jwtToken.refreshToken());
        userRepository.save(user);

        log.info("User logged in successfully. userId: {}, isNewUser: {}", user.getId(), isNewUser);

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
     * 사용자 생성
     */
    private User createUser(KakaoUserInfo kakaoUserInfo, Role role) {
        User newUser = User.builder()
                .kakaoId(kakaoUserInfo.getKakaoIdAsString())
                .email(kakaoUserInfo.getEmail())
                .nickname(kakaoUserInfo.getNickname())
                .profileImageUrl(kakaoUserInfo.getProfileImageUrl())
                .role(role)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("New user created. userId: {}, kakaoId: {}, role: {}",
                savedUser.getId(), savedUser.getKakaoId(), savedUser.getRole());

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
