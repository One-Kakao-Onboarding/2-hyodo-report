# 효도시그널 백엔드 설정 가이드

카카오 OAuth2 로그인과 JWT 인증을 사용하기 위한 설정 가이드입니다.

## 목차
1. [카카오 개발자 콘솔 설정](#1-카카오-개발자-콘솔-설정)
2. [application-secret.properties 설정](#2-application-secretproperties-설정)
3. [데이터베이스 설정](#3-데이터베이스-설정)
4. [애플리케이션 실행](#4-애플리케이션-실행)
5. [API 테스트](#5-api-테스트)

---

## 1. 카카오 개발자 콘솔 설정

### 1.1 애플리케이션 생성

1. [카카오 개발자 콘솔](https://developers.kakao.com/console/app) 접속
2. "애플리케이션 추가하기" 클릭
3. 앱 이름: `효도시그널` (또는 원하는 이름)
4. 사업자명: 개인 또는 회사명 입력

### 1.2 앱 키 확인

애플리케이션 생성 후, **앱 키** 메뉴에서 다음 정보를 확인합니다:

- **REST API 키**: `kakao.client-id`에 사용
- **JavaScript 키**: 프론트엔드에서 사용
- **Admin 키**: 서버 관리용 (선택사항)

### 1.3 플랫폼 설정

**Web 플랫폼 등록**

1. 좌측 메뉴에서 **플랫폼** 클릭
2. **Web 플랫폼 등록** 클릭
3. 사이트 도메인 입력:
   - 개발: `http://localhost:3000`
   - 운영: `https://yourdomain.com`

### 1.4 카카오 로그인 활성화

1. 좌측 메뉴에서 **카카오 로그인** 클릭
2. **활성화 설정** → **ON**
3. **Redirect URI 등록**:
   ```
   http://localhost:8080/api/auth/kakao/callback
   ```
   (프론트엔드에서 실제로 사용할 Redirect URI)

### 1.5 동의 항목 설정

**카카오 로그인** > **동의항목** 메뉴에서 다음 항목을 설정합니다:

| 동의 항목 | 설정 | 이유 |
|---------|------|------|
| 닉네임 | 필수 동의 | 사용자 식별용 |
| 프로필 사진 | 선택 동의 | 프로필 이미지 표시 |
| 카카오계정(이메일) | 필수 동의 | 사용자 계정 식별 |

### 1.6 Client Secret 발급 (권장)

1. **카카오 로그인** > **보안** 메뉴 이동
2. **Client Secret** 코드 생성 클릭
3. **상태**: **사용함**으로 변경
4. 생성된 코드를 복사해둡니다 → `kakao.client-secret`에 사용

---

## 2. application-secret.properties 설정

### 2.1 파일 생성

```bash
cd src/main/resources
cp application-secret.properties.example application-secret.properties
```

### 2.2 설정값 입력

`application-secret.properties` 파일을 열고 다음 값들을 입력합니다:

```properties
# Database Password
spring.datasource.password=your_actual_database_password

# JWT Configuration
jwt.secret=YourActualBase64EncodedSecretKeyHere
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=604800000

# Kakao OAuth2 Configuration
kakao.client-id=your_kakao_rest_api_key
kakao.client-secret=your_kakao_client_secret
kakao.redirect-uri=http://localhost:8080/api/auth/kakao/callback
kakao.token-uri=https://kauth.kakao.com/oauth/token
kakao.user-info-uri=https://kapi.kakao.com/v2/user/me
```

### 2.3 JWT Secret 생성

JWT Secret은 최소 64자 이상의 Base64 인코딩된 문자열이어야 합니다.

**macOS/Linux**
```bash
openssl rand -base64 64
```

**Windows (PowerShell)**
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

**온라인 생성기**
- https://generate-random.org/base64-string-generator (64 bytes 선택)

생성된 값을 복사하여 `jwt.secret`에 입력합니다.

### 2.4 설정값 상세 설명

| 항목 | 설명 | 예시 |
|------|------|------|
| `spring.datasource.password` | Supabase PostgreSQL 비밀번호 | `your_db_password` |
| `jwt.secret` | JWT 서명용 비밀키 (Base64, 64자+) | `YourBase64Secret...` |
| `jwt.access-token-expiration` | Access Token 만료 시간 (ms) | `3600000` (1시간) |
| `jwt.refresh-token-expiration` | Refresh Token 만료 시간 (ms) | `604800000` (7일) |
| `kakao.client-id` | 카카오 REST API 키 | `abc123def456...` |
| `kakao.client-secret` | 카카오 Client Secret | `xyz789uvw012...` |
| `kakao.redirect-uri` | 프론트엔드 콜백 URI | `http://localhost:8080/...` |

---

## 3. 데이터베이스 설정

### 3.1 Supabase 확인

현재 `application.properties`에 Supabase PostgreSQL 설정이 되어 있습니다:

```properties
spring.datasource.url=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres
spring.datasource.username=postgres.okgnckdhebnmlkqowfvo
```

### 3.2 테이블 자동 생성

`spring.jpa.hibernate.ddl-auto=update` 설정으로 애플리케이션 실행 시 `users` 테이블이 자동으로 생성됩니다.

**테이블 스키마 (자동 생성됨)**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    kakao_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    refresh_token VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## 4. 애플리케이션 실행

### 4.1 의존성 설치 및 빌드

```bash
./gradlew build
```

### 4.2 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/app.jar
```

### 4.3 실행 확인

애플리케이션이 정상적으로 실행되면 다음 로그가 출력됩니다:

```
Started Application in X.XXX seconds
```

기본 포트: `8080`

---

## 5. API 테스트

### 5.1 카카오 로그인 플로우

#### Step 1: 프론트엔드에서 카카오 로그인 페이지로 리다이렉트

```
https://kauth.kakao.com/oauth/authorize?client_id={REST_API_KEY}&redirect_uri={REDIRECT_URI}&response_type=code
```

**예시**
```
https://kauth.kakao.com/oauth/authorize?client_id=abc123def456&redirect_uri=http://localhost:8080/api/auth/kakao/callback&response_type=code
```

#### Step 2: 사용자가 카카오 로그인

사용자가 카카오 계정으로 로그인하고 동의하면, 카카오가 Redirect URI로 인증 코드를 전달합니다:

```
http://localhost:8080/api/auth/kakao/callback?code=AUTHORIZATION_CODE
```

#### Step 3: 백엔드 로그인 API 호출

프론트엔드에서 인증 코드를 백엔드로 전달합니다:

**요청**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "code": "AUTHORIZATION_CODE",
  "role": "CHILD"
}
```

**응답 (성공)**
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
      "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    },
    "isNewUser": true
  },
  "message": "회원가입 및 로그인 성공"
}
```

### 5.2 토큰 갱신

**요청**
```http
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**응답**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000
  },
  "message": "토큰 갱신 성공"
}
```

### 5.3 사용자 정보 조회

**요청**
```http
GET http://localhost:8080/api/auth/me/1
Authorization: Bearer {ACCESS_TOKEN}
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
    "role": "CHILD"
  }
}
```

### 5.4 로그아웃

**요청**
```http
POST http://localhost:8080/api/auth/logout/1
Authorization: Bearer {ACCESS_TOKEN}
```

**응답**
```json
{
  "success": true,
  "message": "로그아웃 성공"
}
```

---

## 6. 트러블슈팅

### 6.1 카카오 로그인 실패

**증상**: "카카오 액세스 토큰 발급 실패" 에러

**해결 방법**:
1. `kakao.client-id`가 REST API 키인지 확인
2. `kakao.client-secret`이 정확한지 확인
3. Redirect URI가 카카오 개발자 콘솔에 등록되어 있는지 확인
4. 카카오 로그인이 활성화되어 있는지 확인

### 6.2 JWT 토큰 검증 실패

**증상**: "Invalid JWT signature" 에러

**해결 방법**:
1. `jwt.secret`이 Base64로 인코딩되어 있는지 확인
2. `jwt.secret`이 최소 64자 이상인지 확인
3. 애플리케이션 재시작

### 6.3 데이터베이스 연결 실패

**증상**: "Connection refused" 에러

**해결 방법**:
1. `spring.datasource.password`가 정확한지 확인
2. Supabase 데이터베이스가 활성 상태인지 확인
3. 네트워크 연결 확인

### 6.4 Validation 에러

**증상**: 400 Bad Request

**해결 방법**:
1. 요청 body에 필수 필드가 모두 포함되어 있는지 확인
2. `code` 필드가 비어있지 않은지 확인
3. `role` 필드가 `CHILD` 또는 `PARENT`인지 확인

---

## 7. 다음 단계

### 7.1 JWT 인증 필터 추가 (선택사항)

현재는 userId를 URL 파라미터로 전달하고 있지만, JWT 필터를 추가하면 더 안전하게 인증할 수 있습니다.

### 7.2 프론트엔드 연동

프론트엔드에서 다음 작업을 수행해야 합니다:
1. 카카오 로그인 버튼 구현
2. 인증 코드를 백엔드로 전달
3. 받은 JWT 토큰을 로컬 스토리지에 저장
4. 모든 API 요청에 `Authorization: Bearer {token}` 헤더 추가
5. Access Token 만료 시 Refresh Token으로 갱신

### 7.3 추가 기능 구현

- [ ] 사용자 프로필 수정 API
- [ ] 사용자 역할 변경 API
- [ ] 사용자 삭제 API
- [ ] 가족 그룹 기능
- [ ] 카카오톡 대화 분석 기능

---

## 체크리스트

설정이 완료되었는지 확인하세요:

- [ ] 카카오 개발자 콘솔에 애플리케이션 생성
- [ ] REST API 키 발급
- [ ] Client Secret 발급
- [ ] Redirect URI 등록
- [ ] 카카오 로그인 활성화
- [ ] 동의 항목 설정
- [ ] `application-secret.properties` 파일 생성
- [ ] JWT Secret 생성 및 입력
- [ ] 카카오 API 키 입력
- [ ] 데이터베이스 비밀번호 입력
- [ ] 애플리케이션 빌드 성공
- [ ] 애플리케이션 실행 성공
- [ ] 로그인 API 테스트 성공

모든 항목이 완료되면 카카오 로그인을 사용할 준비가 완료되었습니다! 🎉
