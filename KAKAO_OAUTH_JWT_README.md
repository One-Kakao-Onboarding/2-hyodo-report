# 카카오 OAuth2 & JWT 인증 구현

효도시그널 프로젝트의 백엔드 인증 시스템 구현입니다.

## 구현 완료 항목

### 1. 의존성 추가 (build.gradle)
- Spring Security
- Spring OAuth2 Client
- JWT (jjwt 0.12.3)
- WebFlux (카카오 API 호출용)

### 2. 설정 파일

#### application.properties
```properties
# Secret 설정 파일 임포트
spring.config.import=optional:classpath:application-secret.properties
```

#### application-secret.properties
모든 민감한 정보는 `application-secret.properties`에 저장됩니다.
- `.gitignore`에 포함되어 Git에 커밋되지 않습니다
- `application-secret.properties.example` 파일을 복사하여 사용하세요

```properties
# JWT 설정
jwt.secret=your_base64_secret_here
jwt.access-token-expiration=3600000        # 1시간
jwt.refresh-token-expiration=604800000     # 7일

# 카카오 OAuth2 설정
kakao.client-id=your_kakao_rest_api_key
kakao.client-secret=your_kakao_client_secret
kakao.redirect-uri=http://localhost:8080/api/auth/kakao/callback
```

### 3. 도메인 모델

#### User 엔티티
- 카카오 ID 기반 사용자 관리
- Role (CHILD/PARENT) 구분
- Refresh Token 저장

```java
@Entity
@Table(name = "users")
public class User {
    private Long id;
    private String kakaoId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Role role;
    private String refreshToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 4. JWT 관련 구현

#### JwtTokenProvider
- Access Token 생성 (유효기간: 1시간)
- Refresh Token 생성 (유효기간: 7일)
- 토큰 검증 및 파싱
- 사용자 정보 추출

주요 메서드:
```java
String createAccessToken(Long userId, String email, String role)
String createRefreshToken(Long userId)
boolean validateToken(String token)
Long getUserIdFromToken(String token)
```

### 5. 카카오 OAuth2 구현

#### KakaoOAuthService
카카오 인증 서버와 통신하는 서비스입니다.

주요 메서드:
```java
KakaoTokenResponse getAccessToken(String authorizationCode)
KakaoUserInfo getUserInfo(String accessToken)
KakaoTokenResponse refreshAccessToken(String refreshToken)
void logout(String accessToken)
```

#### DTO 클래스
- `KakaoTokenResponse`: 카카오 토큰 응답
- `KakaoUserInfo`: 카카오 사용자 정보

### 6. 인증 서비스

#### AuthService
전체 인증 플로우를 처리하는 메인 서비스입니다.

주요 메서드:
```java
// 카카오 로그인 및 회원가입
LoginResponse login(String authorizationCode, Role role)

// Refresh Token으로 Access Token 갱신
JwtToken refreshToken(String refreshToken)

// 로그아웃
void logout(Long userId)

// 사용자 조회
User getUserById(Long userId)
User getUserByKakaoId(String kakaoId)

