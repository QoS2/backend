# 수집(Collections) API 설계

프론트 수집 기능을 위한 Place·Treasure·Photo Spot 전용 API 설계안입니다.

---

## 개요

| 영역 | 용도 | 핵심 기능 |
|------|------|-----------|
| **Place Collection** | 플레이스 도감 | 방문한 MAIN/SUB 스팟 모아보기 |
| **Treasure Collection** | 트레저 도감 | 발견한 보물 스팟 모아보기 |
| **Photo Spot** | 포토 스팟 | 사진 찍기 좋은 장소 + 유저 포토 제출 → 검증 → 민트 → 노출 |

---

## 1. Place Collection (플레이스 도감)

### 개념

- MAIN, SUB 타입 스팟을 "도감"처럼 수집
- `user_spot_progress`(completed) 기반으로 수집 여부 판단
- 투어별·전체 플레이스 목록 조회

### API

#### 1.1 내 Place 컬렉션 조회

```
GET /api/v1/collections/places
Authorization: Bearer <accessToken>
```

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
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
      "orderIndex": 1
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| totalCollected | 수집한 플레이스 수 |
| totalAvailable | 해당 투어(또는 전체) 플레이스 수 |
| items[].collectedAt | user_spot_progress.completed_at |

#### 1.2 Place 컬렉션 요약 (도감 진행률)

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

## 2. Treasure Collection (트레저 도감)

### 개념

- TREASURE 타입 스팟을 "도감"처럼 수집
- `user_treasure_status`(status: GOT) 기반
- 플레이스와 동일한 패턴으로 API 설계

### API

#### 2.1 내 Treasure 컬렉션 조회

```
GET /api/v1/collections/treasures
Authorization: Bearer <accessToken>
```

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
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
      "orderIndex": 1
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| gotAt | user_treasure_status.got_at |

#### 2.2 Treasure 컬렉션 요약

```
GET /api/v1/collections/treasures/summary
Authorization: Bearer <accessToken>
```

---

## 3. Photo Spot API (포토 스팟)

### 개념

- **포토 스팟**: 사진 찍기 좋은 장소 (PHOTO 타입 스팟)
- **유저 포토**: 해당 장소에서 유저가 찍은 사진
- **흐름**: 제출 → 관리자 검증 → 승인 시 민트 → 다른 유저에게 노출

### 추정 ERD (신규 테이블)

```
user_photo_submissions {
  long id PK
  uuid user_id FK
  long spot_id FK (tour_spots, type=PHOTO)
  long asset_id FK (media_assets)
  string status  -- PENDING, APPROVED, REJECTED
  text reject_reason
  timestamp submitted_at
  timestamp verified_at
  uuid verified_by FK (admin)
  string mint_token  -- 민트 식별자 (선택)
  boolean is_public  -- 노출 여부
  timestamp created_at
  timestamp updated_at
}
```

### API

#### 3.1 포토 스팟 목록 조회 (사진 찍기 좋은 장소)

```
GET /api/v1/photo-spots
```

**인증:** 불필요 (공개)

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| tourId | long | X | 투어 ID |
| lang | string | X | ko, en, jp, cn |

**Response 200**

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

| 필드 | 설명 |
|------|------|
| userPhotoCount | 승인되어 노출 중인 유저 포토 수 |
| samplePhotos | 갤러리용 샘플 (검증·민트 완료된 포토) |

#### 3.2 포토 스팟 상세 + 유저 포토 갤러리

```
GET /api/v1/photo-spots/{spotId}
```

**Response 200**

```json
{
  "spotId": 5,
  "title": "근정전 앞 광장",
  "description": "...",
  "thumbnailUrl": "...",
  "latitude": 37.5796,
  "longitude": 126.9769,
  "officialPhotos": [...],
  "userPhotos": [
    {
      "submissionId": 101,
      "url": "https://s3.../user_photo.jpg",
      "submittedBy": { "nickname": "여행자A", "avatarUrl": null },
      "mintedAt": "2026-02-10T14:00:00",
      "likeCount": 12
    }
  ]
}
```

#### 3.3 포토 제출 (유저가 포토 스팟에서 촬영 후 제출)

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

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| photoUrl | string | O | S3 업로드 API로 먼저 업로드한 URL |

**Response 201**

```json
{
  "submissionId": 102,
  "status": "PENDING",
  "message": "검증 후 승인되면 민트 및 갤러리 노출됩니다."
}
```

#### 3.4 내 포토 제출 목록

```
GET /api/v1/photo-spots/my-submissions
Authorization: Bearer <accessToken>
```

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| status | string | X | PENDING, APPROVED, REJECTED |
| spotId | long | X | 포토 스팟 ID |

**Response 200**

```json
{
  "items": [
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
}
```

---

## 4. 관리자 API (포토 검증·민트)

#### 4.1 포토 제출 검증 대기 목록

```
GET /api/v1/admin/photo-submissions
?status=PENDING
```

#### 4.2 포토 승인/거절

```
PATCH /api/v1/admin/photo-submissions/{submissionId}
```

**Request Body**

```json
{
  "action": "APPROVE"
}
```
또는
```json
{
  "action": "REJECT",
  "rejectReason": "포토 스팟과 무관한 이미지입니다."
}
```

#### 4.3 민트 처리 (승인 후)

- 승인 시 자동 민트 또는 별도 트리거
- `mint_token` 저장, `is_public = true` 설정

---

## 5. 인증 정리

| API | 인증 |
|-----|------|
| GET /collections/places | 필수 |
| GET /collections/treasures | 필수 |
| GET /photo-spots | 불필요 |
| GET /photo-spots/{id} | 불필요 |
| POST /photo-spots/{id}/submissions | 필수 |
| GET /photo-spots/my-submissions | 필수 |
| GET,PATCH /admin/photo-submissions | 세션(관리자) |

---

## 6. Figma 연동 참고

- **Place/Treasure 도감**: 카드 그리드, 수집률 바, 잠금/해제 상태
- **Photo Spot**: 스팟 갤러리, 유저 포토 그리드, 제출 버튼, 검증 상태 배지

피그마 화면 확정 시 UI 컴포넌트와 필드 매핑을 보완합니다.
