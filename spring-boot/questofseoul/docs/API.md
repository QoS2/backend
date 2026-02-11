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

모든 API는 아래 형식의 에러 응답을 반환할 수 있습니다.

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
| error | string | HTTP 상태 메시지 |
| errorCode | string | 에러 코드 (VALIDATION_FAILED, NOT_FOUND, AUTHENTICATION_FAILED 등) |
| message | string | 사용자에게 표시할 메시지 |
| timestamp | string | ISO 8601 형식 타임스탬프 |
| path | string | 요청 경로 |
| errors | object | 필드별 검증 에러 (optional) |

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (No Content) |
| 400 | 잘못된 요청 (검증 실패 등) |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 충돌 (중복, IllegalState 등) |
| 500 | 서버 오류 |

---

## 1. 인증 (Auth)

**Base Path:** `/api/v1/auth`

---

### 1.1 JWT 로그인

```
POST /api/v1/auth/login
```

이메일/비밀번호로 로그인하여 JWT 액세스 토큰을 발급합니다.

**Request Body**

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

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | string | JWT 액세스 토큰 |
| expiresIn | number | 토큰 만료 시간(초) |
| tokenType | string | "Bearer" |

**에러:** 401 - 이메일/비밀번호 불일치

---

### 1.2 회원가입

```
POST /api/v1/auth/register
```

가입 후 바로 로그인된 상태로 액세스 토큰을 반환합니다.

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "닉네임"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | O | 이메일 (형식 검증) |
| password | string | O | 비밀번호 (8자 이상) |
| nickname | string | X | 닉네임 (없으면 email 사용) |

**Response 201**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

**에러:** 409 - 이미 존재하는 이메일

---

### 1.3 현재 사용자 조회

```
GET /api/v1/auth/me
```

JWT 또는 세션으로 인증된 사용자 ID를 조회합니다.

**Headers:** `Authorization: Bearer <token>` 또는 세션 쿠키

**Response 200**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**에러:** 401 - 인증되지 않음

---

### 1.4 OAuth2 → JWT 토큰 발급

```
POST /api/v1/auth/token
```

OAuth2(구글 등) 로그인 후 세션이 있으면 JWT 액세스 토큰을 발급합니다.

**Headers:** 세션 쿠키 필요

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

**에러:** 401 - 세션 없음

---

## 2. 파일 업로드

**Base Path:** `/api/v1/upload`

---

### 2.1 파일 업로드

```
POST /api/v1/upload
Content-Type: multipart/form-data
```

이미지 또는 오디오 파일을 S3에 업로드하고 URL을 반환합니다.
S3가 비활성화된 경우 400 에러가 발생합니다.

**Headers:** `Authorization: Bearer <token>` 또는 세션

**Request (multipart/form-data)**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| file | File | O | 업로드 파일 |
| type | string | X | "image" \| "audio" (미지정 시 Content-Type으로 판별) |

**지원 형식**

- 이미지: jpeg, png, gif, webp
- 오디오: mp3, wav, ogg, m4a

**Response 200**

```json
{
  "url": "https://s3.ap-northeast-2.amazonaws.com/bucket/path/filename.jpg"
}
```

**에러:** 400 - S3 비활성화 / 401 - 인증 필요

---

## 3. 퀘스트 (Quest)

**Base Path:** `/api/v1/quests`

---

### 3.1 활성 퀘스트 목록 조회

```
GET /api/v1/quests
```

활성화된(isActive=true) 퀘스트 목록을 조회합니다.

