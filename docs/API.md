# Quest of Seoul API 문서

## 개요

- **Base URL**: `http://localhost:8080` (개발) / 운영 환경에 따라 상이
- **API 버전**: `v1`
- **공통 prefix**: `/api/v1`
- **인증**: 다음 두 가지 방식 지원  
  - **JWT**: `POST /api/v1/auth/login` 또는 `/api/v1/auth/register`로 발급한 액세스 토큰을 `Authorization: Bearer <token>` 헤더에 포함  
  - **세션**: Google OAuth2 로그인 후 발급되는 세션 쿠키 (관리자 API 등에서 `sessionAuth` 표시 시 사용)

---

## 인증

로그인이 필요한 API는 다음 중 하나로 인증합니다.

- **JWT**: 이메일/비밀번호 로그인 또는 회원가입 후 받은 `accessToken`을 요청 헤더에 `Authorization: Bearer <accessToken>` 로 넣습니다.
- **세션**: Google OAuth2 로그인 후 동일 도메인에서 발급된 세션 쿠키를 사용합니다.  
  인증 후 `/api/v1/auth/me`로 현재 사용자 확인 가능합니다.

---

## 공통 에러 응답

에러 시 아래 형식으로 반환됩니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `error` | string | HTTP 상태 메시지 (예: "Not Found") |
| `errorCode` | string | 애플리케이션 에러 코드 (예: "RESOURCE_NOT_FOUND") |
| `message` | string | 사용자용 메시지 |
| `timestamp` | string (ISO-8601) | 발생 시각 |
| `path` | string | 요청 URI (선택) |
| `errors` | object | 필드별 검증 에러 (validation 실패 시) |

**HTTP 상태 코드**

- `400` Bad Request — 잘못된 요청/검증 실패
- `401` Unauthorized — 미로그인 또는 인증 실패
- `403` Forbidden — 권한 없음
- `404` Not Found — 리소스 없음
- `409` Conflict — 중복/비즈니스 규칙 위반
- `500` Internal Server Error — 서버 오류

---

# 공개 API (클라이언트/앱)

## 1. 인증 (Auth)

### 현재 사용자 조회

JWT 또는 세션으로 인증된 현재 사용자 정보를 조회합니다.

```
GET /api/v1/auth/me
```

- **인증**: `Authorization: Bearer <token>` 또는 세션 쿠키

**Response** `200 OK`

```json
{
  "userId": "uuid-string"
}
```

- 미로그인: `401 Unauthorized`

### JWT 로그인 (이메일 / 비밀번호)

이메일과 비밀번호로 로그인하여 JWT 액세스 토큰을 발급합니다. 인증 불필요.

```
POST /api/v1/auth/login
```

**Request Body**

```json
{
  "email": "admin@example.com",
  "password": "your-password"
}
```

**Response** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

- 잘못된 이메일/비밀번호 또는 비밀번호 미설정 계정(Google 전용): `401 Unauthorized`

---

### 회원가입 (이메일 / 비밀번호)

이메일·비밀번호로 회원가입합니다. 가입 후 JWT 로그인으로 로그인할 수 있습니다. 인증 불필요.

```
POST /api/v1/auth/register
```

**Request Body**

```json
{
  "email": "admin@example.com",
  "password": "password8chars",
  "nickname": "닉네임"
}
```

- `password`: 8자 이상
- `nickname`: 선택

**Response** `201 Created`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

- 동일 이메일 이미 존재: `409 Conflict`

---

### OAuth2 → JWT 토큰 발급

Google OAuth2 로그인 후 세션이 있으면 JWT 액세스 토큰을 발급합니다. 클라이언트는 이후 API 호출 시 `Authorization: Bearer <token>` 으로 사용할 수 있습니다.

```
POST /api/v1/auth/token
```

- **인증**: 세션 쿠키 필요 (OAuth2 로그인 후)

**Response** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

- `expiresIn`: 토큰 유효 시간(초). 기본 24시간.
- 이후 API 호출 시 요청 헤더에 `Authorization: Bearer <accessToken>` 를 넣으면 세션 없이 인증 가능.

---

## 2. 퀘스트 (Quests)

### 2.1 활성 퀘스트 목록 조회

활성화된 퀘스트 목록을 조회합니다.

```
GET /api/v1/quests
```

