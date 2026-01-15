# AI 분석 API 문서

## 개요
프론트엔드의 비즈니스 로직을 백엔드로 이동시킨 **인증 불필요** AI 분석 API입니다.
프론트엔드는 데이터만 전송하면 분석 결과를 받을 수 있습니다.

## 특징
- ✅ 인증 불필요 (Authentication-free)
- ✅ RESTful API
- ✅ JSON 요청/응답
- ✅ 실시간 분석
- ✅ CORS 지원

---

## API 엔드포인트

### 1. 건강 리스크 분석
**POST** `/api/analysis/health-risk`

프론트엔드에서 하던 리스크 계산을 백엔드로 이동

**Request:**
```json
{
  "keywords": ["무릎", "통증", "병원"],
  "mentionCount": 8
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "riskLevel": "MEDIUM",
    "mentionCount": 8,
    "keywords": ["무릎", "통증", "병원"],
    "recommendation": "정기 검진을 권유해보세요",
    "color": "#F59E0B"
  },
  "message": "건강 리스크 분석 완료"
}
```

**Risk Levels:**
- `HIGH` - 10회 이상 또는 응급 키워드 포함
- `MEDIUM` - 5-9회
- `LOW` - 5회 미만

---

### 2. 감정 분석
**POST** `/api/analysis/sentiment`

대화 감정 상태 및 대화량 변화 분석

**Request:**
```json
{
  "positiveCount": 30,
  "negativeCount": 10,
  "neutralCount": 20,
  "previousTotalCount": 80,
  "currentTotalCount": 60
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "emotionStatus": "CONCERNED",
    "emoji": "😟",
    "summary": "평범한 감정 상태입니다. 대화량이 크게 감소했습니다",
    "positiveRatio": 50.0,
    "negativeRatio": 16.7,
    "neutralRatio": 33.3,
    "conversationChange": -25.0,
    "totalMessages": 60
  },
  "message": "감정 분석 완료"
}
```

**Emotion Status:**
- `POSITIVE` - 긍정 비율 > 60%
- `CONCERNED` - 부정 비율 > 40% 또는 대화량 -20% 이상 감소
- `NEUTRAL` - 그 외

---

### 3. 트렌드 분석
**POST** `/api/analysis/trend`

이전 기간 대비 변화율 계산

**Request:**
```json
{
  "previousValue": 100,
  "currentValue": 120
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "direction": "UP",
    "icon": "↑",
    "description": "20.0% 증가",
    "changePercent": 20.0,
    "previousValue": 100,
    "currentValue": 120
  },
  "message": "트렌드 분석 완료"
}
```

**Trend Directions:**
- `UP` - 10% 이상 증가
- `DOWN` - 10% 이상 감소
- `STABLE` - ±10% 이내

---

### 4. 키워드 빈도 분석
**POST** `/api/analysis/keywords`

메시지에서 키워드 추출 및 빈도 계산

**Request:**
```json
{
  "messages": [
    "요즘 무릎이 아파요",
    "병원 가야 할 것 같아요",
    "손주가 보고 싶네요"
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {"keyword": "무릎", "count": 1, "trend": "DOWN"},
    {"keyword": "병원", "count": 1, "trend": "DOWN"},
    {"keyword": "손주", "count": 1, "trend": "DOWN"}
  ],
  "message": "키워드 분석 완료"
}
```

- 2글자 이상 키워드만 추출
- 빈도순 정렬
- 상위 20개 반환

---

### 5. 대화 팁 생성
**POST** `/api/analysis/conversation-tips`

감정 상태와 키워드 기반 대화 추천

**Request:**
```json
{
  "recentKeywords": ["무릎", "병원", "손주"],
  "recentTopics": ["산책", "날씨"],
  "emotionStatus": "CONCERNED"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "questions": [
      "요즘 어떠세요? 걱정되는 일이 있으신가요?",
      "힘든 일이 있으면 언제든 말씀해주세요",
      "무릎은 좀 어떠세요? 많이 불편하신가요?",
      "병원 다녀오셨어요? 어떻게 되셨나요?",
      "산책 이야기 더 들려주세요"
    ],
    "topics": ["무릎", "병원"],
    "priority": 10,
    "category": "정서적 지원"
  },
  "message": "대화 팁 생성 완료"
}
```

**Priority Levels:**
- `10` - 긴급 (CONCERNED 상태)
- `5` - 보통 (NEUTRAL 상태)
- `3` - 낮음 (POSITIVE 상태)

---

### 6. 제품 추천
**POST** `/api/analysis/product-recommendations`

니즈와 키워드 기반 제품 추천

**Request:**
```json
{
  "needs": ["무릎이 아프다", "잠이 안 온다"],
  "keywords": ["등산", "요리"]
}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "name": "관절 건강 제품",
      "detectedNeed": "무릎이 아프다",
      "suggestion": "MSM 관절 영양제",
      "link": "https://shopping.example.com/joint-supplement",
      "priority": 10,
      "category": "건강"
    },
    {
      "name": "숙면 유도 아이템",
      "detectedNeed": "잠이 안 온다",
      "suggestion": "라벤더 아로마 세트",
      "link": "https://shopping.example.com/sleep-aid",
      "priority": 9,
      "category": "웰빙"
    }
  ],
  "message": "제품 추천 완료"
}
```

---

### 7. 대화 통계 계산
**POST** `/api/analysis/conversation-stats`

대화 통계 및 트렌드 계산

