# GitHub Secrets 설정 가이드

배포를 위해 GitHub Repository Secrets에 등록해야 하는 환경변수 목록입니다.

## 📋 등록해야 할 Secrets

### 1. 기존 Secrets (이미 있음)
- `EC2_HOST` - EC2 인스턴스 IP 주소
- `EC2_USERNAME` - EC2 사용자명 (ubuntu)
- `EC2_KEY` - EC2 SSH 개인키
- `EC2_PORT` - EC2 SSH 포트 (22)
- `DB_PASSWORD` - 데이터베이스 비밀번호

### 2. 추가해야 할 Secrets (JWT & 카카오 OAuth)

| Secret Name | 값 | 설명 |
|-------------|-----|------|
| `JWT_SECRET` | `8weWLVm1/ymgjvMn2r5e+UIavE+oss+a1LkWszE4KzlhJFudVY5Wf66grf/o2XVW5Kq2wEk1i+M2VpI43+HkKg==` | JWT 서명용 비밀키 |
| `KAKAO_CLIENT_ID` | `5e44e34fc74e2d4de17c455562b35302` | 카카오 REST API 키 |
| `KAKAO_CLIENT_SECRET` | `0QKwKXjwqtV63zFgVKoH1PnenRYgl8zN` | 카카오 Client Secret |
| `KAKAO_REDIRECT_URI` | `http://{EC2_PUBLIC_IP}:8080/api/auth/kakao/callback` | 카카오 OAuth 리다이렉트 URI (운영) |

⚠️ **주의**: `KAKAO_REDIRECT_URI`는 EC2 서버의 실제 Public IP 또는 도메인으로 설정해야 합니다!

---

## 🔧 GitHub Secrets 등록 방법

### 1. GitHub Repository 접속
```
https://github.com/{username}/{repository}/settings/secrets/actions
```

### 2. New repository secret 클릭

### 3. 각 Secret 등록

**JWT_SECRET**
- Name: `JWT_SECRET`
- Secret: `8weWLVm1/ymgjvMn2r5e+UIavE+oss+a1LkWszE4KzlhJFudVY5Wf66grf/o2XVW5Kq2wEk1i+M2VpI43+HkKg==`

**KAKAO_CLIENT_ID**
- Name: `KAKAO_CLIENT_ID`
- Secret: `5e44e34fc74e2d4de17c455562b35302`

**KAKAO_CLIENT_SECRET**
- Name: `KAKAO_CLIENT_SECRET`
- Secret: `0QKwKXjwqtV63zFgVKoH1PnenRYgl8zN`

**KAKAO_REDIRECT_URI**
- Name: `KAKAO_REDIRECT_URI`
- Secret: `http://YOUR_EC2_PUBLIC_IP:8080/api/auth/kakao/callback`
  - 예시: `http://43.201.123.456:8080/api/auth/kakao/callback`
  - 또는 도메인이 있다면: `https://api.hyodosignal.com/api/auth/kakao/callback`

---

## 📝 카카오 개발자 콘솔 추가 설정

운영 환경의 Redirect URI도 카카오 개발자 콘솔에 등록해야 합니다.

### 1. 카카오 개발자 콘솔 접속
https://developers.kakao.com/console/app

### 2. 애플리케이션 선택

### 3. 카카오 로그인 > Redirect URI 설정

기존 로컬 URI 외에 운영 URI 추가:
```
http://localhost:8080/api/auth/kakao/callback          # 로컬 개발용
http://YOUR_EC2_PUBLIC_IP:8080/api/auth/kakao/callback # 운영용
```

또는 도메인이 있다면:
```
https://api.hyodosignal.com/api/auth/kakao/callback   # 운영용 (HTTPS 권장)
```

---

## 🔍 설정 확인 체크리스트

배포 전에 다음 사항들을 확인하세요:

### GitHub Secrets 확인
- [ ] `JWT_SECRET` 등록 완료
- [ ] `KAKAO_CLIENT_ID` 등록 완료
- [ ] `KAKAO_CLIENT_SECRET` 등록 완료
- [ ] `KAKAO_REDIRECT_URI` 등록 완료 (EC2 Public IP 또는 도메인)

### 카카오 개발자 콘솔 확인
- [ ] 운영 환경 Redirect URI 등록 완료
- [ ] 카카오 로그인 활성화 상태 확인
- [ ] 동의 항목 설정 완료 (닉네임, 이메일, 프로필 사진)

### deploy.yml 확인
- [ ] 모든 환경변수가 `env:` 섹션에 포함되어 있는지 확인
- [ ] EC2에서 환경변수가 제대로 전달되는지 확인

---

## 🚨 보안 주의사항

1. **절대로 커밋하지 마세요**:
   - `application-secret.properties` 파일은 `.gitignore`에 포함되어 있어야 합니다
   - Secret 값들을 코드에 직접 작성하지 마세요

2. **Secret 값 관리**:
   - GitHub Secrets에 등록된 값은 UI에서 다시 확인할 수 없습니다
   - 안전한 곳에 백업해두세요 (예: 팀 비밀번호 관리 도구)

3. **주기적 갱신**:
   - JWT_SECRET은 주기적으로 변경하는 것이 좋습니다
   - 카카오 Client Secret도 필요시 재발급할 수 있습니다

4. **운영 환경 분리**:
   - 가능하다면 개발/스테이징/운영 환경별로 다른 카카오 앱을 사용하세요
   - JWT Secret도 환경별로 다르게 설정하세요

---

## 🧪 배포 후 테스트

배포가 완료되면 다음을 테스트하세요:

### 1. 애플리케이션 Health Check
```bash
curl http://YOUR_EC2_IP:8080/actuator/health
```

### 2. 카카오 로그인 테스트
```
http://YOUR_EC2_IP:8080/api/auth/login
```

프론트엔드에서 카카오 로그인 플로우가 정상 작동하는지 확인하세요.

---

## 📞 문제 해결

### EC2 로그 확인
```bash
ssh -i your-key.pem ubuntu@YOUR_EC2_IP
tail -f /home/ubuntu/logs/output.log
```

### 환경변수 확인
EC2에서 애플리케이션이 실행 중일 때:
```bash
ps aux | grep java
```

### 일반적인 문제들

**1. 카카오 로그인 실패**
- Redirect URI가 카카오 콘솔과 일치하는지 확인
- KAKAO_CLIENT_ID, KAKAO_CLIENT_SECRET이 올바른지 확인

**2. JWT 토큰 검증 실패**
- JWT_SECRET이 Base64로 인코딩되었는지 확인
- 로컬과 운영 환경의 JWT_SECRET이 다른 경우 토큰 호환 안됨

**3. 데이터베이스 연결 실패**
- DB_PASSWORD가 올바른지 확인
- Supabase 방화벽 설정 확인 (EC2 IP 허용 필요)