**Response** `200 OK`

```json
[
  {
    "id": "uuid",
    "title": "string",
    "subtitle": "string",
    "theme": "HISTORY",
    "tone": "FRIENDLY",
    "difficulty": "EASY",
    "estimatedMinutes": 60,
    "startLocationLongitude": 127.0,
    "startLocationLatitude": 37.5,
    "isActive": true,
    "createdAt": "2025-01-01T00:00:00"
  }
]
```

---

### 2.2 퀘스트 상세 조회

```
GET /api/v1/quests/{questId}
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |

**Response** `200 OK` — `QuestResponse` (위 목록 항목과 동일)  
**Response** `404 Not Found` — 퀘스트 없음

---

### 2.3 도착 확인

사용자가 퀘스트 시작 위치로부터 약 100m 이내인지 확인합니다.

```
POST /api/v1/quests/{questId}/check-arrival
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |
| Body | - | - | ArrivalCheckRequest |

**Request Body**

```json
{
  "latitude": 37.5,
  "longitude": 127.0
}
```

**Response** `200 OK`

```json
{
  "isArrived": true,
  "distanceMeters": 50.0,
  "canStart": true
}
```

---

### 2.4 퀘스트 시작

사용자에 대한 퀘스트를 초기화합니다. **인증 필요.**

```
POST /api/v1/quests/{questId}/start
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |

**Response** `200 OK` — Body 없음

---

### 2.5 현재 노드 조회

퀘스트 진행 중인 사용자의 현재 노드 메타데이터를 조회합니다. **인증 필요.**

```
GET /api/v1/quests/{questId}/current-node
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |

**Response** `200 OK`

```json
{
  "nodeId": "uuid",
  "nodeType": "VIEW",
  "title": "string",
  "latitude": 37.5,
  "longitude": 127.0,
  "hasContent": true
}
```

**Response** `404 Not Found` — 진행 중인 퀘스트/노드 없음

---

### 2.6 노드 콘텐츠 조회

특정 노드의 모든 콘텐츠를 조회합니다.

```
GET /api/v1/quests/nodes/{nodeId}/content?lang=KO
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | nodeId | UUID | 노드 ID |
| Query | lang | string | 언어 코드 (기본값: KO) |

**Response** `200 OK`

```json
{
  "nodeId": "uuid",
  "title": "string",
  "contents": [
    {
      "contentId": "uuid",
      "contentOrder": 1,
      "text": {
        "script": "string",
        "displayMode": "PARAGRAPH"
      },
      "audio": {
        "audioUrl": "string",
        "durationSec": 60,
        "autoPlay": true
      },
      "uiHints": {
        "showSubtitle": true,
        "allowSpeedControl": true,
        "allowReplay": true
      }
    }
  ],
  "totalContents": 1,
  "currentContentOrder": 1
}
```

---

### 2.7 현재 노드 콘텐츠 조회

사용자의 현재 노드에 대한 콘텐츠를 조회합니다. **인증 필요.**

```
GET /api/v1/quests/{questId}/current-node/content?lang=KO
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |
| Query | lang | string | 언어 코드 (기본값: KO) |

**Response** — 2.6과 동일한 `NodeContentsResponse` 형식

---

### 2.8 콘텐츠 소비 완료

콘텐츠 항목을 “소비 완료”로 표시합니다. **인증 필요.**

```
POST /api/v1/quests/nodes/{nodeId}/content-complete?questId={questId}&contentId={contentId}&contentOrder={contentOrder}
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | nodeId | UUID | 노드 ID |
| Query | questId | UUID | 퀘스트 ID |
| Query | contentId | UUID | 콘텐츠 ID |
| Query | contentOrder | integer | 콘텐츠 순서 |

**Response** `200 OK`

```json
{
  "hasNextContent": true,
  "nextContentOrder": 2,
  "nextActionEnabled": true
}
```

---

### 2.9 액션 제출

사용자의 액션 응답(텍스트/사진/선택 등)을 제출합니다. **인증 필요.**

```
POST /api/v1/quests/nodes/{nodeId}/actions/{actionId}/submit?questId={questId}
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | nodeId | UUID | 노드 ID |
| Path | actionId | UUID | 액션 ID |
| Query | questId | UUID | 퀘스트 ID |
| Body | - | - | ActionSubmitRequest |

**Request Body**