// 사용자 역할 업데이트
void updateUserRole(Long userId, Role role)
```

### 7. Security 설정

#### SecurityConfig
- CSRF 비활성화 (JWT 사용)
- CORS 설정
- Stateless 세션 관리
- 엔드포인트 권한 설정

```java
// 허용된 엔드포인트
/api/auth/**
/api/oauth/**
/health
/actuator/**
```

## 인증 플로우

### 1. 로그인 플로우
```
1. 프론트엔드가 카카오 로그인 페이지로 리다이렉트
2. 사용자가 카카오 로그인
3. 카카오가 authorization code를 콜백 URL로 전달
4. 백엔드가 authorization code로 카카오 액세스 토큰 요청
5. 카카오 액세스 토큰으로 사용자 정보 조회
6. 사용자 정보로 회원가입 또는 로그인 처리
7. JWT 토큰 생성 및 반환
```

### 2. 토큰 갱신 플로우
```
1. Access Token 만료 시 Refresh Token 사용
2. Refresh Token 유효성 검증
3. 새로운 Access Token 및 Refresh Token 생성
4. DB에 새로운 Refresh Token 저장
```

## 프로젝트 구조 (도메인 중심)

```
src/main/java/com/example/spring/
├── user/                               # 사용자 도메인
│   ├── domain/
│   │   ├── User.java                   # 사용자 엔티티
│   │   └── Role.java                   # 사용자 역할 Enum
│   └── repository/
│       └── UserRepository.java         # 사용자 Repository
│
├── auth/                               # 인증 도메인
│   ├── controller/
│   │   └── AuthController.java         # 인증 API 컨트롤러
│   ├── service/
│   │   └── AuthService.java            # 인증 서비스
│   ├── dto/
│   │   ├── JwtToken.java               # JWT 토큰 DTO (record)
│   │   ├── LoginRequest.java           # 로그인 요청 (record)
│   │   ├── LoginResponse.java          # 로그인 응답 (record)
│   │   └── TokenRefreshRequest.java    # 토큰 갱신 요청 (record)
│   ├── security/
│   │   └── JwtTokenProvider.java       # JWT 토큰 관리
│   └── config/
│       ├── JwtProperties.java          # JWT 설정
│       └── SecurityConfig.java         # Spring Security 설정
│
├── oauth/                              # OAuth 도메인
│   ├── service/
│   │   └── KakaoOAuthService.java      # 카카오 OAuth 서비스
│   ├── dto/
│   │   ├── KakaoTokenResponse.java     # 카카오 토큰 응답 (record)
│   │   └── KakaoUserInfo.java          # 카카오 사용자 정보 (record)
│   └── config/
│       └── KakaoOAuthProperties.java   # 카카오 OAuth 설정
│
├── common/                             # 공통 모듈
│   ├── config/
│   │   └── WebClientConfig.java        # WebClient 설정
│   ├── dto/
│   │   └── ApiResponse.java            # 공통 API 응답 포맷 (record)
│   └── exception/
│       ├── GlobalExceptionHandler.java # 전역 예외 처리
│       ├── UserNotFoundException.java  # 사용자 없음 예외
│       └── InvalidTokenException.java  # 유효하지 않은 토큰 예외
│
└── Application.java                    # Spring Boot 애플리케이션
```

### 도메인 중심 구조의 장점
- 관련된 코드가 한 곳에 모여 있어 찾기 쉬움
- 도메인별로 독립적인 개발 가능
- 패키지 간 의존성이 명확함
- 확장성이 뛰어남 (새 도메인 추가 시 새 패키지만 생성)

## API 엔드포인트

### AuthController

모든 응답은 `ApiResponse<T>` 포맷으로 반환됩니다:
```json
{
  "success": true,
  "data": { ... },
  "message": "성공 메시지",
  "error": null
}
```

#### 1. 카카오 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "code": "KAKAO_AUTHORIZATION_CODE",
  "role": "CHILD" or "PARENT"
}
```

**응답**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "홍길동",
    "profileImageUrl": "https://...",
    "role": "CHILD",
    "token": {
      "accessToken": "eyJhbG...",
      "refreshToken": "eyJhbG...",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    },
    "isNewUser": true
  },
  "message": "회원가입 및 로그인 성공"
}
```

#### 2. 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbG..."
}
```

#### 3. 로그아웃
```http
POST /api/auth/logout/{userId}
Authorization: Bearer {ACCESS_TOKEN}
```

#### 4. 현재 사용자 정보 조회
```http
GET /api/auth/me/{userId}
Authorization: Bearer {ACCESS_TOKEN}
```

### 에러 처리

`GlobalExceptionHandler`가 모든 예외를 처리합니다:
- `UserNotFoundException` → 404
- `InvalidTokenException` → 401
- `MethodArgumentNotValidException` → 400
- `RuntimeException` → 500

## 다음 단계

### JWT 필터 추가 (선택사항)
현재는 userId를 URL 파라미터로 전달하지만, JWT 필터를 추가하면:
- JwtAuthenticationFilter: 요청에서 JWT 추출 및 검증
- JwtAuthenticationEntryPoint: 인증 실패 처리
- SecurityContext에서 userId 자동 추출 가능

### 환경 설정

1. `application-secret.properties` 파일 생성
   ```bash
   cd src/main/resources
   cp application-secret.properties.example application-secret.properties
   ```

2. 카카오 개발자 콘솔에서 REST API 키 발급
   - https://developers.kakao.com/console/app
   - REST API 키와 Client Secret 발급
   - Redirect URI 등록: `http://localhost:8080/api/auth/kakao/callback`

3. JWT Secret 키 생성 (Base64 인코딩, 최소 64자)
   ```bash
   # macOS/Linux
   openssl rand -base64 64
   ```

4. `application-secret.properties`에 실제 값 입력

### 테스트

현재는 서비스 로직만 구현되어 있으므로, 컨트롤러 추가 후 통합 테스트가 필요합니다.

## 참고사항

- 모든 서비스 메서드에는 로깅이 추가되어 있습니다
- 트랜잭션 관리가 적용되어 있습니다
- 예외 처리가 구현되어 있습니다
- 카카오 사용자 정보는 안전하게 파싱됩니다

## 빠른 시작

1. `application-secret.properties` 설정
   ```bash
   cd src/main/resources
   cp application-secret.properties.example application-secret.properties
   # 파일을 열어 실제 값 입력
   ```

2. 빌드 및 실행
   ```bash
   ./gradlew bootRun
   ```

3. 컨트롤러 추가 후 테스트
   - 프론트엔드 확정 후 AuthController 추가 필요
   - 현재는 서비스 레이어만 구현 완료
