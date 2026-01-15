# 📋 효도시그널 API 엔드포인트 요약

## 🔐 Auth (인증)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/login` | 카카오 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |
| GET | `/api/auth/me` | 내 정보 조회 |

## 👨‍👩‍👧‍👦 Family (가족 그룹)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/families` | 가족 그룹 생성 |
| POST | `/api/families/join` | 초대 코드로 가입 |
| GET | `/api/families/my` | 내 가족 목록 |
| GET | `/api/families/{familyId}` | 특정 가족 조회 |
| DELETE | `/api/families/{familyId}/leave` | 가족 그룹 탈퇴 |

## 💬 Conversation (대화)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/conversations` | 대화방 생성 |
| POST | `/api/conversations/messages` | 메시지 일괄 업로드 |
| GET | `/api/conversations?familyId={id}` | 가족의 대화방 목록 |
| GET | `/api/conversations/{conversationId}` | 대화방 상세 (메시지 포함) |
| GET | `/api/conversations/{id}/messages` | 특정 기간 메시지 조회 |
| GET | `/api/conversations/family/{id}/recent` | 최근 N일 메시지 |
| GET | `/api/conversations/unanalyzed-images` | 미분석 이미지 메시지 |

## 🤖 Analysis (AI 분석)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/analysis/run` | 수동 분석 실행 |
| POST | `/api/analysis/health` | 건강 분석만 실행 |
| POST | `/api/analysis/run-all` | 전체 가족 분석 (관리자) |

## 📊 Insight (인사이트)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/insights/health` | 건강 인사이트 조회 |
| GET | `/api/insights/emotion` | 감정 인사이트 조회 |
| GET | `/api/insights/needs` | 니즈 인사이트 조회 |
| GET | `/api/insights/health/high-risk` | 고위험 건강 인사이트 |
| GET | `/api/insights/emotion/negative` | 부정적 감정 인사이트 |
| GET | `/api/insights/needs/high-priority` | 고우선순위 니즈 인사이트 |

## 📝 Report (주간 리포트)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/reports/generate` | 주간 리포트 생성 |
| GET | `/api/reports/latest` | 최신 리포트 조회 |
| GET | `/api/reports?familyId={id}` | 모든 리포트 조회 |
| POST | `/api/reports/generate-all` | 전체 가족 리포트 생성 (관리자) |

## 🚨 Alert (긴급 알림)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/alerts/unacknowledged` | 미확인 긴급 알림 조회 |
| POST | `/api/alerts/{alertId}/acknowledge` | 알림 확인 처리 |
| POST | `/api/alerts/detect` | 긴급 상황 감지 수동 실행 |

---

## 📊 총 엔드포인트 수: **31개**

- **Auth**: 4개
- **Family**: 5개
- **Conversation**: 7개
- **Analysis**: 3개
- **Insight**: 6개
- **Report**: 4개
- **Alert**: 3개

---

## 🔑 인증

모든 API는 JWT 토큰 필요 (Auth API 제외)
```http
Authorization: Bearer {accessToken}
```

---

## 🎯 핵심 플로우별 엔드포인트

### 1️⃣ 회원가입 & 초기 설정
```
POST /api/auth/login          # 카카오 로그인
POST /api/families             # 가족 그룹 생성
POST /api/families/join        # 초대 코드로 가입
```

### 2️⃣ 대화 데이터 관리
```
POST /api/conversations        # 대화방 생성
POST /api/conversations/messages  # 메시지 업로드
```

### 3️⃣ 리포트 확인
```
GET /api/reports/latest        # 최신 리포트
GET /api/insights/health       # 건강 인사이트
GET /api/insights/emotion      # 감정 인사이트
GET /api/insights/needs        # 니즈 인사이트
```

### 4️⃣ 긴급 상황 대응
```
GET /api/alerts/unacknowledged # 미확인 알림
POST /api/alerts/{id}/acknowledge  # 알림 확인
```

---

## 📅 자동 실행 (스케줄러)

### 매일 자정 (00:00)
- AI 분석 자동 실행
- 긴급 상황 감지

### 매주 금요일 오후 3시 (15:00)
- 주간 리포트 자동 생성

---

## 🚀 수동 트리거 API (관리자/테스트용)

```
POST /api/analysis/run-all      # 전체 분석 실행
POST /api/reports/generate-all  # 전체 리포트 생성
POST /api/alerts/detect         # 긴급 상황 감지
```

---

**Base URL**: `http://localhost:8080` (로컬)
**Base URL**: `http://your-server:8080` (배포)