```json
{
  "userInput": "string",
  "photoUrl": "string",
  "selectedOption": {}
}
```

- `userInput`, `photoUrl`, `selectedOption`은 액션 타입에 따라 선택 사용

**Response** `200 OK`

```json
{
  "success": true,
  "effects": [
    {
      "type": "PROGRESS",
      "value": {}
    }
  ],
  "nextNodeUnlocked": "uuid-or-null"
}
```

---

### 2.10 퀘스트 진행 내역 조회

해당 퀘스트에서 사용자가 제출한 액션 내역을 조회합니다. **인증 필요.**

```
GET /api/v1/quests/{questId}/history
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |

**Response** `200 OK`

```json
{
  "questId": "uuid",
  "questTitle": "string",
  "history": [
    {
      "nodeId": "uuid",
      "nodeTitle": "string",
      "actions": [
        {
          "actionType": "TEXT_INPUT",
          "userInput": "string",
          "photoUrl": "string",
          "selectedOption": {},
          "createdAt": "2025-01-01T00:00:00"
        }
      ]
    }
  ]
}
```

---

### 2.11 노드 액션 목록 조회

노드에서 사용 가능한 모든 액션을 조회합니다.

```
GET /api/v1/quests/nodes/{nodeId}/actions
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | nodeId | UUID | 노드 ID |

**Response** `200 OK`

```json
{
  "nodeId": "uuid",
  "nodeTitle": "string",
  "actions": [
    {
      "actionId": "uuid",
      "actionType": "TEXT_INPUT",
      "prompt": "string",
      "options": {}
    }
  ]
}
```

---

### 2.12 이동 메시지 조회

두 노드 간 전환 시 표시할 메시지 목록을 조회합니다.

```
GET /api/v1/quests/nodes/{fromNodeId}/transition/{toNodeId}?lang=KO
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | fromNodeId | UUID | 출발 노드 ID |
| Path | toNodeId | UUID | 도착 노드 ID |
| Query | lang | string | 언어 코드 (기본값: KO) |

**Response** `200 OK`

```json
{
  "fromNodeId": "uuid",
  "toNodeId": "uuid",
  "messages": [
    {
      "transitionOrder": 1,
      "messageType": "TEXT",
      "textContent": "string",
      "audioUrl": "string"
    }
  ]
}
```

---

### 2.13 퀘스트 완료

사용자의 퀘스트를 완료로 표시합니다. **인증 필요.**

```
POST /api/v1/quests/{questId}/complete
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | questId | UUID | 퀘스트 ID |

**Response** `200 OK`

```json
{
  "questId": "uuid",
  "questTitle": "string",
  "completedAt": "2025-01-01T00:00:00",
  "reportReady": false,
  "reportUrl": "string-or-null"
}
```

---

# 파일 업로드 (File Upload)

이미지 또는 오디오 파일을 AWS S3에 업로드하고 URL을 반환합니다. **인증 필요** (JWT 또는 세션).

## 파일 업로드

```
POST /api/v1/upload
Content-Type: multipart/form-data
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Body | file | File | 업로드할 파일 (필수) |
| Query | type | string | 파일 타입 (선택: `image` \| `audio`) - 생략 시 Content-Type으로 자동 판별 |

**지원 파일 형식**: 이미지(jpeg/png/gif/webp), 오디오(mp3/wav/ogg/m4a) | 최대 50MB

**Response** `200 OK`

```json
{
  "url": "https://bucket.s3.region.amazonaws.com/audio/uuid.mp3"
}
```

---

# 관리자 API (Admin)

관리자 API는 모두 **인증 필요**입니다. JWT Bearer 토큰(`Authorization: Bearer <token>`) 또는 세션 쿠키(`sessionAuth`) 중 하나로 인증할 수 있습니다.

## 3. 관리자 - 퀘스트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests` | 퀘스트 목록 (필터/페이징) |
| GET | `/api/v1/admin/quests/{questId}` | 퀘스트 단건 조회 |
| POST | `/api/v1/admin/quests` | 퀘스트 생성 |
| PATCH | `/api/v1/admin/quests/{questId}` | 퀘스트 수정 |
| DELETE | `/api/v1/admin/quests/{questId}` | 퀘스트 삭제 |
| PATCH | `/api/v1/admin/quests/{questId}/active?active=true\|false` | 활성/비활성 토글 |

