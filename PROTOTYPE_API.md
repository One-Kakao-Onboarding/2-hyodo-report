# 프로토타입 API 가이드

## 개요
카카오 OAuth를 제거하고 간단한 username 기반 mock 인증으로 변경했습니다.
프로토타입 개발용으로 빠르게 테스트할 수 있습니다.

## 변경 사항

### 제거된 것들
- ❌ 카카오 OAuth 로그인
- ❌ `/api/auth/kakao/callback` 엔드포인트
- ❌ `KakaoOAuthService`, `KakaoOAuthProperties`
- ❌ `KakaoTokenResponse`, `KakaoUserInfo`
- ❌ `spring-boot-starter-oauth2-client` 의존성

### 유지된 것들
- ✅ JWT 토큰 인증
- ✅ User 엔티티 (kakaoId 필드를 username으로 사용)
- ✅ Refresh Token 갱신
- ✅ 로그아웃
- ✅ Security 설정 (CORS 포함)

## API 엔드포인트

### 1. 로그인 (Mock)

**POST** `/api/auth/login`

**Request Body:**
```json
{
  "username": "testuser",
  "role": "CHILD"
}
```

- `username`: 사용자 이름 (없으면 자동 생성)
- `role`: `CHILD` 또는 `PARENT`

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "testuser@example.com",
    "nickname": "testuser",
    "profileImageUrl": "https://via.placeholder.com/150",
    "role": "CHILD",
    "token": {
      "accessToken": "eyJhbGc...",
      "refreshToken": "eyJhbGc...",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    },
    "isNewUser": true
  },
  "message": "회원가입 및 로그인 성공"
}
```

### 2. 토큰 갱신

**POST** `/api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 3600000
  },
  "message": "토큰 갱신 성공"
}
```

### 3. 로그아웃

**POST** `/api/auth/logout/{userId}`

**Response:**
```json
{
  "success": true,
  "data": null,
  "message": "로그아웃 성공"
}
```

### 4. 현재 사용자 정보 조회

**GET** `/api/auth/me/{userId}`

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "testuser@example.com",
    "nickname": "testuser",
    "profileImageUrl": "https://via.placeholder.com/150",
    "role": "CHILD"
  }
}
```

## 테스트 방법

### 1. curl로 테스트

```bash
# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "role": "CHILD"}'

# 토큰 갱신
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'

# 사용자 정보 조회
curl -X GET http://localhost:8080/api/auth/me/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 2. 프론트엔드 통합

프론트엔드 코드는 수정하지 않았으므로, 기존 코드가 다음과 같이 호출합니다:

```typescript
// 기존: apiClient.login({ code: "...", role: "CHILD" })
// 새로운: apiClient.login({ username: "testuser", role: "CHILD" })
```

**주의:** 프론트엔드에서 `LoginRequest` 타입을 `{ code, role }`에서 `{ username, role }`로 변경해야 합니다.

## 사용자 데이터

- **username**을 User 테이블의 `kakaoId` 필드에 저장
- **email**은 자동으로 `username@example.com` 생성
- **nickname**은 username과 동일
- **profileImageUrl**은 플레이스홀더 이미지

## 보안 설정

- 모든 `/api/auth/**` 엔드포인트는 인증 불필요
- 나머지 엔드포인트는 JWT 토큰 필요
- CORS: 모든 origin 허용 (프로토타입용)

## 다음 단계

프론트엔드 통합을 위해 다음을 수정해야 합니다:
1. `.env.local` 파일 생성
2. `LoginRequest` 타입 변경 (code → username)
3. 카카오 로그인 UI를 간단한 username 입력 폼으로 변경
