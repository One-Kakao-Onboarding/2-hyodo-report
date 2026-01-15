# 🚀 효도시그널 API 문서

## 📋 목차
1. [인증 (Auth)](#인증-auth)
2. [가족 그룹 (Family)](#가족-그룹-family)
3. [대화 데이터 (Conversation)](#대화-데이터-conversation)
4. [AI 분석 (Analysis)](#ai-분석-analysis)
5. [인사이트 (Insight)](#인사이트-insight)
6. [주간 리포트 (Report)](#주간-리포트-report)
7. [긴급 알림 (Alert)](#긴급-알림-alert)

---

## 🔐 인증 (Auth)

### 1. 카카오 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "code": "카카오 인증 코드",
  "role": "CHILD" // 또는 "PARENT"
}
```

**응답:**
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
      "accessToken": "eyJhbGc...",
      "refreshToken": "eyJhbGc...",
      "tokenType": "Bearer",
      "expiresIn": 3600000
    },
    "isNewUser": true
  },
  "message": "로그인 성공"
}
```

### 2. 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}
```

### 3. 로그아웃
```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

### 4. 내 정보 조회
```http
GET /api/auth/me
Authorization: Bearer {accessToken}
```

---

## 👨‍👩‍👧‍👦 가족 그룹 (Family)

### 1. 가족 그룹 생성
```http
POST /api/families
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "우리 가족",
  "role": "CHILD",
  "nickname": "아들"
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "우리 가족",
    "inviteCode": "123456",
    "members": [
      {
        "id": 1,
        "userId": 1,
        "userNickname": "홍길동",
        "userProfileImageUrl": "https://...",
        "role": "CHILD",
        "nickname": "아들",
        "joinedAt": "2024-01-15T10:30:00"
      }
    ],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "message": "가족 그룹이 생성되었습니다."
}
```

### 2. 초대 코드로 가입
```http
POST /api/families/join
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "inviteCode": "123456",
  "role": "PARENT",
  "nickname": "엄마"
}
```

### 3. 내 가족 목록 조회
```http
GET /api/families/my
Authorization: Bearer {accessToken}
```

### 4. 특정 가족 그룹 조회
```http
GET /api/families/{familyId}
Authorization: Bearer {accessToken}
```

### 5. 가족 그룹 탈퇴
```http
DELETE /api/families/{familyId}/leave
Authorization: Bearer {accessToken}
```

---

## 💬 대화 데이터 (Conversation)

### 1. 대화방 생성
```http
POST /api/conversations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "familyId": 1,
  "name": "엄마와의 대화"
}
```

### 2. 메시지 일괄 업로드
```http
POST /api/conversations/messages
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "conversationId": 1,
  "messages": [
    {
      "senderId": 1,
      "type": "TEXT",
      "content": "엄마 요즘 건강은 어떠세요?",
      "sentAt": "2024-01-15T10:30:00"
    },
    {
      "senderId": 2,
      "type": "TEXT",
      "content": "괜찮아. 무릎이 좀 아프긴 한데...",
      "sentAt": "2024-01-15T10:31:00"
    },
    {
      "senderId": 2,
      "type": "IMAGE",
      "imageUrl": "https://...",
      "sentAt": "2024-01-15T10:32:00"
    }
  ]
}
```

### 3. 가족의 모든 대화방 조회
```http
GET /api/conversations?familyId=1
Authorization: Bearer {accessToken}
```

### 4. 대화방 상세 조회 (메시지 포함)
```http
GET /api/conversations/{conversationId}
Authorization: Bearer {accessToken}
```

### 5. 특정 기간 메시지 조회
```http
GET /api/conversations/{conversationId}/messages?start=2024-01-01T00:00:00&end=2024-01-15T23:59:59
Authorization: Bearer {accessToken}
```

### 6. 최근 N일 메시지 조회
```http
GET /api/conversations/family/{familyId}/recent?days=7
Authorization: Bearer {accessToken}
```

---

## 🤖 AI 분석 (Analysis)

### 1. 수동 분석 실행
```http
POST /api/analysis/run?familyId=1&days=7
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "success": true,
  "data": null,
  "message": "가족 ID 1의 최근 7일 대화 분석이 완료되었습니다."
}
```

### 2. 전체 가족 분석 실행 (관리자용)
```http
POST /api/analysis/run-all
Authorization: Bearer {accessToken}
```

---

## 📊 인사이트 (Insight)

### 1. 건강 인사이트 조회
```http
GET /api/insights/health?familyId=1&days=7
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "keywords": "[\"무릎 통증\", \"당뇨\"]",
      "severity": 7,
      "summary": "무릎 통증을 자주 호소하시며, 당뇨 관련 언급이 있었습니다.",
      "recommendation": "정형외과 검진을 권장하며, 혈당 관리에 주의가 필요합니다.",
      "analyzedAt": "2024-01-15T00:00:00",
      "createdAt": "2024-01-15T00:05:00"
    }
  ]
}
```

### 2. 감정 인사이트 조회
```http
GET /api/insights/emotion?familyId=1&days=7
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "emotionType": "외로움",
      "emotionScore": -5,
      "description": "최근 외로움을 표현하는 대화가 증가했습니다.",
      "conversationTips": "[\"요즘 어떻게 지내세요?\", \"주말에 같이 시간 보낼까요?\"]",
      "analyzedAt": "2024-01-15T00:00:00",
      "createdAt": "2024-01-15T00:05:00"
    }
  ]
}
```

### 3. 니즈 인사이트 조회
```http
GET /api/insights/needs?familyId=1&days=7
Authorization: Bearer {accessToken}
```

### 4. 고위험 건강 인사이트 조회
```http
GET /api/insights/health/high-risk?familyId=1
Authorization: Bearer {accessToken}
```

### 5. 부정적 감정 인사이트 조회
```http
GET /api/insights/emotion/negative?familyId=1
Authorization: Bearer {accessToken}
```

### 6. 고우선순위 니즈 인사이트 조회
```http
GET /api/insights/needs/high-priority?familyId=1
Authorization: Bearer {accessToken}
```

---

## 📝 주간 리포트 (Report)

### 1. 주간 리포트 수동 생성
```http
POST /api/reports/generate?familyId=1
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "familyId": 1,
    "periodStart": "2024-01-08T00:00:00",
    "periodEnd": "2024-01-14T23:59:59",
    "summary": "이번 주 부모님은 무릎 통증을 자주 호소하셨고, 약간의 외로움을 느끼고 계신 것 같습니다. 온열 찜질기나 홍삼 스틱 같은 건강 제품이 도움이 될 수 있습니다.",
    "healthSummary": "• 무릎 통증을 자주 호소 (심각도: 7/10)\n• 당뇨 관련 언급",
    "emotionSummary": "• 외로움 (감정: 외로움, 점수: -5/10)",
    "needsSummary": "• [건강/의료] 무릎 통증 완화 제품 필요 (우선순위: 8/10)",
    "conversationTips": [
      {
        "id": 1,
        "content": "지난번 무릎 아프시다던데, 병원은 다녀오셨어요?",
        "priority": 9,
        "category": "건강 관심"
      },
      {
        "id": 2,
        "content": "요즘 외롭지 않으세요? 주말에 같이 시간 보낼까요?",
        "priority": 7,
        "category": "감정 케어"
      },
      {
        "id": 3,
        "content": "홍삼 스틱 한 번 드셔보시는 건 어떠세요?",
        "priority": 6,
        "category": "취미 공유"
      }
    ],
    "generatedAt": "2024-01-15T15:00:00",
    "createdAt": "2024-01-15T15:00:00"
  },
  "message": "주간 리포트가 생성되었습니다."
}
```

### 2. 최신 주간 리포트 조회
```http
GET /api/reports/latest?familyId=1
Authorization: Bearer {accessToken}
```

### 3. 모든 주간 리포트 조회
```http
GET /api/reports?familyId=1
Authorization: Bearer {accessToken}
```

### 4. 전체 가족 리포트 생성 (관리자용)
```http
POST /api/reports/generate-all
Authorization: Bearer {accessToken}
```

---

## 🚨 긴급 알림 (Alert)

### 1. 미확인 긴급 알림 조회
```http
GET /api/alerts/unacknowledged?familyId=1
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "familyId": 1,
      "alertType": "HEALTH_EMERGENCY",
      "title": "🚨 건강 긴급 상황 감지",
      "content": "최근 대화에서 건강 관련 긴급 키워드가 감지되었습니다.\n감지된 키워드: 응급실, 입원\n관련 메시지: 2건\n\n즉시 부모님께 연락하여 상황을 확인해주세요.",
      "severity": 9,
      "detectedKeywords": "응급실, 입원",
      "aiAnalysis": "실제 응급실 방문이 있었던 것으로 판단됩니다. 즉시 연락이 필요합니다.",
      "acknowledged": false,
      "acknowledgedAt": null,
      "createdAt": "2024-01-15T14:30:00"
    }
  ]
}
```

### 2. 긴급 알림 확인 처리
```http
POST /api/alerts/{alertId}/acknowledge
Authorization: Bearer {accessToken}
```

### 3. 긴급 상황 감지 수동 실행
```http
POST /api/alerts/detect?familyId=1
Authorization: Bearer {accessToken}
```

---

## 📌 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": { /* 데이터 */ },
  "message": "성공 메시지",
  "error": null
}
```

### 오류 응답
```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": "오류 메시지"
}
```

---

## 🔑 인증 헤더

모든 API 요청 시 JWT 토큰 필요 (Auth API 제외):

```http
Authorization: Bearer {accessToken}
```

---

## 📅 스케줄러 자동 실행

### 1. 매일 자정 (00:00)
- 모든 가족의 AI 분석 실행
- 긴급 상황 감지

### 2. 매주 금요일 오후 3시 (15:00)
- 모든 가족의 주간 리포트 생성
- 알림톡 발송 (추후 구현)

---

## 🎯 주요 플로우

### 1. 초기 설정
```
1. 카카오 로그인 (POST /api/auth/login)
2. 가족 그룹 생성 (POST /api/families)
3. 초대 코드 공유
4. 부모님 가입 (POST /api/families/join)
```

### 2. 대화 데이터 업로드
```
1. 대화방 생성 (POST /api/conversations)
2. 메시지 업로드 (POST /api/conversations/messages)
3. AI 분석 자동 실행 (매일 자정)
```

### 3. 리포트 확인
```
1. 최신 리포트 조회 (GET /api/reports/latest)
2. 대화 치트키 확인
3. 인사이트 상세 조회 (GET /api/insights/*)
```

### 4. 긴급 상황 대응
```
1. 미확인 알림 조회 (GET /api/alerts/unacknowledged)
2. 알림 내용 확인
3. 부모님께 연락
4. 알림 확인 처리 (POST /api/alerts/{alertId}/acknowledge)
```

---

## 🚀 다음 단계

1. **프론트엔드 연동** - 위 API 활용
2. **알림톡 연동** - 카카오 비즈니스 메시지
3. **이미지 업로드** - S3 또는 클라우드 스토리지
4. **실시간 알림** - WebSocket 또는 FCM

---

**Base URL**: `http://your-server:8080`

**API 버전**: v1

**문서 버전**: 1.0.0

**마지막 업데이트**: 2024-01-15