**Request:**
```json
{
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-01-07T23:59:59",
  "currentMessages": [
    {
      "content": "안녕하세요",
      "timestamp": "2025-01-01T10:00:00",
      "senderId": "user1"
    }
  ],
  "previousMessages": [
    {
      "content": "지난주 메시지",
      "timestamp": "2024-12-25T10:00:00",
      "senderId": "user1"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalMessages": 45,
    "averagePerDay": 6.4,
    "trend": "DOWN",
    "trendDescription": "대화량이 15.0% 감소했습니다",
    "changePercent": -15.0,
    "dailyDistribution": {
      "2025-01-01": 10,
      "2025-01-02": 8
    },
    "hourlyPattern": {
      "09:00": 5,
      "10:00": 8
    },
    "peakHour": "10:00",
    "periodDays": 7,
    "previousTotalMessages": 53,
    "previousAveragePerDay": 7.6
  },
  "message": "대화 통계 계산 완료"
}
```

---

### 8. 메시지 통계
**POST** `/api/analysis/message-stats`

메시지 길이 및 단답형 비율 분석

**Request:**
```json
{
  "messages": [
    {
      "content": "안녕하세요",
      "timestamp": "2025-01-01T10:00:00",
      "senderId": "user1"
    },
    {
      "content": "네",
      "timestamp": "2025-01-01T10:05:00",
      "senderId": "user2"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "averageLength": 7.5,
    "minLength": 1,
    "maxLength": 14,
    "totalCharacters": 15,
    "shortAnswerCount": 1,
    "shortAnswerRatio": 50.0
  },
  "message": "메시지 통계 계산 완료"
}
```

- **단답형**: 10자 이하 메시지

---

### 9. 종합 분석 (All-in-One)
**POST** `/api/analysis/comprehensive`

한 번의 요청으로 모든 분석 수행

**Request:**
```json
{
  "messages": ["대화 내용들..."],
  "keywords": ["키워드들..."],
  "recentTopics": ["주제들..."],
  "detectedNeeds": ["니즈들..."],
  "healthMentionCount": 5,
  "positiveCount": 20,
  "negativeCount": 5,
  "neutralCount": 15,
  "previousMessageCount": 50,
  "currentMessageCount": 40
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "healthRisk": { ... },
    "sentiment": { ... },
    "keywords": [ ... ],
    "conversationTips": { ... },
    "productRecommendations": [ ... ]
  },
  "message": "종합 분석 완료"
}
```

---

## 사용 예시

### JavaScript/TypeScript
```typescript
// 건강 리스크 분석
const analyzeHealthRisk = async (keywords: string[], count: number) => {
  const response = await fetch('http://localhost:8080/api/analysis/health-risk', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ keywords, mentionCount: count })
  });
  return await response.json();
};

// 사용
const result = await analyzeHealthRisk(['무릎', '통증'], 8);
console.log(result.data.riskLevel); // "MEDIUM"
```

### Python
```python
import requests

# 감정 분석
response = requests.post(
    'http://localhost:8080/api/analysis/sentiment',
    json={
        'positiveCount': 30,
        'negativeCount': 10,
        'neutralCount': 20,
        'previousTotalCount': 80,
        'currentTotalCount': 60
    }
)

result = response.json()
print(result['data']['emotionStatus'])  # "CONCERNED"
```

### cURL
```bash
# 키워드 분석
curl -X POST http://localhost:8080/api/analysis/keywords \
  -H "Content-Type: application/json" \
  -d '{
    "messages": ["메시지1", "메시지2", "메시지3"]
  }'
```

---

## 에러 응답

```json
{
  "success": false,
  "data": null,
  "message": "요청 데이터가 올바르지 않습니다",
  "error": "입력값 검증 실패"
}
```

**HTTP Status Codes:**
- `200` - 성공
- `400` - 잘못된 요청
- `500` - 서버 오류

---

## 프론트엔드 통합

### Before (프론트엔드에서 계산)
```typescript
// ❌ 프론트엔드에서 직접 계산
const getRiskColor = (level: string) => {
  switch (level) {
    case "high": return { bg: "bg-red-100", text: "text-red-700" }
    case "medium": return { bg: "bg-amber-100", text: "text-amber-700" }
    default: return { bg: "bg-green-100", text: "text-green-700" }
  }
}
```

### After (백엔드 API 사용)
```typescript
// ✅ 백엔드 API 호출
const result = await fetch('/api/analysis/health-risk', {
  method: 'POST',
  body: JSON.stringify({ keywords, mentionCount })
});
const { riskLevel, color, recommendation } = result.data;
```

---

## 배포 정보

**로컬 개발:**
```bash
cd backend/spring
./gradlew bootRun
```

**프로덕션:**
- Base URL: `https://your-api-server.com`
- CORS: 모든 origin 허용 (개발용)
- Rate Limiting: 없음 (추가 권장)

---

## 다음 단계

1. **AI 모델 통합**: Gemini API와 연동하여 실제 AI 분석
2. **캐싱**: Redis로 분석 결과 캐싱
3. **Rate Limiting**: 남용 방지
4. **인증 추가** (선택): API 키 기반 인증
5. **모니터링**: 분석 성능 추적

---

## 기술 스택

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Build Tool**: Gradle
- **Security**: Spring Security (인증 비활성화)
- **Documentation**: OpenAPI/Swagger (향후 추가 가능)

---

## 라이센스 & 지원

- MIT License
- Issues: GitHub Issues
- Contact: support@hyodo-signal.com
