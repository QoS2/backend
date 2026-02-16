# Quest of Seoul API 문서

## 개요

- **Base URL:** `/api/v1`
- **인증:** JWT Bearer Token 또는 세션(쿠키) 인증 지원
- **Content-Type:** `application/json` (파일 업로드 제외)

---

## 목차

1. [공통 사항](#공통-사항)
2. [인증 (Auth)](#1-인증-auth)
3. [파일 업로드](#2-파일-업로드)
4. [투어 (Tour)](#3-투어-tour)
5. [관리자 API](#4-관리자-api)
6. [Swagger UI](#swagger-ui)
7. [수집 API (Place·Treasure·Photo Spot)](#수집-api-placetreasurephoto-spot)

---

## 공통 사항

### 인증 헤더

| 방식 | 헤더 |
|------|------|
| JWT | `Authorization: Bearer <accessToken>` |
| 세션 | 쿠키 기반 (관리자 API) |

### 공통 에러 응답

모든 에러는 다음 형식으로 반환됩니다:

```json
{
  "error": "Bad Request",
  "errorCode": "VALIDATION_FAILED",
  "message": "입력값 검증에 실패했습니다.",
  "timestamp": "2026-02-11T12:00:00",
  "path": "/api/v1/auth/login",
  "errors": {
    "email": "이메일을 입력해 주세요.",
    "password": "비밀번호를 입력해 주세요."
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| error | string | HTTP 상태 설명 (예: Bad Request) |
| errorCode | string | 애플리케이션 에러 코드 |
| message | string | 사용자용 에러 메시지 |
| timestamp | string | ISO 8601 타임스탬프 |
| path | string | 요청 경로 (선택) |
| errors | object | 필드별 검증 에러 (선택, `VALIDATION_FAILED` 시) |

### 에러 코드 목록

| errorCode | HTTP | 설명 |
|-----------|------|------|
| VALIDATION_FAILED | 400 | `@Valid` 검증 실패 |
| INVALID_REQUEST_BODY | 400 | JSON 파싱 실패 |
| INVALID_PARAMETER_TYPE | 400 | 파라미터 타입 불일치 |
| ILLEGAL_ARGUMENT | 400 | 잘못된 인자 |
| NOT_FOUND | 404 | 리소스 없음 |
| UNAUTHORIZED | 401 | 인증 필요 |
| AUTHENTICATION_FAILED | 401 | 토큰/세션 인증 실패 |
| AUTHORIZATION_FAILED | 403 | 권한 없음 |
| DUPLICATE_RESOURCE | 409 | 리소스 중복 |
| ILLEGAL_STATE | 409 | 상태 충돌 |
| INTERNAL_SERVER_ERROR | 500 | 서버 오류 |

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (No Content) |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복, 상태 오류) |
| 500 | 서버 오류 |

---

## 1. 인증 (Auth)

**Base Path:** `/api/v1/auth`

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/login` | - | 이메일/비밀번호 로그인 → JWT 발급 |
| POST | `/register` | - | 회원가입 → JWT 발급 |
| GET | `/me` | JWT 또는 세션 | 현재 사용자 조회 |
| POST | `/token` | 세션 | OAuth2 세션 → JWT 발급 |

---

### 1.1 로그인

```
POST /api/v1/auth/login
Content-Type: application/json
```

**Request Body (LoginRequest)**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 (형식 검증) |
| password | string | O | 비밀번호 |

**검증 규칙**
- `email`: `@NotBlank`, `@Email`
- `password`: `@NotBlank`

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

| 필드 | 설명 |
|------|------|
| accessToken | JWT 액세스 토큰 |
| expiresIn | 토큰 만료 시간 (초) |
| tokenType | `"Bearer"` |

---

### 1.2 회원가입

```
POST /api/v1/auth/register
Content-Type: application/json
```

**Request Body (RegisterRequest)**

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "홍길동"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 (형식 검증) |
| password | string | O | 비밀번호 (8자 이상) |
| nickname | string | X | 닉네임 |

**검증 규칙**
- `email`: `@NotBlank`, `@Email`
- `password`: `@NotBlank`, `@Size(min=8)`
- `nickname`: 선택

**Response 201**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

---

### 1.3 현재 사용자 조회

```
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

**Response 200**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 401** — 비로그인

---

### 1.4 OAuth2 → JWT 토큰 발급

```
POST /api/v1/auth/token
Cookie: JSESSIONID=...
```

세션 인증 후 JWT를 발급합니다. OAuth2 로그인 직후 앱에서 JWT로 전환할 때 사용.

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

---

## 2. 파일 업로드

**Base Path:** `/api/v1/upload`  
**인증:** Bearer JWT 또는 세션 필수

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/` | multipart/form-data로 S3 업로드 후 URL 반환 |

---

### 2.1 파일 업로드

```
POST /api/v1/upload
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

**Request (multipart/form-data)**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| file | file | O | 업로드할 파일 |
| type | string | X | `"image"` \| `"audio"` — 미지정 시 Content-Type으로 판별 |
| category | string | X | 폴더 카테고리 (tour, spot, mission, intro, ambient 등, 기본: general). 경로: images/{category}/, audio/{category}/ |

**지원 형식**
- **이미지:** jpeg, png, gif, webp
- **오디오:** mp3, wav, ogg, m4a

**Response 200**

```json
{
  "url": "https://s3.ap-northeast-2.amazonaws.com/bucket/path/file.jpg"
}
```

**Response 401** — 인증 필요  
**Response 500** — S3 비활성화 등 (예: "파일 업로드가 비활성화되어 있습니다.")

---

## 3. 투어 (Tour)

**Base Path:** `/api/v1`

---

### 3.1 투어 목록

```
GET /api/v1/tours
```

**인증:** 불필요 (공개)

**Response 200**

```json
[
  {
    "id": 1,
    "externalKey": "gyeongbokgung",
    "title": "경복궁 핵심 투어"
  },
  {
    "id": 2,
    "externalKey": "changdeokgung",
    "title": "창덕궁 탐험"
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | long | 투어 ID |
| externalKey | string | 외부 식별 키 |
| title | string | 표시 제목 |

---

### 3.2 투어 디테일

```
GET /api/v1/tours/{tourId}
```

접근 상태, 진행 Run, 액션 버튼, 맵 스팟 등 포함. **인증 시** `access`, `currentRun`, `actions` 등 추가 정보 반환.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| tourId | long | 투어 ID |

**Response 200**

```json
{
  "tourId": 1,
  "title": "경복궁 핵심 투어",
  "description": "조선의 대표 궁궐을 둘러보는 핵심 코스입니다.",
  "tags": [
    { "id": 1, "name": "역사", "slug": "history" }
  ],
  "counts": {
    "main": 8,
    "sub": 12,
    "photo": 5,
    "treasure": 3,
    "missions": 4
  },
  "info": {
    "entrance_fee": { "adult": 3000, "child": 1500 },
    "available_hours": [
      { "day": "weekday", "open": "09:00", "close": "18:00" }
    ],
    "estimated_duration_min": 90
  },
  "goodToKnow": ["한복 입장 무료", "편한 신발 추천"],
  "startSpot": {
    "spotId": 1,
    "title": "광화문",
    "lat": 37.576,
    "lng": 126.977,
    "radiusM": 60
  },
  "mapSpots": [
    { "spotId": 1, "type": "MAIN", "title": "광화문", "lat": 37.576, "lng": 126.977 },
    { "spotId": 9, "type": "TREASURE", "title": "비밀의 문", "lat": 37.579, "lng": 126.975 }
  ],
  "access": { "status": "UNLOCKED", "hasAccess": true },
  "thumbnails": ["https://s3.../images/tour/img1.jpg", "https://s3.../images/tour/img2.jpg"],
  "mainPlaceThumbnails": [
    { "spotId": 1, "title": "광화문", "thumbnailUrl": "https://s3.../thumb.jpg" }
  ],
  "currentRun": {
    "runId": 101,
    "status": "IN_PROGRESS",
    "startedAt": "2026-02-11T10:00:00",
    "progress": { "completedSpots": 2, "totalSpots": 8 }
  },
  "actions": {
    "primaryButton": "CONTINUE",
    "secondaryButton": "GPS_TO_START",
    "moreActions": ["RESTART"]
  }
}
```

**응답 필드 상세**

| 필드 | 타입 | 설명 |
|------|------|------|
| tourId | long | 투어 ID |
| title | string | 표시 제목 |
| description | string | 설명 |
| tags | array | 태그 목록 (id, name, slug) |
| counts | object | main, sub, photo, treasure, missions 개수 |
| info | object | entrance_fee, available_hours, estimated_duration_min (infoJson 기반) |
| goodToKnow | array | 팁 배열 (goodToKnowJson: {"tips": ["a","b"]} 또는 루트 배열 지원) |
| startSpot | object | 시작 스팟 (spotId, title, lat, lng, radiusM) |
| mapSpots | array | 맵에 표시할 스팟 (MAIN + TREASURE) |
| access | object | status: LOCKED \| UNLOCKED, hasAccess |
| thumbnails | array | 투어 디테일 캐러셀용 이미지 URL (tour_assets 우선, 없으면 메인 플레이스 이미지) |
| mainPlaceThumbnails | array | 메인 플레이스별 썸네일 (spotId, title, thumbnailUrl) |
| currentRun | object | IN_PROGRESS인 Run (없으면 null) |
| actions | object | 버튼 액션 정보 |
| mainMissionPath | array | Main Mission Path (스팟별 미션 목록) |

**mainMissionPath** 각 항목: `spotId`, `spotTitle`, `orderIndex`, `missions` (stepId, missionId, title)

**actions 규칙**
- `primaryButton`: `UNLOCK` (미접근) \| `START` (접근, Run 없음) \| `CONTINUE` (접근, Run 있음)
- `secondaryButton`: `GPS_TO_START`
- `moreActions`: `["RESTART"]` (Run 있으면)

**tours.info_json 스키마** (입장료·운영시간·예상소요시간 등)

```json
{
  "entrance_fee": { "adult": 3000, "child": 1500 },
  "available_hours": [{ "day": "weekday", "open": "09:00", "close": "18:00" }],
  "estimated_duration_min": 90
}
```

**tours.good_to_know_json 스키마** (팁 목록)

```json
{ "tips": ["한복 입장 무료", "편한 신발 추천", "화요일 휴궁"] }
```
또는 루트 배열 `["한복 입장 무료", "편한 신발 추천"]` 형식도 지원.

---

### 3.3 마커 목록

```
GET /api/v1/tours/{tourId}/markers
```

맵 표시용 마커 조회.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| tourId | long | 투어 ID |

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| filter | MarkerType | X | `STEP` \| `WAYPOINT` \| `PHOTO_SPOT` \| `TREASURE` — 미지정 시 전체 |

**MarkerType** (API 용어) ↔ **SpotType** (DB `tour_spots.type`)

| MarkerType | SpotType | 설명 |
|------------|----------|------|
| STEP | MAIN | 퀘스트 핵심 장소 (Place) |
| WAYPOINT | SUB | 서브 장소 (이동 경로) |
| PHOTO_SPOT | PHOTO | 포토 스팟 |
| TREASURE | TREASURE | 보물 찾기 |

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
  },
  {
    "id": 2,
    "type": "WAYPOINT",
    "title": "근정전 앞",
    "latitude": 37.579,
    "longitude": 126.977,
    "radiusM": 30,
    "refId": 2,
    "stepOrder": 2
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | long | 마커 ID |
| type | string | MarkerType |
| title | string | 제목 |
| latitude | number | 위도 |
| longitude | number | 경도 |
| radiusM | int | 감지 반경(미터) |
| refId | long | 참조 스팟 ID |
| stepOrder | int | 순서 |

---

### 3.4 Unlock

```
POST /api/v1/tours/{tourId}/access/unlock
Authorization: Bearer <accessToken>
```

**인증:** JWT 필수

`user_tour_access`를 UNLOCKED로 설정하여 해당 투어 접근을 해제합니다.

**Response 200** — 빈 본문

**Response 401** — 인증 필요  
**Response 404** — Tour not found

---

### 3.5 Run 처리

```
POST /api/v1/tours/{tourId}/runs
Authorization: Bearer <accessToken>
Content-Type: application/json
```

투어 Run 시작/재개/재시작. 유저당 투어별 `IN_PROGRESS` Run은 1개만 유지됩니다.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| tourId | long | 투어 ID |

**Query Parameters** (우선)

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| mode | RunMode | X | `START` \| `CONTINUE` \| `RESTART` — Query와 Body 중 하나만 사용 가능 |

**Request Body** (선택)

```json
{
  "mode": "START"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| mode | string | X | Query에 없으면 Body 사용. 없으면 기본 `START` |

**RunMode**

| 값 | 설명 |
|----|------|
| START | 새 Run 시작 |
| CONTINUE | 기존 IN_PROGRESS Run 재개 |
| RESTART | 기존 Run 종료 후 새로 시작 |

**Response 200**

```json
{
  "runId": 101,
  "tourId": 1,
  "status": "IN_PROGRESS",
  "mode": "START",
  "previousRun": null,
  "startSpot": {
    "spotId": 1,
    "title": "광화문",
    "lat": 37.576,
    "lng": 126.977,
    "radiusM": 60
  }
}
```

RESTART 시 `previousRun`에 이전 Run 정보가 포함될 수 있음.

| 필드 | 타입 | 설명 |
|------|------|------|
| runId | long | Run ID |
| tourId | long | 투어 ID |
| status | string | IN_PROGRESS \| COMPLETED \| ABANDONED |
| mode | string | 요청한 RunMode |
| previousRun | object | 이전 Run (runId, finalStatus) — RESTART 시 |
| startSpot | object | 시작 스팟 정보 |

---

### 3.6 근접 감지

```
POST /api/v1/tour-runs/{runId}/proximity
Authorization: Bearer <accessToken>
Content-Type: application/json
```

현재 위치가 스팟 반경(radiusM, 기본 50m) 안에 들어오면 해당 스팟 유형별 이벤트를 반환합니다.
- **Main/Sub Place**: 가이드 스크립트 + `UserSpotProgress` unlock
- **Treasure**: 첫 발견 시 `TREASURE_FOUND` 알람 + `UserTreasureStatus` unlock
- **Photo Spot**: `PHOTO_SPOT_FOUND` 알람

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| runId | long | Tour Run ID |

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| lang | string | X | `KO`, `EN`, `JP`, `CN` (소문자 ko, en, jp, cn 가능) — 기본 `ko` |

**Request Body (ProximityRequest)**

```json
{
  "latitude": 37.5796,
  "longitude": 126.9769
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| latitude | number | O | 현재 위도 |
| longitude | number | O | 현재 경도 |

**Response 200** — 근접 스팟 있을 때

```json
{
  "event": "PROXIMITY",
  "contentType": "GUIDE",
  "sessionId": 201,
  "context": {
    "refType": "SPOT",
    "refId": 1,
    "placeName": "광화문"
  },
  "messages": [
    {
      "turnId": 501,
      "role": "GUIDE",
      "source": "SCRIPT",
      "text": "광화문에 오신 것을 환영합니다. 이곳은 경복궁의 정문으로...",
      "assets": [
        { "id": 1, "type": "IMAGE", "url": "https://s3.../image.jpg", "meta": null }
      ],
      "action": { "type": "NEXT", "label": "다음", "stepId": 1 }
    }
  ]
}
```

**action.type 규칙** (spot_content_steps.next_action 기반)
- 중간 턴: `NEXT` (다음 세그먼트)
- 마지막 턴: `NEXT` (다음 컨텐츠) \| `MISSION_CHOICE` (게임 시작/스킵, stepId는 MISSION 스텝 ID)

**Response 200** — Treasure 근접 (첫 발견 시)

```json
{
  "event": "TREASURE_FOUND",
  "contentType": "TREASURE_ALARM",
  "sessionId": null,
  "context": { "refType": "SPOT", "refId": 9, "placeName": "풍기대", "spotType": "TREASURE" },
  "messages": []
}
```

**Response 200** — Photo Spot 근접

```json
{
  "event": "PHOTO_SPOT_FOUND",
  "contentType": "PHOTO_ALARM",
  "sessionId": null,
  "context": { "refType": "SPOT", "refId": 5, "placeName": "근정전 앞 광장", "spotType": "PHOTO" },
  "messages": []
}
```

**Response 204** — 근접 스팟 없음

| 필드 | 설명 |
|------|------|
| event | `PROXIMITY` \| `TREASURE_FOUND` \| `PHOTO_SPOT_FOUND` |
| contentType | `GUIDE` \| `TREASURE_ALARM` \| `PHOTO_ALARM` |
| sessionId | 채팅 세션 ID (GUIDE만 해당, Treasure/Photo는 null) |
| context | refType, refId, placeName, spotType |
| messages | 가이드 턴 목록 (GUIDE만, Treasure/Photo는 빈 배열) |

---

### 3.6.1 Collect Treasure

```
POST /api/v1/tour-runs/{runId}/treasures/{spotId}/collect
Authorization: Bearer <accessToken>
```

Treasure 50m 근접 후 상세 확인 → "Collect Treasure" 클릭 시 도감에 추가.

**Response 200** — 빈 본문

---

### 3.7 채팅 세션 조회/생성

```
GET /api/v1/tour-runs/{runId}/spots/{spotId}/chat-session
Authorization: Bearer <accessToken>
```

Run + Spot에 대한 채팅 세션 ID를 반환합니다. 없으면 생성합니다.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| runId | long | Tour Run ID |
| spotId | long | Spot ID |

**Response 200**

```json
{
  "sessionId": 201
}
```

---

### 3.8 채팅 히스토리

```
GET /api/v1/chat-sessions/{sessionId}/turns
Authorization: Bearer <accessToken>
```

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| sessionId | long | 채팅 세션 ID |

**Response 200**

```json
{
  "sessionId": 201,
  "turns": [
    {
      "id": 501,
      "role": "USER",
      "source": "USER",
      "text": "이 건물의 역사가 궁금해요",
      "assets": null,
      "action": null,
      "createdAt": "2026-02-11T10:30:00"
    },
    {
      "id": 502,
      "role": "ASSISTANT",
      "source": "AI",
      "text": "근정전은 1395년 태조에 의해 건축된 조선의 정전입니다...",
      "assets": null,
      "action": null,
      "createdAt": "2026-02-11T10:30:05"
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| role | USER \| ASSISTANT |
| source | USER \| GUIDE \| AI \| SCRIPT |

---

### 3.9 채팅 메시지 전송

```
POST /api/v1/chat-sessions/{sessionId}/messages
Authorization: Bearer <accessToken>
Content-Type: application/json
```

유저 질문 전송 후 AI 응답을 바로 반환합니다.

**Request Body (ChatMessageRequest)**

```json
{
  "text": "이 건물의 역사가 궁금해요"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| text | string | O | 유저 메시지 (`@NotBlank`) |

**Response 200**

```json
{
  "userTurnId": 503,
  "userText": "이 건물의 역사가 궁금해요",
  "aiTurnId": 504,
  "aiText": "근정전은 1395년 태조에 의해 건축된 조선의 정전입니다..."
}
```

---

### 3.10 스팟 (상세·가이드)

#### 3.10.1 스팟 상세 (Place/Treasure 더블모달)

```
GET /api/v1/spots/{spotId}
```

Place/Treasure/Photo Spot 공통 상세 정보. `titleKr`, `pronunciationUrl`, `address` 포함.

**Response 200**

```json
{
  "spotId": 1,
  "type": "MAIN",
  "title": "광화문",
  "titleKr": "광화문",
  "description": "...",
  "pronunciationUrl": "https://s3.../audio.mp3",
  "thumbnailUrl": "https://s3.../image.jpg",
  "latitude": 37.576,
  "longitude": 126.977,
  "address": "161 Sajik-ro, Jongno-gu, Seoul"
}
```

#### 3.10.2 스팟 가이드 (가이드 세그먼트)

```
GET /api/v1/spots/{spotId}/guide
```

스팟 페이지용 가이드 세그먼트(설명 + 이미지) 조회. **인증 필요** (투어 진행 중 스크립트 제공 목적).

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| spotId | long | Spot ID |

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| lang | string | X | `ko`, `en`, `jp`, `cn` — 기본 `ko` |

**Response 200**

```json
{
  "stepId": 1,
  "stepTitle": "광화문",
  "nextAction": "NEXT",
  "segments": [
    {
      "id": 10,
      "segIdx": 1,
      "textEn": "광화문에 오신 것을 환영합니다! 이곳은 경복궁의 정문으로...",
      "triggerKey": null,
      "media": [
        { "id": 1, "url": "https://s3.../image.jpg", "meta": null }
      ]
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| stepId | 스팟 ID (spotId와 동일) |
| stepTitle | 스팟 제목 |
| nextAction | `NEXT` \| `MISSION_CHOICE` (마지막 GUIDE 스텝 기준, null 가능) |
| segments[].id | 스크립트 라인 ID |
| segments[].segIdx | 세그먼트 순서 |
| segments[].textEn | 가이드 문장 (역할: 텍스트) |
| segments[].triggerKey | 트리거 키 (있는 경우) |
| segments[].media | 첨부 미디어 (id, url, meta) |

#### 3.10.3 미션 스텝 상세

```
GET /api/v1/content-steps/{stepId}/mission
```

Proximity 응답의 MISSION_CHOICE 후, 미션 UI용 prompt·optionsJson 조회. `options_json` 구조는 MISSION_SCHEMA.md 참조.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| stepId | long | spot_content_steps.id (MISSION kind) |

**Response 200**

```json
{
  "stepId": 10,
  "missionId": 3,
  "missionType": "QUIZ",
  "prompt": "광화문은 몇 개의 잡상이 있을까요?",
  "optionsJson": {
    "choices": [
      { "id": "a", "text": "1개", "imageUrl": "https://..." },
      { "id": "b", "text": "2개" }
    ],
    "questionImageUrl": "https://..."
  },
  "title": "광화문 퀴즈"
}
```

---

### 3.11 미션 제출

```
POST /api/v1/tour-runs/{runId}/steps/{stepId}/missions/submit
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Run의 step에 연결된 미션을 제출하고 채점합니다.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| runId | long | Tour Run ID |
| stepId | long | Step ID (spot_content_steps.id) |

**Request Body (MissionSubmitRequest)**

```json
{
  "userInput": "사용자 입력 텍스트",
  "photoUrl": "https://s3.../photo.jpg",
  "selectedOption": { "key": "A" }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userInput | string | X | 텍스트 입력 (TEXT_INPUT, QUIZ 등) |
| photoUrl | string | X | 업로드된 사진 URL (PHOTO 미션) |
| selectedOption | object | X | 선택 옵션 (CHOICE 미션, 예: `{ "key": "A" }`) |

미션 타입에 맞는 필드만 전송하면 됩니다.

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

| 필드 | 타입 | 설명 |
|------|------|------|
| attemptId | long | 제출 시도 ID |
| success | boolean | 처리 성공 여부 |
| isCorrect | boolean | 정답 여부 |
| score | int | 점수 |
| feedback | string | 피드백 메시지 |

---

## 4. 관리자 API

**Base Path:** `/api/v1/admin`  
**인증:** 세션 (sessionAuth) 필수 — 관리자 로그인 후 쿠키로 인증

---

### 4.1 Tour CRUD

**Base Path:** `/api/v1/admin/tours`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 목록 (페이지네이션) |
| GET | `/{tourId}` | 단건 조회 |
| POST | `/` | 생성 |
| PATCH | `/{tourId}` | 수정 |
| DELETE | `/{tourId}` | 삭제 |

#### Tour 목록

```
GET /api/v1/admin/tours?page=0&size=20
```

**Query Parameters**

| 이름 | 타입 | 기본 | 설명 |
|------|------|------|------|
| page | int | 0 | 페이지 번호 |
| size | int | 20 | 페이지 크기 |

**Response 200** — Spring `Page<TourAdminResponse>`

```json
{
  "content": [
    {
      "id": 1,
      "externalKey": "gyeongbokgung",
      "titleEn": "Gyeongbokgung Palace",
      "descriptionEn": "The main royal palace of Joseon.",
      "infoJson": {},
      "goodToKnowJson": {},
      "mainCount": 8,
      "subCount": 12,
      "photoSpotsCount": 5,
      "treasuresCount": 3,
      "missionsCount": 4
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

#### Tour 생성

```
POST /api/v1/admin/tours
Content-Type: application/json
```

**Request Body (TourCreateRequest)**

```json
{
  "externalKey": "gyeongbokgung",
  "titleEn": "Gyeongbokgung Palace",
  "descriptionEn": "The main royal palace of Joseon Dynasty.",
  "infoJson": {
    "entrance_fee": { "adult": 3000, "child": 1500 },
    "available_hours": [],
    "estimated_duration_min": 90
  },
  "goodToKnowJson": {
    "tips": ["한복 입장 무료", "편한 신발 추천"]
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| externalKey | string | O | 고유 식별 키 |
| titleEn | string | O | 제목 (영문) |
| descriptionEn | string | X | 설명 (영문) |
| infoJson | object | X | 입장료, 운영시간, 예상 소요시간 등 |
| goodToKnowJson | object | X | tips 배열 등 |

**Response 201** — TourAdminResponse

#### Tour 수정

```
PATCH /api/v1/admin/tours/{tourId}
Content-Type: application/json
```

**Request Body (TourUpdateRequest)** — 변경할 필드만 전송

```json
{
  "titleEn": "Gyeongbokgung Palace (Updated)",
  "descriptionEn": "Updated description.",
  "infoJson": { "estimated_duration_min": 120 },
  "goodToKnowJson": { "tips": ["새로운 팁"] }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| titleEn | string | X | 제목 |
| descriptionEn | string | X | 설명 |
| infoJson | object | X | 정보 JSON |
| goodToKnowJson | object | X | 팁 JSON |

#### Tour 삭제

```
DELETE /api/v1/admin/tours/{tourId}
```

**Response 204**

---

### 4.2 Tour Spot CRUD

**Base Path:** `/api/v1/admin/tours/{tourId}/spots`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | Spot 목록 |
| GET | `/{spotId}` | 단건 조회 |
| POST | `/` | 생성 |
| PATCH | `/{spotId}` | 수정 |
| DELETE | `/{spotId}` | 삭제 |

#### Spot 목록

```
GET /api/v1/admin/tours/{tourId}/spots
```

**Response 200** — `List<SpotAdminResponse>`

#### Spot 생성

```
POST /api/v1/admin/tours/{tourId}/spots
Content-Type: application/json
```

**Request Body (SpotCreateRequest)**

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

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| type | string | O | `MAIN` \| `SUB` \| `PHOTO` \| `TREASURE` |
| title | string | O | 스팟 제목 |
| description | string | X | 설명 |
| latitude | double | X | 위도 |
| longitude | double | X | 경도 |
| orderIndex | int | O | 순서 |
| radiusM | int | X | 근접 감지 반경(미터) |

**SpotType**

| 값 | 설명 |
|----|------|
| MAIN | 핵심 장소 |
| SUB | 서브(이동 경로) |
| PHOTO | 포토 스팟 |
| TREASURE | 보물 찾기 |

**Response 201** — SpotAdminResponse

#### Spot 수정

```
PATCH /api/v1/admin/tours/{tourId}/spots/{spotId}
```

**Request Body (SpotUpdateRequest)** — 변경할 필드만

```json
{
  "title": "광화문 (수정)",
  "description": "경복궁의 정문입니다.",
  "orderIndex": 2,
  "latitude": 37.576,
  "longitude": 126.977,
  "radiusM": 50
}
```

#### Spot 삭제

```
DELETE /api/v1/admin/tours/{tourId}/spots/{spotId}
```

**Response 204**

---

### 4.3 Spot 가이드 (Guide)

**Base Path:** `/api/v1/admin/tours/{tourId}/spots/{spotId}/guide`

스팟 가이드 문장 및 미디어(이미지/오디오) 관리. 미디어 URL은 `POST /api/v1/upload`로 S3 업로드 후 사용합니다.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 가이드 조회 |
| PUT | `/` | 가이드 전체 덮어쓰기 |

#### 가이드 조회

```
GET /api/v1/admin/tours/{tourId}/spots/{spotId}/guide
```

**Response 200**

```json
{
  "stepId": 1,
  "language": "ko",
  "stepTitle": "광화문",
  "lines": [
    {
      "id": 10,
      "seq": 1,
      "text": "광화문에 오신 것을 환영합니다.",
      "assets": [
        {
          "id": 1,
          "url": "https://s3.../image.jpg",
          "assetType": "IMAGE",
          "usage": "ILLUSTRATION"
        },
        {
          "id": 2,
          "url": "https://s3.../audio.mp3",
          "assetType": "AUDIO",
          "usage": "SCRIPT_AUDIO"
        }
      ]
    }
  ]
}
```

#### 가이드 저장 (덮어쓰기)

```
PUT /api/v1/admin/tours/{tourId}/spots/{spotId}/guide
Content-Type: application/json
```

**Request Body (GuideSaveRequest)**

```json
{
  "language": "ko",
  "stepTitle": "광화문",
  "nextAction": "NEXT",
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

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| language | string | O | 언어 (`ko`, `en`, `jp`, `cn`) |
| stepTitle | string | X | 스텝 제목 (없으면 스팟 제목 사용) |
| nextAction | string | X | `NEXT`(다음 컨텐츠) \| `MISSION_CHOICE`(게임 스타트/스킵) |
| lines | array | O | 가이드 문장 목록 (최소 1개) |

**GuideLineRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| text | string | O | 가이드 문장 |
| assets | array | O | 첨부 미디어 (없으면 `[]`) |

**GuideAssetRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| url | string | O | S3 업로드 API로 얻은 URL |
| assetType | string | O | `IMAGE` \| `AUDIO` |
| usage | string | O | `ILLUSTRATION` \| `SCRIPT_AUDIO` |

**Response 200** — GuideAdminResponse

---

### 4.4 Tour Assets (투어 레벨 썸네일/이미지)

**Base Path:** `/api/v1/admin/tours/{tourId}/assets`

투어 디테일 페이지용 썸네일·히어로 이미지 관리. tour_assets 테이블 (THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE).

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 에셋 목록 |
| POST | `/` | 에셋 추가 |
| DELETE | `/{tourAssetId}` | 에셋 삭제 |

#### 에셋 목록

```
GET /api/v1/admin/tours/{tourId}/assets
```

**Response 200**

```json
[
  {
    "id": 1,
    "assetId": 101,
    "url": "https://s3.../images/tour/img1.jpg",
    "usage": "THUMBNAIL",
    "sortOrder": 1,
    "caption": "경복궁 전경"
  }
]
```

#### 에셋 추가

```
POST /api/v1/admin/tours/{tourId}/assets
Content-Type: application/json
```

**Request Body (TourAssetRequest)**

```json
{
  "url": "https://s3.../images/tour/img1.jpg",
  "usage": "THUMBNAIL",
  "sortOrder": 1,
  "caption": "경복궁 전경"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| url | string | O | S3 업로드 API로 얻은 URL |
| usage | string | O | THUMBNAIL \| HERO_IMAGE \| GALLERY_IMAGE |
| sortOrder | int | X | 정렬 순서 (없으면 자동) |
| caption | string | X | 캡션 |

**Response 201** — TourAssetResponse

#### 에셋 삭제

```
DELETE /api/v1/admin/tours/{tourId}/assets/{tourAssetId}
```

**Response 204**

---

### 4.5 Spot Assets (스팟별 썸네일/히어로/갤러리)

**Base Path:** `/api/v1/admin/tours/{tourId}/spots/{spotId}/assets`

메인 플레이스별 썸네일(mainPlaceThumbnails) 관리. THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 스팟 에셋 목록 |
| POST | `/` | 스팟 에셋 추가 |
| DELETE | `/{spotAssetId}` | 스팟 에셋 삭제 |

(요청/응답 구조는 Tour Assets와 동일: url, usage, sortOrder, caption)

---

### 4.6 Mission Steps (MISSION 스텝 관리)

**Base Path:** `/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps`

스팟별 MISSION 스텝 (QUIZ, INPUT, PHOTO_CHECK) CRUD. options_json/answer_json 구조는 MISSION_SCHEMA.md 참조.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | MISSION 스텝 목록 |
| POST | `/` | MISSION 스텝 추가 |
| PATCH | `/{stepId}` | MISSION 스텝 수정 |
| DELETE | `/{stepId}` | MISSION 스텝 삭제 |

#### 스텝 목록

```
GET /api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps
```

**Response 200**

```json
[
  {
    "stepId": 10,
    "missionId": 5,
    "missionType": "QUIZ",
    "prompt": "이 건물은 언제 지어졌나요?",
    "optionsJson": {"choices": [{"id":"a","text":"1395년"}]},
    "answerJson": {"answer": "a"},
    "title": "퀴즈 1",
    "stepIndex": 1
  }
]
```

#### 스텝 추가

```
POST /api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps
Content-Type: application/json
```

**Request Body (MissionStepCreateRequest)**

```json
{
  "missionType": "QUIZ",
  "prompt": "정답을 고르세요.",
  "title": "퀴즈 1",
  "optionsJson": {"choices": [{"id":"a","text":"보기1"},{"id":"b","text":"보기2"}]},
  "answerJson": {"answer": "a"}
}
```

#### 스텝 수정

```
PATCH /api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps/{stepId}
```

**Request Body (MissionStepUpdateRequest)** — prompt, title, optionsJson, answerJson (선택)

#### 스텝 삭제

```
DELETE /api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps/{stepId}
```

**Response 204**

---

### 4.7 Enum

```
GET /api/v1/admin/enums/{enumName}
```

관리자 폼용 Enum 값 목록 조회.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| enumName | string | `language` \| `spotType` \| `markerType` \| `stepKind` \| `tourAssetUsage` \| `spotAssetUsage` |

**Response 200**

```json
["KO", "EN", "JP", "CN"]
```

**enumName별 반환 값**

| enumName | 값 |
|----------|-----|
| language | KO, EN, JP, CN |
| spotType | MAIN, SUB, PHOTO, TREASURE |
| markerType | STEP, WAYPOINT, PHOTO_SPOT, TREASURE |
| stepKind | GUIDE, MISSION |
| tourAssetUsage | THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE |
| spotAssetUsage | THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE, INTRO_AUDIO, AMBIENT_AUDIO |

**Response 404** — 지원하지 않는 enumName

---

### 4.8 RAG (벡터 지식 동기화)

투어·가이드 콘텐츠를 Pgvector에 임베딩하여 AI 가이드 RAG에 활용합니다. ai-server의 VectorRetriever가 이 데이터를 조회합니다.

**Base Path:** `/api/v1/admin/rag`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/sync` | 투어 지식 벡터 동기화 |
| GET | `/search` | 벡터 유사도 검색 테스트 |

**POST /api/v1/admin/rag/sync**

Tour, TourSpot, SpotScriptLine(가이드) 콘텐츠를 OpenAI 임베딩 후 `tour_knowledge_embeddings` 테이블에 저장.
**요구사항:** Backend `OPENAI_API_KEY` 환경변수 설정 필요. 미설정 시 동기화 스킵.

| Query | 타입 | 설명 |
|-------|------|------|
| tourId | long | (선택) 특정 투어만 동기화. 없으면 전체 동기화 |

**Response 200**
```json
{
  "embeddingsCount": 42
}
```

**GET /api/v1/admin/rag/search**

유사도 검색 테스트. 질문을 임베딩 후 Pgvector에서 유사 문서 검색.

| Query | 타입 | 설명 |
|-------|------|------|
| q | string | 검색할 질문 |
| limit | int | (기본 5) 반환 개수, 최대 20 |

**Response 200**
```json
[
  "투어: 경복궁...",
  "[광화문] 광화문은 조선시대..."
]
```

---

## 수집 API (Place·Treasure·Photo Spot)

플레이스 도감, 트레저 도감, 포토 스팟 전용 API.

### 개요

| 영역 | 용도 | 핵심 기능 |
|------|------|-----------|
| **Place Collection** | 플레이스 도감 | 방문한 MAIN/SUB 스팟 모아보기 |
| **Treasure Collection** | 트레저 도감 | 발견한 보물 스팟 모아보기 |
| **Photo Spot** | 포토 스팟 | 사진 찍기 좋은 장소 + 유저 포토 제출 → 검증 → 민트 → 노출 |

---

### 5.1 Place Collection (플레이스 도감)

MAIN, SUB 타입 스팟을 도감처럼 수집. `user_spot_progress`(progress_status ≠ PENDING) 기반.

#### 5.1.1 내 Place 컬렉션 조회

```
GET /api/v1/collections/places
Authorization: Bearer <accessToken>
```

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| tourId | long | X | 투어 ID — 미지정 시 전체 |
| lang | string | X | ko, en, jp, cn — 기본 ko |

**Response 200**

```json
{
  "totalCollected": 12,
  "totalAvailable": 20,
  "items": [
    {
      "spotId": 1,
      "tourId": 1,
      "tourTitle": "경복궁 조선의 왕의 날",
      "type": "MAIN",
      "title": "광화문",
      "description": "...",
      "thumbnailUrl": "https://s3.../gwanggwamun.jpg",
      "collectedAt": "2026-02-14T10:30:00",
      "orderIndex": 1,
      "collected": true
    }
  ]
}
```

#### 5.1.2 Place 컬렉션 요약

```
GET /api/v1/collections/places/summary
Authorization: Bearer <accessToken>
```

**Response 200**

```json
{
  "byTour": [
    { "tourId": 1, "tourTitle": "경복궁...", "collected": 4, "total": 8 }
  ],
  "totalCollected": 12,
  "totalAvailable": 20
}
```

---

### 5.2 Treasure Collection (트레저 도감)

TREASURE 타입 스팟 수집. `user_treasure_status`(status: GET) 기반.

#### 5.2.1 내 Treasure 컬렉션 조회

```
GET /api/v1/collections/treasures
Authorization: Bearer <accessToken>
```

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| tourId | long | X | 투어 ID |
| lang | string | X | ko, en, jp, cn |

**Response 200**

```json
{
  "totalCollected": 2,
  "totalAvailable": 2,
  "items": [
    {
      "spotId": 9,
      "tourId": 1,
      "tourTitle": "경복궁 조선의 왕의 날",
      "title": "비밀의 문",
      "description": "...",
      "thumbnailUrl": "https://s3.../treasure1.jpg",
      "gotAt": "2026-02-14T11:00:00",
      "orderIndex": 1,
      "collected": true
    }
  ]
}
```

#### 5.2.2 Treasure 컬렉션 요약

```
GET /api/v1/collections/treasures/summary
Authorization: Bearer <accessToken>
```

#### 5.2.3 Collect Treasure (보물 수집)

```
POST /api/v1/tour-runs/{runId}/treasures/{spotId}/collect
Authorization: Bearer <accessToken>
```

50m 근접 후 "See Treasure now" → 상세 확인 → "Collect Treasure" 클릭 시 호출.

**Response 200** — 빈 본문 (이미 수집된 경우에도 200)

---

### 5.3 Photo Spot API (포토 스팟)

#### user_photo_submissions 테이블

| 컬럼 | 설명 |
|------|------|
| status | PENDING, APPROVED, REJECTED |
| reject_reason | 거절 사유 |
| verified_at | 검증 시각 |
| mint_token | 민트 식별자 (선택) |
| is_public | 노출 여부 |

#### 5.3.1 포토 스팟 목록

```
GET /api/v1/photo-spots
```

**인증:** 불필요

| Query | 타입 | 설명 |
|-------|------|------|
| tourId | long | X | 투어 ID |
| lang | string | X | ko, en, jp, cn |

**Response 200** — 배열

```json
[
  {
    "spotId": 5,
    "tourId": 1,
    "tourTitle": "경복궁 조선의 왕의 날",
    "title": "근정전 앞 광장",
    "description": "경복궁 대표 포토 스팟",
    "thumbnailUrl": "https://s3.../photo_spot_1.jpg",
    "latitude": 37.5796,
    "longitude": 126.9769,
    "userPhotoCount": 8,
    "samplePhotos": [
      { "id": 1, "url": "...", "submittedBy": "user1", "mintedAt": "2026-02-10" }
    ]
  }
]
```

#### 5.3.2 포토 제출

```
POST /api/v1/photo-spots/{spotId}/submissions
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**Request Body**

```json
{
  "photoUrl": "https://s3.../uploaded_photo.jpg"
}
```

**Response 201**

```json
{
  "submissionId": 102,
  "status": "PENDING",
  "message": "검증 후 승인되면 민트 및 갤러리 노출됩니다."
}
```

#### 5.3.3 내 포토 제출 목록

```
GET /api/v1/photo-spots/my-submissions
Authorization: Bearer <accessToken>
```

| Query | 타입 | 설명 |
|-------|------|------|
| status | string | X | PENDING, APPROVED, REJECTED |
| spotId | long | X | 포토 스팟 ID |

**Response 200** — 배열 직접 반환

```json
[
  {
    "submissionId": 102,
    "spotId": 5,
    "spotTitle": "근정전 앞 광장",
    "photoUrl": "...",
    "status": "PENDING",
    "submittedAt": "2026-02-14T12:00:00",
    "rejectReason": null,
    "mintedAt": null
  }
]
```

#### 5.3.4 포토 스팟 상세

```
GET /api/v1/photo-spots/{spotId}
```

**인증:** 불필요

**Response 200**

```json
{
  "spotId": 5,
  "tourId": 1,
  "tourTitle": "경복궁 조선의 왕의 날",
  "title": "근정전 앞 광장",
  "description": "...",
  "thumbnailUrl": "...",
  "latitude": 37.5796,
  "longitude": 126.9769,
  "address": "161 Sajik-ro, Jongno-gu, Seoul",
  "officialPhotos": [
    { "id": 1, "url": "...", "caption": null }
  ],
  "userPhotos": [
    {
      "submissionId": 101,
      "url": "...",
      "submittedBy": "여행자A",
      "mintedAt": "2026-02-10T14:00:00"
    }
  ]
}
```

---

### 5.4 관리자 API (포토 검증)

#### 5.4.1 검증 대기 목록

```
GET /api/v1/admin/photo-submissions?status=PENDING
```

#### 5.4.2 포토 승인/거절

```
PATCH /api/v1/admin/photo-submissions/{submissionId}
```

**Request Body**

```json
{ "action": "APPROVE" }
```
또는
```json
{ "action": "REJECT", "rejectReason": "포토 스팟과 무관한 이미지입니다." }
```

#### 5.4.3 민트 처리

승인 시 `is_public = true` 설정 및 `mint_token` 자동 생성·저장 (`MINT-{submissionId}-{uuid8}`).

---

### 5.5 스팟 상세 (Place/Treasure 더블모달)

```
GET /api/v1/spots/{spotId}
```

**인증:** 불필요

**Response 200**

```json
{
  "spotId": 1,
  "type": "MAIN",
  "title": "광화문",
  "titleKr": "광화문",
  "description": "...",
  "pronunciationUrl": "https://s3.../audio.mp3",
  "thumbnailUrl": "https://s3.../image.jpg",
  "latitude": 37.576,
  "longitude": 126.977,
  "address": "161 Sajik-ro, Jongno-gu, Seoul"
}
```

---

### 5.6 Proximity API 확장 (근접 알람)

`POST /api/v1/tour-runs/{runId}/proximity` 이벤트 타입:

| event | contentType | 설명 |
|-------|-------------|------|
| PROXIMITY | GUIDE | Main/Sub Place 50m 진입, 가이드 스크립트 |
| TREASURE_FOUND | TREASURE_ALARM | Treasure 50m 진입 (첫 발견) |
| PHOTO_SPOT_FOUND | PHOTO_ALARM | Photo Spot 50m 진입 |

---

### 5.7 인증 정리

| API | 인증 |
|-----|------|
| GET /collections/places | 필수 |
| GET /collections/treasures | 필수 |
| GET /photo-spots | 불필요 |
| POST /photo-spots/{id}/submissions | 필수 |
| GET /photo-spots/my-submissions | 필수 |
| GET,PATCH /admin/photo-submissions | 세션(관리자) |

---

## Swagger UI

개발 환경에서 API 문서 확인:

- `/swagger-ui.html`
- `/swagger-ui/index.html`