**Response 200**

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "경복궁 한복체험",
    "subtitle": "조선의 궁궐을 걸으며",
    "theme": "HISTORY",
    "tone": "STORY",
    "difficulty": "EASY",
    "estimatedMinutes": 60,
    "startLocationLongitude": 126.9769,
    "startLocationLatitude": 37.5796,
    "isActive": true,
    "createdAt": "2026-01-01T00:00:00"
  }
]
```

**QuestTheme:** HISTORY, FOOD, FUN, PERSON, ARCHITECTURE, SENSORY  
**QuestTone:** STORY, ADVENTURE, RELAX 등  
**Difficulty:** EASY, MEDIUM, HARD

---

### 3.2 퀘스트 상세 조회

```
GET /api/v1/quests/{questId}
```

| Path | 타입 | 설명 |
|------|------|------|
| questId | UUID | 퀘스트 ID |

**Response 200:** 동일한 QuestResponse 객체 1개

**에러:** 404 - 퀘스트 없음

---

### 3.3 도착 확인

```
POST /api/v1/quests/{questId}/check-arrival
```

사용자 위치가 퀘스트 시작 장소로부터 100m 이내인지 확인합니다.

**Request Body**

```json
{
  "latitude": 37.5796,
  "longitude": 126.9769
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| latitude | number | 위도 |
| longitude | number | 경도 |

**Response 200**

```json
{
  "isArrived": true,
  "distanceMeters": 45.2,
  "canStart": true
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| isArrived | boolean | 100m 이내 도착 여부 |
| distanceMeters | number | 거리(미터) |
| canStart | boolean | 퀘스트 시작 가능 여부 |

---

### 3.4 퀘스트 시작

```
POST /api/v1/quests/{questId}/start
```

**인증:** Session (세션 기반)

**Response 200:** 빈 본문

**에러:** 401 - 인증 필요

---

### 3.5 현재 노드 조회

```
GET /api/v1/quests/{questId}/current-node
```

**인증:** Session

사용자의 퀘스트 진행 상황에 따른 현재 노드 메타데이터를 조회합니다.

**Response 200**

```json
{
  "nodeId": "660e8400-e29b-41d4-a716-446655440001",
  "nodeType": "LOCATION",
  "title": "근정전 앞",
  "latitude": 37.5796,
  "longitude": 126.9769,
  "hasContent": true
}
```

**NodeType:** LOCATION, WALK, VIEW, EAT, LISTEN, REFLECTION

**에러:** 404 - 진행 중인 퀘스트 없음

---

### 3.6 노드 콘텐츠 조회

```
GET /api/v1/quests/nodes/{nodeId}/content
```

```
GET /api/v1/quests/{questId}/current-node/content
```

특정 노드 또는 현재 노드의 콘텐츠를 조회합니다.

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| lang | string | KO | KO, EN, JP, CN |

**Response 200**

```json
{
  "nodeId": "660e8400-e29b-41d4-a716-446655440001",
  "title": "근정전 앞",
  "contents": [
    {
      "contentId": "770e8400-e29b-41d4-a716-446655440002",
      "contentOrder": 1,
      "text": {
        "script": "경복궁의 정전인 근정전은...",
        "displayMode": "PARAGRAPH"
      },
      "audio": {
        "audioUrl": "https://...",
        "durationSec": 120,
        "autoPlay": true
      },
      "uiHints": {
        "showSubtitle": true,
        "allowSpeedControl": true,
        "allowReplay": true
      }
    }
  ],
  "totalContents": 3,
  "currentContentOrder": 1
}
```

**DisplayMode:** PARAGRAPH, BULLET, TITLE 등

---

### 3.7 콘텐츠 소비 완료

```
POST /api/v1/quests/nodes/{nodeId}/content-complete
```

**인증:** Session

**Query Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| questId | UUID | 퀘스트 ID |
| contentId | UUID | 콘텐츠 ID |
| contentOrder | integer | 콘텐츠 순서 |

**Response 200**

```json
{
  "hasNextContent": true,
  "nextContentOrder": 2,
  "nextActionEnabled": true
}
```

---

### 3.8 노드 액션 목록 조회

```
GET /api/v1/quests/nodes/{nodeId}/actions
```

**Response 200**

```json
{
  "nodeId": "660e8400-e29b-41d4-a716-446655440001",
  "nodeTitle": "근정전 앞",
  "actions": [
    {
      "actionId": "880e8400-e29b-41d4-a716-446655440003",
      "actionType": "CHOICE",
      "prompt": "어떤 경로로 이동할까요?",
      "options": {
        "choices": ["동쪽", "서쪽", "정면"]
      }
    }
  ]
}
```

**ActionType:** CHOICE, PHOTO, TEXT_INPUT, TIMER, EAT_CONFIRM

---

### 3.9 액션 제출

```
POST /api/v1/quests/nodes/{nodeId}/actions/{actionId}/submit
```

**인증:** Session

**Query Parameters:** `questId` (UUID)

**Request Body**

```json
{
  "userInput": "사용자 입력 텍스트",
  "photoUrl": "https://s3.../photo.jpg",
  "selectedOption": { "key": "value" }
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
  "success": true,
  "effects": [
    {
      "type": "UNLOCK_NODE",
      "value": "990e8400-e29b-41d4-a716-446655440004"
    }
  ],
  "nextNodeUnlocked": "990e8400-e29b-41d4-a716-446655440004"
}
```

---

### 3.10 이동 메시지 조회

```
GET /api/v1/quests/nodes/{fromNodeId}/transition/{toNodeId}
```

두 노드 간 이동 시 재생할 메시지(텍스트/오디오)를 조회합니다.

**Query Parameters:** `lang` (KO, EN, JP, CN, 기본 KO)

**Response 200**

```json
{
  "fromNodeId": "660e8400-e29b-41d4-a716-446655440001",
  "toNodeId": "990e8400-e29b-41d4-a716-446655440004",
  "messages": [
    {
      "transitionOrder": 1,
      "messageType": "TEXT",
      "textContent": "다음 장소로 이동합니다...",
      "audioUrl": "https://..."
    }
  ]
}
```

---

### 3.11 퀘스트 진행 내역 조회

```
GET /api/v1/quests/{questId}/history
```

**인증:** Session

**Response 200**

```json
{
  "questId": "550e8400-e29b-41d4-a716-446655440000",
  "questTitle": "경복궁 한복체험",
  "history": [
    {
      "nodeId": "660e8400-e29b-41d4-a716-446655440001",
      "nodeTitle": "근정전 앞",
      "actions": [
        {
          "actionType": "PHOTO",
          "userInput": null,
          "photoUrl": "https://...",
          "selectedOption": null,
          "createdAt": "2026-02-11T12:00:00"
        }
      ]
    }
  ]
}
```

---

### 3.12 퀘스트 완료

```
POST /api/v1/quests/{questId}/complete
```

**인증:** Session

**Response 200**

```json
{
  "questId": "550e8400-e29b-41d4-a716-446655440000",
  "questTitle": "경복궁 한복체험",
  "completedAt": "2026-02-11T14:30:00",
  "reportReady": false,
  "reportUrl": null
}
```

---

## 4. 투어 (Tour)

**Base Path:** `/api/v1`

---

### 4.1 투어 목록

```
GET /api/v1/tours
```

**Response 200**

```json
[
  {
    "id": 1,
    "externalKey": "gyeongbokgung",
    "titleEn": "Gyeongbokgung Palace"
  }
]
```

---

### 4.2 투어 상세

```
GET /api/v1/tours/{tourId}
```

퀘스트 디테일 페이지용 - 태그, Place/게임/보물/포토 수, 설명, info, good_to_know 포함.

**Response 200**

```json
{
  "id": 1,
  "externalKey": "gyeongbokgung",
  "titleEn": "Gyeongbokgung Palace",
  "descriptionEn": "The main royal palace...",
  "infoJson": {
    "entranceFee": "3000원",
    "hours": "09:00-18:00"
  },
  "goodToKnowJson": {
    "tips": ["한복 입장 무료"]
  },
  "tags": ["history", "palace"],
  "stepsCount": 8,
  "waypointsCount": 12,
  "photoSpotsCount": 5,
  "treasuresCount": 3,
  "quizzesCount": 4,
  "startLatitude": 37.5796,
  "startLongitude": 126.9769,
  "unlocked": true
}
```

**unlocked:** 로그인 사용자 기준 이미 진행한 투어인지 여부

---

### 4.3 마커 목록

```
GET /api/v1/tours/{tourId}/markers
```

맵용 마커 목록. 타입별 필터링 가능.

**Query Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| filter | string | STEP, WAYPOINT, PHOTO_SPOT, TREASURE (미지정 시 전체) |

**Response 200**

```json
[
  {
    "id": 1,
    "type": "STEP",
    "title": "Gwanghwamun Gate",
    "latitude": 37.5720,
    "longitude": 126.9769,
    "radiusM": 50,
    "refId": 1,
    "stepOrder": 1
  }
]
```

**MarkerType:** STEP(Place), WAYPOINT(Sub Place), PHOTO_SPOT, TREASURE

---

### 4.4 투어 시작

```
POST /api/v1/tours/{tourId}/start
```

**인증:** Bearer JWT

**Response 200**

```json
{
  "tourRunId": 101,
  "tourId": 1,
  "chatSessionId": 201
}
```

---

### 4.5 근접 감지

```
POST /api/v1/tour-runs/{tourRunId}/proximity
```

**인증:** Bearer JWT

사용자가 50m 이내 마커에 진입하면 준비된 대사(가이드/웨이포인트 메시지)를 반환합니다.
근접 마커가 없으면 204 No Content.

**Request Body**

```json
{
  "latitude": 37.5796,
  "longitude": 126.9769
}
```

**Query Parameters:** `lang` (KO, EN, JP, CN)

**Response 200**

```json
{
  "event": "PROXIMITY",
  "contentType": "GUIDE",
  "sessionId": 201,
  "context": {
    "refType": "STEP",
    "refId": 1,
    "placeName": "Gwanghwamun Gate"
  },
  "messages": [
    {
      "turnId": 501,
      "role": "ASSISTANT",
      "source": "GUIDE",
      "text": "경복궁의 정문인 광화문입니다...",
      "assets": [{"id": 1, "type": "IMAGE", "url": "https://...", "meta": null}],
      "action": {"type": "NEXT_STEP", "label": "다음", "stepId": 2}
    }
  ]
}
```

---

### 4.6 채팅 히스토리

```
GET /api/v1/chat-sessions/{sessionId}/turns
```

**인증:** Bearer JWT

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
      "createdAt": "2026-02-11T12:00:00"
    },
    {
      "id": 502,
      "role": "ASSISTANT",
      "source": "AI",
      "text": "근정전은 1395년 태조에 의해...",
      "assets": null,
      "action": null,
      "createdAt": "2026-02-11T12:00:05"
    }
  ]
}
```

---

### 4.7 채팅 메시지 전송

```
POST /api/v1/chat-sessions/{sessionId}/messages
```

**인증:** Bearer JWT

유저 질문을 전송하고 AI 응답을 받습니다.

**Request Body**

```json
{
  "text": "이 건물의 역사가 궁금해요"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| text | string | O | 사용자 메시지 |

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

### 4.8 스텝 가이드

```
GET /api/v1/steps/{stepId}/guide
```

스텝 페이지용 가이드 세그먼트(설명 + 이미지) 조회.

**Query Parameters:** `lang` (KO, EN, JP, CN)

**Response 200**

```json
{
  "stepId": 1,
  "stepTitle": "Gwanghwamun Gate",
  "segments": [
    {
      "id": 10,
      "segIdx": 1,
      "textEn": "Gwanghwamun is the main gate...",
      "triggerKey": null,
      "media": [
        {"id": 1, "url": "https://...", "meta": null}
      ]
    }
  ]
}
```

---

### 4.9 스텝 퀴즈 목록

```
GET /api/v1/steps/{stepId}/quizzes
```

**Response 200**

```json
[
  {
    "id": 1,
    "externalKey": "quiz-gate-name",
    "type": "MULTIPLE_CHOICE",
    "promptEn": "광화문의 한자 표기는?",
    "specJson": {"options": ["光化門", "景福門", "興仁門"]},
    "hintEn": "광명을 비추는 문",
    "mintReward": 10,
    "hasHint": true
  }
]
```

**QuizType:** MULTIPLE_CHOICE, TRUE_FALSE, PHOTO_MATCH 등

---

## 5. 관리자 API

모든 관리자 API는 **세션 인증** 필요 (`sessionAuth`).

---

### 5.1 퀘스트 관리

**Base Path:** `/api/v1/admin/quests`

#### 목록 조회

```
GET /api/v1/admin/quests?isActive=true&theme=HISTORY&page=0&size=20
```

| Query | 타입 | 설명 |
|-------|------|------|
| isActive | boolean | 활성 여부 필터 |
| theme | string | QuestTheme |
| page | int | 페이지 (0부터) |
| size | int | 페이지 크기 (기본 20) |

**Response 200:** `Page<QuestResponse>`

#### 단건 조회

```
GET /api/v1/admin/quests/{questId}
```

#### 생성

```
POST /api/v1/admin/quests
```

**Request Body (QuestCreateRequest)**

```json
{
  "title": "제목",
  "subtitle": "부제목",
  "theme": "HISTORY",
  "tone": "STORY",
  "difficulty": "EASY",
  "estimatedMinutes": 60,
  "startLocationLatitude": 37.5796,
  "startLocationLongitude": 126.9769
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| title | O | 제목 |
| subtitle | X | 부제목 |
| theme | O | QuestTheme |
| tone | O | QuestTone |
| difficulty | O | Difficulty |
| estimatedMinutes | X | 예상 소요 분 |
| startLocationLatitude | X | 시작 위도 |
| startLocationLongitude | X | 시작 경도 |

#### 수정

```
PATCH /api/v1/admin/quests/{questId}
```

**Request Body (QuestUpdateRequest):** 위와 동일, 모든 필드 optional. `isActive` 추가.

#### 삭제

```
DELETE /api/v1/admin/quests/{questId}
```

**Response 204**

#### 활성 토글

```
PATCH /api/v1/admin/quests/{questId}/active?active=true
```

---

### 5.2 퀘스트 노드

**Base Path:** `/api/v1/admin/quests/{questId}/nodes`

#### 목록/단건/생성/수정/삭제

CRUD 표준. 생성 시 **NodeCreateRequest:**

```json
{
  "nodeType": "LOCATION",
  "title": "근정전 앞",
  "orderIndex": 1,
  "geoLatitude": 37.5796,
  "geoLongitude": 126.9769,
  "unlockCondition": {}
}
```

#### 순서 일괄 변경

```
PATCH /api/v1/admin/quests/{questId}/nodes/reorder
```

**Request Body**

```json
{
  "nodes": [
    {"nodeId": "uuid-1", "orderIndex": 1},
    {"nodeId": "uuid-2", "orderIndex": 2}
  ]
}
```

---

### 5.3 노드 콘텐츠

**Base Path:** `/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents`

**ContentCreateRequest**

```json
{
  "contentOrder": 1,
  "contentType": "TEXT",
  "language": "KO",
  "body": "본문 텍스트",
  "audioUrl": "https://...",
  "voiceStyle": null,
  "displayMode": "PARAGRAPH"
}
```

**ContentType:** TEXT, AUDIO, AI_PROMPT  
**Language:** KO, EN, JP, CN  
**DisplayMode:** PARAGRAPH, BULLET 등

---

### 5.4 노드 액션

**Base Path:** `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions`

**ActionCreateRequest**

```json
{
  "actionType": "CHOICE",
  "prompt": "선택하세요",
  "options": {"choices": ["A", "B", "C"]}
}
```

단건 조회 시 `?includeEffects=true`(기본)로 이펙트 포함.

---

### 5.5 액션 이펙트

**Base Path:** `/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects`

**EffectCreateRequest**

```json
{
  "effectType": "UNLOCK_NODE",
  "effectValue": {"targetNodeId": "uuid"}
}
```

**EffectType:** UNLOCK_NODE, SET_STATE 등

---

### 5.6 노드 전환

**Base Path (퀘스트 기준):** `/api/v1/admin/quests/{questId}/transitions`

**TransitionCreateRequest**

```json
{
  "fromNodeId": "uuid-from",
  "toNodeId": "uuid-to",
  "transitionOrder": 1,
  "messageType": "TEXT",
  "textContent": "이동 메시지",
  "audioUrl": "https://...",
  "language": "KO"
}
```

**노드 기준 목록**

- `GET .../nodes/{nodeId}/transitions/outgoing` - 나가는 전환
- `GET .../nodes/{nodeId}/transitions/incoming` - 들어오는 전환

---

### 5.7 투어 관리

**Base Path:** `/api/v1/admin/tours`

#### 목록

```
GET /api/v1/admin/tours?page=0&size=20
```

#### 생성

**TourCreateRequest**

```json
{
  "externalKey": "gyeongbokgung",
  "titleEn": "Gyeongbokgung Palace",
  "descriptionEn": "The main royal palace of Joseon.",
  "infoJson": {"entranceFee": "3000원"},
  "goodToKnowJson": {"tips": []}
}
```

#### 수정 (TourUpdateRequest)

모든 필드 optional.

#### 모바일 미리보기 AI 채팅

```
POST /api/v1/admin/tours/{tourId}/preview/chat
```

**Request Body**

```json
{
  "text": "이 궁에 대해 알려줘",
  "history": [
    {"role": "user", "content": "안녕"},
    {"role": "assistant", "content": "안녕하세요!"}
  ]
}
```

**Response 200**

```json
{
  "aiText": "경복궁은 1395년에..."
}
```

---

### 5.8 투어 스텝

**Base Path:** `/api/v1/admin/tours/{tourId}/steps`

**StepCreateRequest**

```json
{
  "externalKey": "step-gwanghwamun",
  "stepOrder": 1,
  "titleEn": "Gwanghwamun Gate",
  "shortDescEn": "The main gate",
  "latitude": 37.5720,
  "longitude": 126.9769,
  "radiusM": 50
}
```

---

### 5.9 Enum

```
GET /api/v1/admin/enums/{enumName}
```

**enumName:** questTheme, questTone, difficulty, nodeType, contentType, actionType, effectType, language, displayMode, transitionMessageType

**Response 200**

```json
["HISTORY", "FOOD", "FUN", "PERSON", "ARCHITECTURE", "SENSORY"]
```

---

## Swagger UI

개발 환경에서 `/swagger-ui.html` 또는 `/swagger-ui/index.html` 에서 대화형 API 문서를 확인할 수 있습니다.
