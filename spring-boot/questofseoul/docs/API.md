# Quest of Seoul API 문서

## 개요

- **Base URL:** `/api/v1`
- **인증:** JWT Bearer Token 또는 세션(쿠키) 인증 지원
- **Content-Type:** `application/json` (파일 업로드 제외)

---

## 공통 사항

### 인증 헤더

```
Authorization: Bearer <accessToken>
```

### 공통 에러 응답

```json
{
  "error": "Bad Request",
  "errorCode": "VALIDATION_FAILED",
  "message": "입력값 검증에 실패했습니다.",
  "timestamp": "2026-02-11T12:00:00",
  "path": "/api/v1/auth/login",
  "errors": { "email": "이메일을 입력해 주세요." }
}
```

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 |
| 500 | 서버 오류 |

---

## 1. 인증 (Auth)

**Base Path:** `/api/v1/auth`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/login` | 이메일/비밀번호 로그인 → JWT 발급 |
| POST | `/register` | 회원가입 → JWT 발급 |
| GET | `/me` | 현재 사용자 조회 (JWT 또는 세션) |
| POST | `/token` | OAuth2 세션 → JWT 발급 (세션 필요) |

---

## 2. 파일 업로드

**Base Path:** `/api/v1/upload`

**인증:** Bearer JWT 또는 세션

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/` | multipart/form-data, S3 업로드 후 URL 반환 |

**Request (multipart/form-data)**

| 필드 | 필수 | 설명 |
|------|------|------|
| file | O | 업로드할 파일 |
| type | X | "image" \| "audio" (미지정 시 Content-Type으로 판별) |

**Response 200:** `{ "url": "https://s3.../path/file.jpg" }`

---

## 3. 투어 (Tour)

**Base Path:** `/api/v1`

### 3.1 투어 목록

```
GET /api/v1/tours
```

**Response 200**

```json
[
  { "id": 1, "externalKey": "gyeongbokgung", "title": "경복궁 핵심 투어" }
]
```

### 3.2 투어 디테일

```
GET /api/v1/tours/{tourId}
```

접근 상태, 진행 Run, 액션 버튼, 맵 스팟 등 포함.

**Response 200**

```json
{
  "tourId": 1,
  "title": "경복궁 핵심 투어",
  "description": "...",
  "tags": [{ "id": 1, "name": "역사", "slug": "history" }],
  "counts": { "main": 8, "sub": 12, "photo": 5, "treasure": 3, "missions": 4 },
  "info": { "entrance_fee": {...}, "available_hours": [...], "estimated_duration_min": 90 },
  "goodToKnow": ["한복 입장 무료", "편한 신발 추천"],
  "startSpot": { "spotId": 1, "title": "광화문", "lat": 37.576, "lng": 126.977, "radiusM": 60 },
  "mapSpots": [{ "spotId": 1, "type": "MAIN", "title": "광화문", "lat": 37.576, "lng": 126.977 }],
  "access": { "status": "UNLOCKED", "hasAccess": true },
  "currentRun": { "runId": 101, "status": "IN_PROGRESS", "startedAt": "...", "progress": { "completedSpots": 2, "totalSpots": 8 } },
  "actions": { "primaryButton": "START", "secondaryButton": null, "moreActions": ["RESTART"] }
}
```

### 3.3 마커 목록

```
GET /api/v1/tours/{tourId}/markers
```

**Query:** `filter` — STEP, WAYPOINT, PHOTO_SPOT, TREASURE (미지정 시 전체)

**Response 200**

```json
[
  {
    "id": 1,
    "type": "STEP",
    "title": "광화문",
    "latitude": 37.576,
    "longitude": 126.977,
    "radiusM": 60,
    "refId": 1,
    "stepOrder": 1
  }
]
```

### 3.4 Unlock

```
POST /api/v1/tours/{tourId}/access/unlock
```

**인증:** Bearer JWT

**Response 200:** 빈 본문