### GET /api/v1/admin/quests

**Query**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| isActive | boolean | N | 활성 여부 필터 |
| theme | QuestTheme | N | 테마 필터 |
| page | int | N | 페이지 (0부터, 기본 0) |
| size | int | N | 페이지 크기 (기본 20) |

**Response** `200 OK` — Spring `Page<QuestResponse>` (content 배열 + page, size, totalElements 등)

### POST /api/v1/admin/quests (퀘스트 생성)

**Request Body** — QuestCreateRequest

```json
{
  "title": "string",
  "subtitle": "string",
  "theme": "HISTORY",
  "tone": "FRIENDLY",
  "difficulty": "EASY",
  "estimatedMinutes": 60,
  "startLocationLatitude": 37.5,
  "startLocationLongitude": 127.0
}
```

- `title`, `theme`, `tone`, `difficulty` 필수

**Response** `201 Created` — `QuestResponse`

### PATCH /api/v1/admin/quests/{questId} (퀘스트 수정)

**Request Body** — QuestUpdateRequest (모든 필드 선택)

```json
{
  "title": "string",
  "subtitle": "string",
  "theme": "HISTORY",
  "tone": "FRIENDLY",
  "difficulty": "EASY",
  "estimatedMinutes": 60,
  "startLocationLatitude": 37.5,
  "startLocationLongitude": 127.0,
  "isActive": true
}
```

---

## 4. 관리자 - 퀘스트 노드

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests/{questId}/nodes` | 노드 목록 |
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}` | 노드 단건 |
| POST | `/api/v1/admin/quests/{questId}/nodes` | 노드 생성 |
| PATCH | `/api/v1/admin/quests/{questId}/nodes/{nodeId}` | 노드 수정 |
| DELETE | `/api/v1/admin/quests/{questId}/nodes/{nodeId}` | 노드 삭제 |
| PATCH | `/api/v1/admin/quests/{questId}/nodes/reorder` | 노드 순서 일괄 변경 |

### POST /api/v1/admin/quests/{questId}/nodes (노드 생성)

**Request Body** — NodeCreateRequest

```json
{
  "nodeType": "VIEW",
  "title": "string",
  "orderIndex": 1,
  "geoLatitude": 37.5,
  "geoLongitude": 127.0,
  "unlockCondition": {}
}
```

- `nodeType`, `title`, `orderIndex` 필수

**Response** `201 Created` — NodeResponse

```json
{
  "id": "uuid",
  "questId": "uuid",
  "nodeType": "VIEW",
  "title": "string",
  "orderIndex": 1,
  "geoLatitude": 37.5,
  "geoLongitude": 127.0,
  "unlockCondition": {},
  "createdAt": "2025-01-01T00:00:00"
}
```

### PATCH /api/v1/admin/quests/{questId}/nodes/reorder (노드 순서 변경)

**Request Body** — NodeReorderRequest

```json
{
  "nodes": [
    { "nodeId": "uuid", "orderIndex": 0 },
    { "nodeId": "uuid", "orderIndex": 1 }
  ]
}
```

---

## 5. 관리자 - 노드 전환 (Transitions)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests/{questId}/transitions` | 퀘스트 내 전환 목록 |
| GET | `/api/v1/admin/quests/{questId}/transitions/{transitionId}` | 전환 단건 |
| POST | `/api/v1/admin/quests/{questId}/transitions` | 전환 생성 |
| PATCH | `/api/v1/admin/quests/{questId}/transitions/{transitionId}` | 전환 수정 |
| DELETE | `/api/v1/admin/quests/{questId}/transitions/{transitionId}` | 전환 삭제 |

### POST /api/v1/admin/quests/{questId}/transitions (전환 생성)

**Request Body** — TransitionCreateRequest

```json
{
  "fromNodeId": "uuid",
  "toNodeId": "uuid",
  "transitionOrder": 1,
  "messageType": "TEXT",
  "textContent": "string",
  "audioUrl": "string",
  "language": "KO"
}
```

- `fromNodeId`, `toNodeId`, `transitionOrder`, `messageType` 필수

**Response** `201 Created` — TransitionResponse

---

