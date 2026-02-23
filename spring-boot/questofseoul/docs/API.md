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

### Nullable 표기 규칙

- 문서 표의 `Nullable`은 해당 필드가 `null`을 허용하는지 의미합니다.
- `O`: null 허용, `X`: null 불가
- `필수` 컬럼은 요청 시 값 제공 필요 여부를 의미합니다.

---

## 1. 인증 (Auth)

**Base Path:** `/api/v1/auth`

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | `/login` | - | 이메일/비밀번호 로그인 → JWT 발급 |
| POST | `/register` | - | 회원가입 → JWT 발급 |
| GET | `/me` | JWT 또는 세션 | 현재 사용자 조회 |
| POST | `/token` | 세션 | OAuth2 세션 → JWT 발급 |
| POST | `/refresh` | Refresh Cookie | 리프레시 토큰 쿠키로 JWT 재발급 (회전) |

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

| 필드 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| email | string | O | X | 이메일 (형식 검증) |
| password | string | O | X | 비밀번호 |

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

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| accessToken | string | X | JWT 액세스 토큰 |
| expiresIn | number | X | 토큰 만료 시간 (초) |
| tokenType | string | X | `"Bearer"` |

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

| 필드 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| email | string | O | X | 이메일 (형식 검증) |
| password | string | O | X | 비밀번호 (8자 이상) |
| nickname | string | X | O | 닉네임 |

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

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| accessToken | string | X | JWT 액세스 토큰 |
| expiresIn | number | X | 토큰 만료 시간 (초) |
| tokenType | string | X | `"Bearer"` |

---

### 1.3 현재 사용자 조회

```
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

**Response 200**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "ADMIN"
}
```

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| userId | string(UUID) | X | 사용자 ID |
| role | string | X | `ADMIN` \| `USER` |

**Response 401** — 비로그인

---

### 1.4 OAuth2 → JWT 토큰 발급

```
POST /api/v1/auth/token
Cookie: JSESSIONID=...
```

세션 인증 후 JWT를 발급합니다. OAuth2 로그인 직후 앱에서 JWT로 전환할 때 사용.

> JWT payload에는 `role` 클레임(`ADMIN` 또는 `USER`)이 포함됩니다.

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| accessToken | string | X | JWT 액세스 토큰 |
| expiresIn | number | X | 토큰 만료 시간 (초) |
| tokenType | string | X | `"Bearer"` |

---

### 1.5 리프레시 토큰으로 JWT 재발급

```
POST /api/v1/auth/refresh
Cookie: qos_refresh_token=...
```

`HttpOnly` refresh cookie를 검증한 뒤 액세스 토큰을 재발급하고, refresh cookie도 회전합니다.

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| accessToken | string | X | 새 JWT 액세스 토큰 |
| expiresIn | number | X | 토큰 만료 시간 (초) |
| tokenType | string | X | `"Bearer"` |

**Response 401** — refresh cookie 없음/만료/위변조

---

## 2. 파일 업로드