### 3.5 Run 처리

```
POST /api/v1/tours/{tourId}/runs
```

**인증:** Bearer JWT

**Query:** `mode` — START | CONTINUE | RESTART  
**Body (optional):** `{ "mode": "START" }`

**Response 200**

```json
{
  "runId": 101,
  "tourId": 1,
  "status": "IN_PROGRESS",
  "mode": "START",
  "startSpot": { "spotId": 1, "title": "광화문", "lat": 37.576, "lng": 126.977, "radiusM": 60 }
}
```

### 3.6 근접 감지

```
POST /api/v1/tour-runs/{runId}/proximity
```

**인증:** Bearer JWT

**Query:** `lang` (ko, en 등)

**Request Body**

```json
{ "latitude": 37.5796, "longitude": 126.9769 }
```

**Response 200** (근접 스팟 있을 때) / **204** (없을 때)

```json
{
  "event": "PROXIMITY",
  "contentType": "GUIDE",
  "sessionId": 201,
  "context": { "refType": "STEP", "refId": 1, "placeName": "광화문" },
  "messages": [
    {
      "turnId": 501,
      "role": "ASSISTANT",
      "source": "GUIDE",
      "text": "광화문에 오신 것을 환영합니다...",
      "assets": [{"id": 1, "type": "IMAGE", "url": "https://...", "meta": null}],
      "action": {"type": "NEXT_STEP", "label": "다음", "stepId": 2}
    }
  ]
}
```

### 3.7 채팅 세션 조회/생성

```
GET /api/v1/tour-runs/{runId}/spots/{spotId}/chat-session
```

**인증:** Bearer JWT

**Response 200**

```json
{ "sessionId": 201 }
```

### 3.8 채팅 히스토리

```
GET /api/v1/chat-sessions/{sessionId}/turns
```

**인증:** Bearer JWT

**Response 200**

```json
{
  "sessionId": 201,
  "turns": [
    { "id": 501, "role": "USER", "source": "USER", "text": "이 건물의 역사가 궁금해요", "assets": null, "action": null, "createdAt": "..." },
    { "id": 502, "role": "ASSISTANT", "source": "AI", "text": "근정전은 1395년...", "assets": null, "action": null, "createdAt": "..." }
  ]
}
```

### 3.9 채팅 메시지 전송

```
POST /api/v1/chat-sessions/{sessionId}/messages
```

**인증:** Bearer JWT

**Request Body:** `{ "text": "이 건물의 역사가 궁금해요" }`

**Response 200**

```json
{
  "userTurnId": 503,
  "userText": "이 건물의 역사가 궁금해요",
  "aiTurnId": 504,
  "aiText": "근정전은 1395년 태조에 의해 건축된 조선의 정전입니다..."
}
```

### 3.10 스팟 가이드

```
GET /api/v1/spots/{spotId}/guide
```

**Query:** `lang` (ko, en 등)

**Response 200**