## 6. 관리자 - 노드별 전환 (Outgoing/Incoming)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/transitions/outgoing` | 해당 노드에서 나가는 전환 목록 |
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/transitions/incoming` | 해당 노드로 들어오는 전환 목록 |

**Response** `200 OK` — `List<TransitionResponse>`

---

## 7. 관리자 - 노드 콘텐츠

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents` | 콘텐츠 목록 |
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents/{contentId}` | 콘텐츠 단건 |
| POST | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents` | 콘텐츠 생성 |
| PATCH | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents/{contentId}` | 콘텐츠 수정 |
| DELETE | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents/{contentId}` | 콘텐츠 삭제 |

### POST /api/v1/admin/quests/{questId}/nodes/{nodeId}/contents (콘텐츠 생성)

**Request Body** — ContentCreateRequest

```json
{
  "contentOrder": 1,
  "contentType": "TEXT",
  "language": "KO",
  "body": "string",
  "audioUrl": "string",
  "voiceStyle": "string",
  "displayMode": "PARAGRAPH"
}
```

- `contentOrder`, `contentType`, `language`, `body` 필수

**Response** `201 Created` — ContentResponse

---

## 8. 관리자 - 노드 액션

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions` | 액션 목록 |
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}?includeEffects=true` | 액션 단건 (이펙트 포함 여부) |
| POST | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions` | 액션 생성 |
| PATCH | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}` | 액션 수정 |
| DELETE | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}` | 액션 삭제 |

### POST /api/v1/admin/quests/{questId}/nodes/{nodeId}/actions (액션 생성)

**Request Body** — ActionCreateRequest

```json
{
  "actionType": "TEXT_INPUT",
  "prompt": "string",
  "options": {}
}
```

- `actionType`, `prompt` 필수

**Response** `201 Created` — ActionResponse (effects 배열 포함 가능)

---

## 9. 관리자 - 액션 이펙트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects` | 이펙트 목록 |
| GET | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects/{effectId}` | 이펙트 단건 |
| POST | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects` | 이펙트 생성 |
| PATCH | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects/{effectId}` | 이펙트 수정 |
| DELETE | `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects/{effectId}` | 이펙트 삭제 |

### POST .../effects (이펙트 생성)

**Request Body** — EffectCreateRequest

```json
{
  "effectType": "UNLOCK_NODE",
  "effectValue": {}
}
```

- `effectType`, `effectValue` 필수

**Response** `201 Created` — EffectResponse

---

## 10. 관리자 - Enum

폼/셀렉트용 Enum 상수 목록을 조회합니다.

```
GET /api/v1/admin/enums/{enumName}
```

| 구분 | 이름 | 타입 | 설명 |
|------|------|------|------|
| Path | enumName | string | Enum 이름 (아래 표 참고) |

**지원 enumName**

| enumName | 설명 |
|----------|------|
| questTheme | 퀘스트 테마 |
| questTone | 퀘스트 톤 |
| difficulty | 난이도 |
| nodeType | 노드 타입 |
| contentType | 콘텐츠 타입 |
| actionType | 액션 타입 |
| effectType | 이펙트 타입 |
| language | 언어 |
| displayMode | 디스플레이 모드 |
| transitionMessageType | 전환 메시지 타입 |

**Response** `200 OK`

```json
["HISTORY", "FOOD", "FUN", "PERSON", "ARCHITECTURE", "SENSORY"]
```

---

# Enum 값 참고

- **QuestTheme**: HISTORY, FOOD, FUN, PERSON, ARCHITECTURE, SENSORY
- **QuestTone**: SERIOUS, FRIENDLY, PLAYFUL, EMOTIONAL
- **Difficulty**: EASY, NORMAL, DEEP
- **NodeType**: LOCATION, WALK, VIEW, EAT, LISTEN, REFLECTION
- **ContentType**: TEXT, AUDIO, AI_PROMPT
- **ActionType**: CHOICE, PHOTO, TEXT_INPUT, TIMER, EAT_CONFIRM
- **EffectType**: TAG, PROGRESS, MEMORY, SCORE
- **Language**: KO, EN, JP, CN
- **DisplayMode**: PARAGRAPH, SUBTITLE, QUOTE
- **TransitionMessageType**: TEXT, AUDIO, AI_GENERATED

실제 값은 `GET /api/v1/admin/enums/{enumName}` 응답으로 확인할 수 있습니다.