**Base Path:** `/api/v1/upload`  
**인증:** Bearer JWT 또는 세션 필수

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/` | multipart/form-data로 S3 업로드 후 URL 반환 |
| DELETE | `/?url=...` | S3에 업로드된 파일 URL로 삭제 (본 서비스 버킷만 가능) |

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

### 2.2 업로드 파일 삭제

```
DELETE /api/v1/upload?url=<S3_URL>
Authorization: Bearer <accessToken>
```

**Query Parameters**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| url | string | O | 삭제할 S3 파일의 전체 URL (인코딩 필요) |

**제한:** 본 서비스 S3 버킷에 업로드된 URL만 삭제 가능. 외부 URL은 400 에러.

**Response 204** — 삭제 성공 (No Content)

**Response 400** — 유효하지 않은 URL 또는 외부 버킷 URL  
**Response 401** — 인증 필요  
**Response 500** — S3 삭제 실패

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
    "title": "경복궁 핵심 투어",
    "thumbnailUrl": "https://s3.../tour-thumb.jpg",
    "description": "조선의 대표 궁궐을 둘러보는 핵심 코스입니다.",
    "counts": { "main": 8, "sub": 12, "photo": 5, "treasure": 3, "missions": 4 },
    "estimatedDurationMin": 90,
    "accessStatus": "UNLOCKED",
    "tags": [{ "id": 1, "name": "역사", "slug": "history" }]
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| id | long | 투어 ID |
| externalKey | string | 외부 식별 키 |
| title | string | 표시 제목 |
| thumbnailUrl | string | 대표 썸네일 URL |
| description | string | 설명 |
| counts | object | main, sub, photo, treasure, missions |
| estimatedDurationMin | int | 예상 소요 시간(분) |
| accessStatus | string | LOCKED \| UNLOCKED |
| tags | array | 태그 목록 |

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
    {
      "spotId": 1,
      "type": "MAIN",
      "title": "광화문",
      "lat": 37.576,
      "lng": 126.977,
      "thumbnailUrl": "https://s3.../thumb.jpg",
      "isHighlight": true
    },
    {
      "spotId": 2,
      "type": "SUB",
      "title": "흥례문",
      "lat": 37.577,
      "lng": 126.976,
      "thumbnailUrl": "https://s3.../sub-thumb.jpg",
      "isHighlight": false
    },
    {
      "spotId": 7,
      "type": "PHOTO",
      "title": "근정전 포토존",
      "lat": 37.579,
      "lng": 126.977,
      "thumbnailUrl": "https://s3.../photo-thumb.jpg",
      "isHighlight": false
    },
    {
      "spotId": 9,
      "type": "TREASURE",
      "title": "비밀의 문",
      "lat": 37.579,
      "lng": 126.975,
      "thumbnailUrl": null,
      "isHighlight": false
    }
  ],
  "access": { "status": "UNLOCKED", "hasAccess": true },
  "thumbnails": ["https://s3.../images/tour/img1.jpg", "https://s3.../images/tour/img2.jpg"],
  "currentRun": {
    "runId": 101,
    "status": "IN_PROGRESS",
    "startedAt": "2026-02-11T10:00:00",
    "progress": {
      "completedCount": 2,
      "totalCount": 20,
      "completedSpotIds": [1, 3]
    }
  },
  "actions": {
    "primaryButton": "CONTINUE",
    "secondaryButton": "GPS_TO_START"
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
| mapSpots | array | 맵에 표시할 스팟 (MAIN + SUB + PHOTO + TREASURE, thumbnailUrl + isHighlight 포함) |
| access | object | status: LOCKED \| UNLOCKED, hasAccess |
| thumbnails | array | 투어 디테일 캐러셀용 이미지 URL (tour_assets 우선, 없으면 메인 플레이스 이미지) |
| currentRun | object | IN_PROGRESS인 Run (없으면 null) |
| actions | object | 버튼 액션 정보 |
| mainMissionPath | array | Main Mission Path (스팟별 미션 목록) |

**mainMissionPath** 각 항목: `spotId`, `spotTitle`, `orderIndex`, `missions` (stepId, missionId, title)

**actions 규칙**
- `primaryButton`: `UNLOCK` (미접근) \| `START` (접근, Run 없음) \| `CONTINUE` (접근, Run 있음)
- `secondaryButton`: `GPS_TO_START`
- `moreActions`: 현재 기본 null

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

기존 `/api/v1/tours/{tourId}/markers`는 제거되었습니다.  
마커 정보는 `GET /api/v1/tours/{tourId}`의 `mapSpots`로 통합됩니다.

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

투어 Run 시작/재개. 유저당 투어별 `IN_PROGRESS` Run은 1개만 유지됩니다.

**Path Parameters**

| 이름 | 타입 | 설명 |
|------|------|------|
| tourId | long | 투어 ID |

**Request Body**

```json
{
  "mode": "START"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| mode | string | O | `START` \| `CONTINUE` |

**RunMode**

| 값 | 설명 |
|----|------|
| START | 새 Run 시작 |
| CONTINUE | 기존 IN_PROGRESS Run 재개 |

**Response 200**

```json
{
  "runId": 101,
  "tourId": 1,
  "status": "IN_PROGRESS",
  "mode": "START",
  "progress": {
    "completedCount": 2,
    "totalCount": 8,
    "completedSpotIds": [1, 3]
  },
  "startSpot": {
    "spotId": 1,
    "title": "광화문",
    "lat": 37.576,
    "lng": 126.977,
    "radiusM": 60
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| runId | long | Run ID |
| tourId | long | 투어 ID |
| status | string | IN_PROGRESS \| COMPLETED \| ABANDONED |
| mode | string | 요청한 RunMode |
| progress | object | completedCount, totalCount, completedSpotIds |
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
  "lat": 37.5796,
  "lng": 126.9769
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| lat | number | O | 현재 위도 |
| lng | number | O | 현재 경도 |

**Response 200** — 근접 스팟 있을 때

```json
{
  "event": "PROXIMITY",
  "contentType": "GUIDE",
  "sessionId": 201,
  "context": {
    "refType": "SPOT",
    "refId": 1,
    "placeName": "광화문",
    "spotType": "MAIN"
  },
  "message": {
    "turnId": 501,
    "role": "GUIDE",
    "source": "SCRIPT",
    "text": "광화문에 오신 것을 환영합니다. 이곳은 경복궁의 정문으로...",
    "assets": [
      { "id": 1, "type": "IMAGE", "url": "https://s3.../image.jpg", "meta": null }
    ],
    "delayMs": 1500,
    "action": {
      "type": "AUTO_NEXT",
      "nextApi": "/api/v1/chat-sessions/201/turns/502"
    }
  }
}
```

**action.type 규칙** (spot_content_steps.next_action 기반)
- 같은 컨텐츠(step) 내 중간 턴: `AUTO_NEXT` (`nextApi`로 자동 진행)
- 컨텐츠(step) 마지막 턴: `NEXT` (다음) \| `MISSION_CHOICE` (게임 시작)
- `MISSION_CHOICE`는 `stepId`(MISSION step)를 포함하며, 다음 컨텐츠가 있으면 `nextApi`도 함께 포함될 수 있습니다.

**Response 200** — Treasure 근접 (첫 발견 시)

```json
{
  "event": "TREASURE_FOUND",
  "contentType": "TREASURE_ALARM",
  "sessionId": null,
  "context": { "refType": "SPOT", "refId": 9, "placeName": "풍기대", "spotType": "TREASURE" },
  "message": null
}
```

**Response 200** — Photo Spot 근접

```json
{
  "event": "PHOTO_SPOT_FOUND",
  "contentType": "PHOTO_ALARM",
  "sessionId": null,
  "context": { "refType": "SPOT", "refId": 5, "placeName": "근정전 앞 광장", "spotType": "PHOTO" },
  "message": null
}
```

**Response 204** — 근접 스팟 없음

| 필드 | 설명 |
|------|------|
| event | `PROXIMITY` \| `TREASURE_FOUND` \| `PHOTO_SPOT_FOUND` |
| contentType | `GUIDE` \| `TREASURE_ALARM` \| `PHOTO_ALARM` |
| sessionId | 채팅 세션 ID (GUIDE만 해당, Treasure/Photo는 null) |
| context | refType, refId, placeName, spotType |
| message | 단일 가이드 턴 (GUIDE만), Treasure/Photo는 null |

---

### 3.6.1 Collect Treasure

```
POST /api/v1/tour-runs/{runId}/treasures/{spotId}/collect
Authorization: Bearer <accessToken>
```

Treasure 50m 근접 후 상세 확인 → "Collect Treasure" 클릭 시 도감에 추가.

**Response 200** — 빈 본문

---

### 3.6.2 다음 장소 조회

```
GET /api/v1/tour-runs/{runId}/next-spot
Authorization: Bearer <accessToken>
```

Run 진행 상태를 기준으로 다음 MAIN/SUB 장소를 반환합니다.

**Response 200**

```json
{
  "runId": 101,
  "status": "IN_PROGRESS",
  "hasNextSpot": true,
  "nextSpot": {
    "spotId": 3,
    "spotType": "MAIN",
    "title": "근정전",
    "lat": 37.579,
    "lng": 126.977,
    "radiusM": 50,
    "orderIndex": 2
  },
  "progress": {
    "completedCount": 1,
    "totalCount": 8,
    "completedSpotIds": [1]
  }
}
```

`hasNextSpot = false`이면 `nextSpot`은 null이며, 상황에 따라 run 상태가 `COMPLETED`로 전환됩니다.

---

### 3.7 채팅 세션 조회/생성

```
GET /api/v1/tour-runs/{runId}/spots/{spotId}/chat-session
Authorization: Bearer <accessToken>
```

Run + Spot에 대한 채팅 세션 ID를 반환합니다. 없으면 생성합니다.
**MAIN/SUB 스팟만 대상**이며, 해당 스팟이 **unlock 상태**여야 합니다.

**Path Parameters**

| 이름 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| runId | long | O | X | Tour Run ID |
| spotId | long | O | X | Spot ID (MAIN/SUB, unlock 상태) |

**Response 200**

```json
{
  "sessionId": 201,
  "status": "ACTIVE",
  "lastTurnId": 505
}
```

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| sessionId | long | X | 채팅 세션 ID |
| status | string | X | `ACTIVE` \| `COMPLETED` |
| lastTurnId | long | O | 마지막으로 노출된 스크립트 턴 ID (아직 없으면 `null`) |

---

### 3.8 채팅 히스토리

```
GET /api/v1/chat-sessions/{sessionId}/turns
Authorization: Bearer <accessToken>
```

**Path Parameters**

| 이름 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| sessionId | long | O | X | 채팅 세션 ID |

**Response 200**

```json
{
  "sessionId": 201,
  "status": "ACTIVE",
  "nextScriptApi": "/api/v1/chat-sessions/201/turns/502",
  "hasNextScript": true,
  "turns": [
    {
      "turnId": 501,
      "role": "USER",
      "source": "USER",
      "text": "이 건물의 역사가 궁금해요",
      "assets": [],
      "delayMs": null,
      "action": null,
      "createdAt": "2026-02-11T10:30:00"
    },
    {
      "turnId": 502,
      "role": "GUIDE",
      "source": "SCRIPT",
      "text": "근정전은 1395년 태조에 의해 건축된 조선의 정전입니다...",
      "assets": [{ "id": 1, "type": "IMAGE", "url": "https://s3.../image.jpg", "meta": null }],
      "delayMs": 1500,
      "action": { "type": "AUTO_NEXT", "nextApi": "/api/v1/chat-sessions/201/turns/503" },
      "createdAt": "2026-02-11T10:30:05"
    }
  ]
}
```

**Response 필드**

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| sessionId | long | X | 채팅 세션 ID |
| status | string | X | `ACTIVE` \| `COMPLETED` |
| nextScriptApi | string | O | 다음 스크립트 턴 API (없으면 `null`) |
| hasNextScript | boolean | X | 남은 스크립트 존재 여부 |
| turns | array | X | 턴 목록 |

**turns[] 필드**

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| turnId | long | X | 턴 ID |
| role | string | X | `USER` \| `GUIDE` \| `SYSTEM` (ChatRole) |
| source | string | X | `USER` \| `SCRIPT` \| `LLM` (ChatSource) |
| text | string | O | 발화 텍스트 |
| assets | array | X | 연결된 에셋 목록 |
| delayMs | int | O | 스크립트 자동 노출 지연(ms), SCRIPT가 아니면 `null` |
| action | object | O | 턴 이후 동작 정보 (`AUTO_NEXT`, `NEXT`, `MISSION_CHOICE`) |
| createdAt | string | O | ISO 8601 생성 시각 |

---

### 3.8.1 다음 스크립트 턴 조회

```
GET /api/v1/chat-sessions/{sessionId}/turns/{nextTurnId}
Authorization: Bearer <accessToken>
```

`nextApi`로 받은 `nextTurnId`의 단일 턴을 반환합니다.

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

| 필드 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| text | string | O | X | 유저 메시지 (`@NotBlank`) |

**Response 200**

```json
{
  "userTurnId": 503,
  "userText": "이 건물의 역사가 궁금해요",
  "aiTurnId": 504,
  "aiText": "근정전은 1395년 태조에 의해 건축된 조선의 정전입니다...",
  "nextScriptApi": "/api/v1/chat-sessions/201/turns/505",
  "hasNextScript": true
}
```

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| userTurnId | long | X | 저장된 사용자 턴 ID |
| userText | string | X | 사용자 발화 |
| aiTurnId | long | X | 저장된 AI 턴 ID |
| aiText | string | O | AI 응답 텍스트 |
| nextScriptApi | string | O | 다음 스크립트 턴 API (없으면 `null`) |
| hasNextScript | boolean | X | 남은 스크립트 존재 여부 |

---

### 3.10 스팟 (상세·가이드)

#### 3.10.1 스팟 상세 (Place/Treasure 더블모달)

```
GET /api/v1/spots/{spotId}
GET /api/v1/spots/{spotId}/detail
```

**인증:** 불필요 (공개)

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
  "lat": 37.576,
  "lng": 126.977,
  "address": "161 Sajik-ro, Jongno-gu, Seoul"
}
```

#### 3.10.2 스팟 가이드 (가이드 세그먼트)

```
GET /api/v1/spots/{spotId}/guide
```

스팟 페이지용 가이드 세그먼트(설명 + 이미지) 조회. **인증:** JWT 필수.

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
      "text": "광화문에 오신 것을 환영합니다! 이곳은 경복궁의 정문으로...",
      "triggerKey": null,
      "assets": [
        { "id": 1, "type": "IMAGE", "url": "https://s3.../image.jpg", "meta": null }
      ],
      "delayMs": 2000
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| stepId | 스팟 ID (spotId와 동일) |
| stepTitle | 스팟 제목 |
| nextAction | `NEXT` \| `MISSION_CHOICE` (마지막 GUIDE 컨텐츠 기준, null 가능) |
| segments[].id | 스크립트 라인 ID |
| segments[].segIdx | 세그먼트 순서 |
| segments[].text | 가이드 문장 |
| segments[].triggerKey | 트리거 키 (있는 경우) |
| segments[].assets | 첨부 에셋 (id, type, url, meta) |
| segments[].delayMs | 자동 재생 딜레이(ms) |

#### 3.10.3 미션 스텝 상세

```
GET /api/v1/content-steps/{stepId}/mission
Authorization: Bearer <accessToken>
```

**인증:** JWT 필수

Proximity 응답의 MISSION_CHOICE 후, 미션 UI용 prompt·optionsJson 조회. `options_json`/`answer_json` 구조는 본 문서 **4.6.1**을 참조하세요.

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
POST /api/v1/tour-runs/{runId}/missions/{stepId}/submit
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
  "missionType": "QUIZ",
  "userInput": "사용자 입력 텍스트",
  "photoUrl": "https://s3.../photo.jpg",
  "selectedOptionId": "A"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| missionType | string | O | `QUIZ` \| `OX` \| `PHOTO` \| `TEXT_INPUT` |
| userInput | string | X | 텍스트 입력 |
| photoUrl | string | X | 업로드된 사진 URL |
| selectedOptionId | string | X | 선택 옵션 ID |

미션 타입에 맞는 필드만 전송하면 됩니다.

**Response 200**

```json
{
  "attemptId": 1,
  "isCorrect": true,
  "score": 100,
  "feedback": "정답입니다!",
  "nextStepApi": "/api/v1/tour-runs/10/next-spot"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| attemptId | long | 제출 시도 ID |
| isCorrect | boolean | 정답 여부 |
| score | int | 점수 |
| feedback | string | 피드백 메시지 |
| nextStepApi | string | 다음 단계 API (`/api/v1/content-steps/{stepId}/mission` 또는 `/api/v1/tour-runs/{runId}/next-spot`) |

---

## 4. 관리자 API

**Base Path:** `/api/v1/admin`  
**인증:** `ADMIN` 권한 필수 (JWT Bearer 또는 세션)

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
| titleKr | string | X | 한국어 제목 |
| description | string | X | 설명 |
| pronunciationUrl | string | X | 발음 오디오 URL |
| address | string | X | 주소 |
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
  "titleKr": "광화문",
  "description": "경복궁의 정문입니다.",
  "pronunciationUrl": "https://s3.../audio.mp3",
  "address": "161 Sajik-ro, Jongno-gu, Seoul",
  "orderIndex": 2,
  "latitude": 37.576,
  "longitude": 126.977,
  "radiusM": 50
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| title, titleKr | string | 제목 |
| description | string | 설명 |
| pronunciationUrl | string | 발음 오디오 URL |
| address | string | 주소 |
| orderIndex | int | 순서 |
| latitude, longitude | double | 위경도 |
| radiusM | int | 근접 반경(m) |

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
GET /api/v1/admin/tours/{tourId}/spots/{spotId}/guide?lang=ko
```

**Query Parameters**

| 이름 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| lang | string | ko | 조회 언어 (`ko`, `en`, `jp`, `cn`) |

**Response 200 (GuideStepsAdminResponse)**

스팟 가이드는 N개 컨텐츠 블록(step)으로 구성됩니다. 각 step은 여러 문장(lines) + 미디어를 가집니다.

```json
{
  "language": "ko",
  "steps": [
    {
      "stepId": 1,
      "stepIndex": 0,
      "stepTitle": "광화문",
      "nextAction": "NEXT",
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
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| language | string | 조회 언어 |
| steps | array | 가이드 스텝 목록 (컨텐츠 블록) |
| steps[].stepId | long | spot_content_steps.id |
| steps[].stepIndex | int | 스텝 순서 |
| steps[].stepTitle | string | 스텝 제목 |
| steps[].nextAction | string | NEXT \| MISSION_CHOICE |
| steps[].lines | array | 문장 + 미디어 목록 |
| steps[].lines[].id | long | 스크립트 라인 ID |
| steps[].lines[].seq | int | 문장 순서 |
| steps[].lines[].text | string | 가이드 문장 |
| steps[].lines[].assets | array | 미디어 (id, url, assetType, usage) |

#### 가이드 저장 (덮어쓰기)

```
PUT /api/v1/admin/tours/{tourId}/spots/{spotId}/guide
Content-Type: application/json
```

**Request Body (GuideStepsSaveRequest)**

N개 컨텐츠 블록 전체를 덮어씁니다.

```json
{
  "language": "ko",
  "steps": [
    {
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
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| language | string | O | 언어 (`ko`, `en`, `jp`, `cn`) |
| steps | array | O | 가이드 스텝 목록 (최소 1개) |

**GuideStepSaveRequest**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| stepTitle | string | X | 스텝 제목 (없으면 스팟 제목 사용) |
| nextAction | string | X | `NEXT` \| `MISSION_CHOICE` |
| lines | array | O | 문장 목록 (최소 1개) |

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

**Response 200** — GuideStepsAdminResponse (조회 응답과 동일 구조)

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

스팟별 에셋(THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE) 관리.  
THUMBNAIL은 사용자 API `mapSpots[].thumbnailUrl` 및 스팟 상세 썸네일 계산에 사용됩니다.

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | 스팟 에셋 목록 |
| POST | `/` | 스팟 에셋 추가 |
| DELETE | `/{spotAssetId}` | 스팟 에셋 삭제 |

(요청/응답 구조는 Tour Assets와 동일: url, usage, sortOrder, caption)

---

### 4.6 Mission Steps (MISSION 스텝 관리)

**Base Path:** `/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps`

스팟별 MISSION 스텝 (QUIZ, OX, PHOTO, TEXT_INPUT) CRUD. `options_json`/`answer_json` 구조는 아래 **4.6.1** 스키마를 따릅니다.

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

| 필드 | 타입 | Nullable | 설명 |
|------|------|----------|------|
| stepId | long | X | MISSION 스텝 ID |
| missionId | long | O | 연결된 미션 ID |
| missionType | string | O | `QUIZ` \| `OX` \| `PHOTO` \| `TEXT_INPUT` |
| prompt | string | O | 문제/지시문 |
| optionsJson | object | X | 미션 옵션(JSON) |
| answerJson | object | X | 정답/채점 기준(JSON) |
| title | string | O | 스텝 제목 |
| stepIndex | int | X | 스텝 순서 |

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
  "optionsJson": {
    "choices": [{"id":"a","text":"보기1"},{"id":"b","text":"보기2"}],
    "hintText": "건물 정면의 현판을 보세요",
    "hintImageUrl": "https://s3.../mission/hint.png"
  },
  "answerJson": {"answer": "a"}
}
```

| 필드 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| missionType | string | O | X | `QUIZ` \| `OX` \| `PHOTO` \| `TEXT_INPUT` |
| prompt | string | X | O | 문제/지시문 |
| title | string | X | O | 스텝 제목 |
| optionsJson | object | X | O | 옵션 JSON (`hintText`, `hintImageUrl` 포함 가능) |
| answerJson | object | X | O | 정답/채점 기준 JSON |

#### 스텝 수정

```
PATCH /api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps/{stepId}
```

**Request Body (MissionStepUpdateRequest)** — prompt, title, optionsJson, answerJson (모두 선택)

| 필드 | 타입 | 필수 | Nullable | 설명 |
|------|------|------|----------|------|
| prompt | string | X | O | 문제/지시문 |
| title | string | X | O | 스텝 제목 |
| optionsJson | object | X | O | 옵션 JSON (`hintText`, `hintImageUrl` 포함 가능) |
| answerJson | object | X | O | 정답/채점 기준 JSON |

#### 스텝 삭제

```
DELETE /api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps/{stepId}
```

**Response 204**

#### 4.6.1 Mission Payload 스키마 (`options_json`, `answer_json`)

**missionType 값 정리**

- 관리자 미션 정의(`missions.mission_type`): `QUIZ`, `OX`, `PHOTO`, `TEXT_INPUT`
- 사용자 미션 제출(`MissionSubmitRequest.missionType`): `QUIZ`, `OX`, `PHOTO`, `TEXT_INPUT`

**options_json 권장 구조**

`QUIZ` (객관식)

```json
{
  "choices": [
    { "id": "a", "text": "보기1 텍스트", "imageUrl": "https://s3.../mission/choice_a.png" },
    { "id": "b", "text": "보기2 텍스트" },
    { "id": "c", "text": "보기3 텍스트", "imageUrl": "https://s3.../mission/choice_c.png" }
  ],
  "questionImageUrl": "https://s3.../mission/question.png",
  "hintText": "정답은 현판 연도와 같습니다.",
  "hintImageUrl": "https://s3.../mission/hint.png"
}
```

- `choices`: 보기 배열 (`id`, `text` 필수, `imageUrl` 선택)
- `questionImageUrl`: 문제 이미지 (선택)
- `hintText`, `hintImageUrl`: 힌트 텍스트/이미지 (선택)

`OX`

- `QUIZ`와 동일하게 `hintText`, `hintImageUrl`를 사용할 수 있습니다.

`TEXT_INPUT` (주관식)

```json
{
  "placeholder": "답을 입력하세요",
  "hintImageUrl": "https://s3.../mission/hint.png"
}
```

`PHOTO` (사진 체크)

```json
{
  "exampleImageUrl": "https://s3.../mission/example.png",
  "instruction": "이 장소를 찍어주세요",
  "hintText": "왼쪽 기둥 문양이 보이도록 찍어보세요.",
  "hintImageUrl": "https://s3.../mission/hint_photo.png"
}
```

**answer_json 권장 구조**

`QUIZ`

```json
{ "answer": "a" }
```

또는

```json
{ "value": "a" }
```

`TEXT_INPUT`, `PHOTO`

- 채점 정책에 따라 `value`, `expected` 등 확장 필드 사용 가능

**업로드 연동**

- 미션 이미지 URL은 `POST /api/v1/upload?type=image&category=mission` 업로드 후 사용

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

MAIN, SUB 타입 스팟 도감. **전체 수집 가능 스팟 목록**을 반환하며, 각 아이템의 `collected`로 수집 여부를 표현합니다. `user_spot_progress`(progress_status ≠ PENDING) 기반.

#### 5.1.1 내 Place 컬렉션 조회

```
GET /api/v1/collections/places
Authorization: Bearer <accessToken>
```

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| tourId | long | X | 투어 ID — 미지정 시 전체 |
| lang | string | X | ko, en, jp, cn — 기본 ko (현재 미사용) |

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
    },
    {
      "spotId": 2,
      "tourId": 1,
      "tourTitle": "경복궁 조선의 왕의 날",
      "type": "SUB",
      "title": "흥례문",
      "description": "...",
      "thumbnailUrl": "https://s3.../heungnyemun.jpg",
      "collectedAt": null,
      "orderIndex": 2,
      "collected": false
    }
  ]
}
```

정렬: `collectedAt` 최신순, 미수집(`collectedAt = null`) 항목은 뒤로 정렬됩니다.

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

TREASURE 타입 스팟 도감. **전체 수집 가능 보물 목록**을 반환하며, 각 아이템의 `collected`로 수집 여부를 표현합니다. `user_treasure_status`(status: GET) 기반.

#### 5.2.1 내 Treasure 컬렉션 조회

```
GET /api/v1/collections/treasures
Authorization: Bearer <accessToken>
```

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| tourId | long | X | 투어 ID |
| lang | string | X | ko, en, jp, cn (현재 미사용) |

**Response 200**

```json
{
  "totalCollected": 2,
  "totalAvailable": 5,
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
    },
    {
      "spotId": 10,
      "tourId": 1,
      "tourTitle": "경복궁 조선의 왕의 날",
      "title": "감춰진 열쇠",
      "description": "...",
      "thumbnailUrl": "https://s3.../treasure2.jpg",
      "gotAt": null,
      "orderIndex": 2,
      "collected": false
    }
  ]
}
```

정렬: `gotAt` 최신순, 미수집(`gotAt = null`) 항목은 뒤로 정렬됩니다.

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
| lang | string | X | ko, en, jp, cn (현재 미사용) |

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

#### 5.4.1 제출 목록(상태 필터)

```
GET /api/v1/admin/photo-submissions
GET /api/v1/admin/photo-submissions?status=PENDING
```

| Query | 타입 | 설명 |
|-------|------|------|
| status | string | X | `PENDING`(기본), `APPROVED`, `REJECTED`, `ALL` |

**Response 200**

```json
[
  {
    "submissionId": 102,
    "spotId": 5,
    "spotTitle": "근정전 앞 광장",
    "photoUrl": "https://s3.../uploaded_photo.jpg",
    "status": "PENDING",
    "submittedAt": "2026-02-14T12:00:00",
    "userNickname": "여행자A"
  }
]
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
GET /api/v1/spots/{spotId}/detail
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
  "lat": 37.576,
  "lng": 126.977,
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
| GET /tours, /tours/{id} | 불필요 |
| GET /spots/{id} | 불필요 (스팟 상세) |
| GET /spots/{id}/guide | **JWT 필수** |
| GET /content-steps/{stepId}/mission | **JWT 필수** |
| GET /photo-spots, /photo-spots/{id} | 불필요 |
| POST /photo-spots/{id}/submissions | JWT 필수 |
| GET /photo-spots/my-submissions | JWT 필수 |
| GET /collections/places, /places/summary, /treasures, /treasures/summary | JWT 필수 |
| POST /tours/{id}/access/unlock | JWT 필수 |
| POST /tours/{id}/runs | JWT 필수 |
| POST /tour-runs/{id}/proximity | JWT 필수 |
| POST /tour-runs/{id}/treasures/{spotId}/collect | JWT 필수 |
| GET /tour-runs/{id}/next-spot | JWT 필수 |
| GET /tour-runs/{id}/spots/{spotId}/chat-session | JWT 필수 |
| GET /chat-sessions/{id}/turns | JWT 필수 |
| GET /chat-sessions/{id}/turns/{nextTurnId} | JWT 필수 |
| POST /chat-sessions/{id}/messages | JWT 필수 |
| POST /tour-runs/{id}/missions/{stepId}/submit | JWT 필수 |
| POST /upload, DELETE /upload | JWT 또는 세션 |
| /admin/** | ADMIN 권한 필수 (JWT 또는 세션) |

**ADMIN 권한 부여 기준**

- `APP_AUTH_ADMIN_EMAILS`: 관리자 이메일 allowlist (쉼표 구분)
- `APP_AUTH_ADMIN_USER_IDS`: 관리자 userId(UUID) allowlist (쉼표 구분)
- 둘 중 하나라도 매칭되면 `ADMIN`, 아니면 `USER`
- 두 allowlist가 모두 비어 있으면 하위 호환을 위해 인증 사용자 전체를 `ADMIN`으로 처리

**JWT/Refresh 주요 환경변수**

- `JWT_ACCESS_TOKEN_EXPIRATION_MS`: 액세스 토큰 만료(ms)
- `JWT_REFRESH_TOKEN_EXPIRATION_MS`: 리프레시 토큰 만료(ms)
- `JWT_REFRESH_COOKIE_NAME`: 리프레시 쿠키 이름
- `JWT_REFRESH_COOKIE_SECURE`: 리프레시 쿠키 `Secure` 플래그 (`true` 권장, HTTPS 환경)

---

## Swagger UI

개발 환경에서 API 문서 확인:

- `/swagger-ui.html`
- `/swagger-ui/index.html`