```json
{
  "stepId": 1,
  "stepTitle": "광화문",
  "segments": [
    {
      "id": 10,
      "segIdx": 1,
      "textEn": "광화문에 오신 것을 환영합니다! 이곳은 경복궁의 정문으로...",
      "triggerKey": null,
      "media": [{"id": 1, "url": "https://...", "meta": null}]
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| stepId | 스팟 ID (spotId와 동일) |
| stepTitle | 스팟 제목 |
| segments[].textEn | 가이드 문장 |

### 3.11 미션 제출

```
POST /api/v1/tour-runs/{runId}/steps/{stepId}/missions/submit
```

**인증:** Bearer JWT

**Request Body**

```json
{
  "userInput": "사용자 입력 텍스트",
  "photoUrl": "https://s3.../photo.jpg",
  "selectedOption": { "key": "A" }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| userInput | string | 텍스트 입력 (TEXT_INPUT 등) |
| photoUrl | string | 업로드된 사진 URL (PHOTO) |
| selectedOption | object | 선택 옵션 (CHOICE 등) |

**Response 200**

```json
{
  "attemptId": 1,
  "success": true,
  "isCorrect": true,
  "score": 100,
  "feedback": "정답입니다!"
}
```

---

## 4. 관리자 API

**Base Path:** `/api/v1/admin`  
**인증:** 세션 (sessionAuth)

### 4.1 Tour CRUD

**Base Path:** `/api/v1/admin/tours`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 목록 (page, size) |
| GET | `/{tourId}` | 단건 |
| POST | `/` | 생성 |
| PATCH | `/{tourId}` | 수정 |
| DELETE | `/{tourId}` | 삭제 |

**TourCreateRequest**

```json
{
  "externalKey": "gyeongbokgung",
  "titleEn": "Gyeongbokgung Palace",
  "descriptionEn": "The main royal palace.",
  "infoJson": {},
  "goodToKnowJson": {}
}
```

**TourAdminResponse**

```json
{
  "id": 1,
  "externalKey": "gyeongbokgung",
  "titleEn": "Gyeongbokgung Palace",
  "descriptionEn": "...",
  "infoJson": {},
  "goodToKnowJson": {},
  "mainCount": 8,
  "subCount": 12,
  "photoSpotsCount": 5,
  "treasuresCount": 3,
  "missionsCount": 4
}
```

### 4.2 Tour Spot CRUD

**Base Path:** `/api/v1/admin/tours/{tourId}/spots`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | Spot 목록 |
| GET | `/{spotId}` | 단건 |
| POST | `/` | 생성 |
| PATCH | `/{spotId}` | 수정 |
| DELETE | `/{spotId}` | 삭제 |

**SpotCreateRequest**

```json
{
  "type": "MAIN",
  "title": "광화문",
  "description": "경복궁 정문",
  "latitude": 37.576,
  "longitude": 126.977,
  "orderIndex": 1,
  "radiusM": 60
}
```

**SpotType:** MAIN, SUB, PHOTO, TREASURE

**SpotAdminResponse**

```json
{
  "id": 1,
  "tourId": 1,
  "type": "MAIN",
  "title": "광화문",
  "description": "...",
  "latitude": 37.576,
  "longitude": 126.977,
  "radiusM": 60,
  "orderIndex": 1
}
```

### 4.4 Spot 가이드 (Guide)

**Base Path:** `/api/v1/admin/tours/{tourId}/spots/{spotId}/guide`

스팟 가이드 문장 및 미디어(이미지/오디오) 관리. 미디어 URL은 `POST /api/v1/upload`로 S3 업로드 후 사용.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 가이드 조회 |
| PUT | `/` | 가이드 전체 덮어쓰기 |

**GuideSaveRequest**

```json
{
  "language": "ko",
  "stepTitle": "광화문",
  "lines": [
    {
      "text": "광화문에 오신 것을 환영합니다.",
      "assets": [
        {
          "url": "https://s3.../image.jpg",
          "assetType": "IMAGE",
          "usage": "ILLUSTRATION"
        },
        {
          "url": "https://s3.../audio.mp3",
          "assetType": "AUDIO",
          "usage": "SCRIPT_AUDIO"
        }
      ]
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| language | 언어 (ko, en 등) |
| stepTitle | 스텝 제목 |
| lines[].text | 가이드 문장 |
| lines[].assets[].url | S3 업로드 API로 얻은 URL |
| lines[].assets[].assetType | IMAGE \| AUDIO |
| lines[].assets[].usage | ILLUSTRATION \| SCRIPT_AUDIO |

**Response 200 (GET/PUT):** `GuideAdminResponse` (동일 구조)

### 4.5 Enum

```
GET /api/v1/admin/enums/{enumName}
```

**enumName:** language, spotType, markerType, stepKind

**Response 200**

```json
["ko", "en", "ja"]
```

---

## Swagger UI

개발 환경: `/swagger-ui.html` 또는 `/swagger-ui/index.html`
