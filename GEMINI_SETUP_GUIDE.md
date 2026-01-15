# 🤖 Gemini API 설정 가이드

## ✅ 완료된 작업

### 1. Gemini 모델명 수정 ✅
- ❌ `gemini-3-flash` (잘못된 모델명)
- ✅ `gemini-1.5-flash` (올바른 모델명)

### 2. 환경변수 설정 완료 ✅

**application.properties**에 추가됨:
```properties
# Gemini AI Configuration (environment variables will override these)
gemini.api-key=${GEMINI_API_KEY:default_gemini_key}
gemini.model=${GEMINI_MODEL:gemini-1.5-flash}
gemini.api-url=${GEMINI_API_URL:https://generativelanguage.googleapis.com/v1beta/models}
```

**deploy.yml**에 추가됨:
- Build 단계: `GEMINI_API_KEY` 환경변수 추가
- Restart 단계: `GEMINI_API_KEY` 환경변수 추가
- Java 실행 시: `GEMINI_API_KEY` 환경변수 전달

---

## 🔑 GitHub Secrets 설정 필수!

### GitHub에서 설정해야 할 환경변수

GitHub 레포 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

다음 Secret을 추가하세요:

```
Name: GEMINI_API_KEY
Value: AIzaSyDlh_kGoSZhs6-ajfyf7OM9MH0WWpS53iE
```

---

## 📋 전체 환경변수 체크리스트

배포 시 필요한 **모든 GitHub Secrets**:

| Secret Name | 설명 | 상태 |
|------------|------|------|
| `DB_PASSWORD` | PostgreSQL 비밀번호 | 기존 설정 |
| `JWT_SECRET` | JWT 비밀키 | 기존 설정 |
| `KAKAO_CLIENT_ID` | 카카오 클라이언트 ID | 기존 설정 |
| `KAKAO_CLIENT_SECRET` | 카카오 클라이언트 Secret | 기존 설정 |
| `KAKAO_REDIRECT_URI` | 카카오 리다이렉트 URI | 기존 설정 |
| **`GEMINI_API_KEY`** | **Gemini API 키** | **⚠️ 추가 필요** |
| `EC2_HOST` | EC2 서버 주소 | 기존 설정 |
| `EC2_USERNAME` | EC2 사용자명 | 기존 설정 |
| `EC2_KEY` | EC2 SSH 키 | 기존 설정 |
| `EC2_PORT` | EC2 SSH 포트 | 기존 설정 |

---

## 🧪 로컬 테스트 (선택)

로컬에서 Gemini API가 잘 작동하는지 테스트하려면:

```bash
# 환경변수 설정 (현재 터미널 세션에서만 유효)
export GEMINI_API_KEY=AIzaSyDlh_kGoSZhs6-ajfyf7OM9MH0WWpS53iE

# Spring Boot 실행
./gradlew bootRun
```

또는 IntelliJ/Eclipse에서 실행 시 Environment Variables에 추가:
```
GEMINI_API_KEY=AIzaSyDlh_kGoSZhs6-ajfyf7OM9MH0WWpS53iE
```

---

## 🚀 배포 플로우

1. **GitHub Secrets 설정** (`GEMINI_API_KEY` 추가)
2. **코드 푸시** (main 브랜치)
3. **자동 배포** (GitHub Actions)
   - Build 시: Gemini API 키로 빌드
   - Deploy 시: EC2에 환경변수 전달
   - 실행 시: Spring Boot에 환경변수 주입

---

## 🔍 배포 후 확인 방법

### 1. GitHub Actions 로그 확인
- Actions 탭에서 배포 워크플로우 확인
- "Build with Gradle" 단계에서 에러 없는지 확인
- "Restart Spring Boot App" 단계에서 "✅ Deployment Successful!" 메시지 확인

### 2. EC2 서버 로그 확인
```bash
# SSH 접속 후
tail -f /home/ubuntu/logs/output.log

# Gemini 관련 로그 검색
grep -i "gemini" /home/ubuntu/logs/output.log
grep -i "api.*key" /home/ubuntu/logs/output.log
```

### 3. API 테스트
분석 스케줄러가 실행될 때 Gemini API가 호출됩니다:
- 매일 자정 (00:00): AI 분석 실행
- 매주 금요일 오후 3시 (15:00): 주간 리포트 생성

---

## ⚠️ 트러블슈팅

### 문제: "Gemini API 호출 실패"

**원인**:
1. GitHub Secret `GEMINI_API_KEY`가 설정되지 않음
2. API 키가 잘못됨
3. API 할당량 초과

**해결**:
```bash
# EC2에서 환경변수 확인
echo $GEMINI_API_KEY

# 로그에서 에러 메시지 확인
grep "Gemini" /home/ubuntu/logs/output.log | tail -20
```

### 문제: "cannot find symbol: class GeminiClient"

**원인**: 컴파일 에러 (이미 해결됨)

**확인**:
```bash
./gradlew clean build -x test
# BUILD SUCCESSFUL이면 OK
```

---

## 📌 중요 사항

1. ⚠️ **API 키 노출 금지**
   - `application-secret.properties`는 `.gitignore`에 포함됨
   - GitHub에 절대 푸시하지 마세요

2. 💰 **API 할당량 확인**
   - Gemini 1.5 Flash 무료 할당량: 월 1,500회
   - https://aistudio.google.com/app/apikey 에서 확인

3. 🔄 **환경변수 우선순위**
   ```
   GitHub Secrets (배포 시)
   ↓
   환경변수 (런타임)
   ↓
   application-secret.properties (로컬)
   ↓
   application.properties (기본값)
   ```

---

## ✅ 최종 체크리스트

- [x] Gemini 모델명 수정 (`gemini-1.5-flash`)
- [x] `application.properties`에 환경변수 설정 추가
- [x] `deploy.yml`에 `GEMINI_API_KEY` 추가 (3곳)
- [ ] **GitHub Secrets에 `GEMINI_API_KEY` 추가** ⬅️ 지금 하세요!
- [x] 빌드 테스트 완료 (`BUILD SUCCESSFUL`)

---

🎉 **설정 완료!** GitHub Secrets에 `GEMINI_API_KEY`만 추가하면 배포 준비 끝!
