# 🚀 배포 체크리스트

배포 전에 확인해야 할 모든 항목을 정리한 문서입니다.

## ✅ 1단계: GitHub Secrets 등록

GitHub Repository → Settings → Secrets and variables → Actions 이동

### 필수 Secrets 목록

| Secret 이름 | 값 | 상태 |
|------------|-----|------|
| `DB_PASSWORD` | `l2h5g9z0!!!` | ✅ 이미 있음 |
| `JWT_SECRET` | `8weWLVm1/ymgjvMn2r5e+UIavE+oss+a1LkWszE4KzlhJFudVY5Wf66grf/o2XVW5Kq2wEk1i+M2VpI43+HkKg==` | 🆕 추가 필요 |
| `KAKAO_CLIENT_ID` | `5e44e34fc74e2d4de17c455562b35302` | 🆕 추가 필요 |
| `KAKAO_CLIENT_SECRET` | `0QKwKXjwqtV63zFgVKoH1PnenRYgl8zN` | 🆕 추가 필요 |
| `KAKAO_REDIRECT_URI` | `http://{EC2_IP}:8080/api/auth/kakao/callback` | 🆕 추가 필요 |

⚠️ **KAKAO_REDIRECT_URI는 실제 EC2 Public IP 또는 도메인으로 변경하세요!**

예시:
```
http://43.201.123.456:8080/api/auth/kakao/callback
```

---

## ✅ 2단계: 카카오 개발자 콘솔 설정

https://developers.kakao.com/console/app 접속

### 1. Redirect URI 추가

**카카오 로그인** → **Redirect URI** 메뉴에서:

기존:
```
http://localhost:8080/api/auth/kakao/callback
```

추가:
```
http://{EC2_PUBLIC_IP}:8080/api/auth/kakao/callback
```

### 2. 동의 항목 확인

다음 항목들이 설정되어 있는지 확인:
- [ ] 닉네임 (필수 동의)
- [ ] 프로필 사진 (선택 동의)
- [ ] 카카오계정(이메일) (필수 동의)

---

## ✅ 3단계: 파일 수정 사항 확인

모든 파일이 최신 상태인지 확인하세요:

### 수정된 파일 목록
- [x] `.github/workflows/deploy.yml` - 환경변수 추가됨
- [x] `src/main/resources/application.properties` - 환경변수 바인딩 추가됨
- [x] `src/main/resources/application-secret.properties` - 로컬 개발용 설정

---

## ✅ 4단계: 배포 실행

### 방법 1: Git Push (자동 배포)
```bash
git add .
git commit -m "Add JWT and Kakao OAuth environment variables"
git push origin main
```

GitHub Actions가 자동으로 빌드 및 배포를 시작합니다.

### 방법 2: GitHub Actions 수동 실행
GitHub Repository → Actions 탭 → Deploy to EC2 → Run workflow

---

## ✅ 5단계: 배포 확인

### 1. GitHub Actions 로그 확인
- GitHub Repository → Actions 탭에서 배포 진행 상황 확인
- 모든 스텝이 ✅ 표시되는지 확인

### 2. EC2 서버 로그 확인
```bash
ssh -i kakao_ai_hack.pem ubuntu@{EC2_IP}
tail -f /home/ubuntu/logs/output.log
```

정상 시작 로그:
```
Started Application in X.XXX seconds
```

### 3. API 테스트

**Health Check**
```bash
curl http://{EC2_IP}:8080/actuator/health
```

응답:
```json
{"status":"UP"}
```

**카카오 로그인 테스트**
1. 브라우저에서 카카오 로그인 URL 접속:
```
https://kauth.kakao.com/oauth/authorize?client_id=5e44e34fc74e2d4de17c455562b35302&redirect_uri=http://{EC2_IP}:8080/api/auth/kakao/callback&response_type=code
```

2. 로그인 후 콜백 URL에서 `code` 파라미터 확인

3. 백엔드 로그인 API 호출:
```bash
curl -X POST http://{EC2_IP}:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "{인증코드}",
    "role": "CHILD"
  }'
```

성공 응답:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "token": {
      "accessToken": "...",
      "refreshToken": "..."
    }
  }
}
```

---

## 🔧 트러블슈팅

### 문제 1: "카카오 액세스 토큰 발급 실패"

**원인**: Redirect URI 불일치

**해결**:
1. GitHub Secrets의 `KAKAO_REDIRECT_URI` 확인
2. 카카오 개발자 콘솔의 Redirect URI 목록 확인
3. 두 값이 정확히 일치하는지 확인

### 문제 2: "Invalid JWT signature"

**원인**: JWT_SECRET 환경변수가 제대로 전달되지 않음

**해결**:
1. GitHub Secrets에 `JWT_SECRET`이 등록되어 있는지 확인
2. EC2에서 환경변수 확인:
```bash
ps aux | grep java
```
3. 애플리케이션 재시작

### 문제 3: 애플리케이션이 시작되지 않음

**원인**: 환경변수 누락

**해결**:
1. `/home/ubuntu/logs/output.log` 확인
2. 에러 메시지에서 누락된 환경변수 확인
3. GitHub Secrets에 해당 Secret 추가 후 재배포

### 문제 4: 데이터베이스 연결 실패

**원인**: Supabase 방화벽 설정

**해결**:
1. Supabase 대시보드 접속
2. Settings → Database → Connection Pooling
3. EC2 Public IP를 허용 목록에 추가

---

## 📊 최종 체크리스트

배포 전에 다음 항목들을 모두 확인하세요:

### GitHub Secrets
- [ ] `JWT_SECRET` 등록
- [ ] `KAKAO_CLIENT_ID` 등록
- [ ] `KAKAO_CLIENT_SECRET` 등록
- [ ] `KAKAO_REDIRECT_URI` 등록 (EC2 IP로)

### 카카오 개발자 콘솔
- [ ] 운영 환경 Redirect URI 등록
- [ ] 카카오 로그인 활성화 확인
- [ ] 동의 항목 설정 확인

### 코드 변경사항
- [ ] `deploy.yml` 수정 완료
- [ ] `application.properties` 수정 완료
- [ ] Git commit & push 완료

### 배포 후 테스트
- [ ] GitHub Actions 성공 확인
- [ ] EC2 로그 정상 확인
- [ ] Health Check 응답 확인
- [ ] 카카오 로그인 테스트 성공

---

## 🎉 배포 완료!

모든 체크리스트를 통과했다면 배포가 완료된 것입니다!

다음 단계:
1. 프론트엔드에서 API 연동
2. 실제 사용자 테스트
3. 모니터링 및 로그 확인

문제가 발생하면 `GITHUB_SECRETS_SETUP.md`와 `SETUP_GUIDE.md`를 참고하세요.
