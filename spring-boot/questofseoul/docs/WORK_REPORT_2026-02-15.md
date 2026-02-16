# 작업 리포트: 인앱 알람, Place/Treasure/Photo Spot 수집 기능

**작업일:** 2026-02-15  
**참조:** 인앱 알람, place collection, treasure, photo spot 기획서 (Figma 캡처 기반)

---

## 1. 작업 개요

가이드·미션 기능을 제외한 다음 기능을 백엔드에 구현했습니다.

| 영역 | 설명 |
|------|------|
| **인앱 알람** | Main/Sub Place, Treasure, Photo Spot 50m 근접 시 알람 이벤트 |
| **Place Collection** | 방문한 Main/Sub 장소 도감 |
| **Treasure Collection** | 수집한 보물 도감 + Collect Treasure |
| **Photo Spot** | 포토 스팟 목록, 유저 포토 제출, 관리자 검증 |

---

## 2. 구현 API 목록

### 2.1 Proximity API 확장

| event | 설명 |
|-------|------|
| `PROXIMITY` + `GUIDE` | Main/Sub Place 50m 진입 → UserSpotProgress unlock + 가이드 스크립트 |
| `TREASURE_FOUND` | Treasure 50m 진입 (첫 발견) → UserTreasureStatus unlock + 알람 |
| `PHOTO_SPOT_FOUND` | Photo Spot 50m 진입 → 알람 |

**기존:** `POST /api/v1/tour-runs/{runId}/proximity`  
**변경:** 위 3가지 이벤트 타입 순서대로 우선 반환 (Main/Sub 가이드 → Treasure → Photo)

### 2.2 Place Collection

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/collections/places` | 내 Place 컬렉션 (tourId 선택) |
| GET | `/api/v1/collections/places/summary` | 투어별 수집 진행률 요약 |

### 2.3 Treasure Collection

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/collections/treasures` | 내 Treasure 컬렉션 |
| GET | `/api/v1/collections/treasures/summary` | 투어별 수집 요약 |
| POST | `/api/v1/tour-runs/{runId}/treasures/{spotId}/collect` | 보물 수집 (Collect Treasure) |

### 2.4 스팟 상세 (더블모달)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/spots/{spotId}` | Place/Treasure/Photo 공통 상세 (title, description, thumbnail, 좌표) |

### 2.5 Photo Spot

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/photo-spots` | 포토 스팟 목록 (공개) |
| GET | `/api/v1/photo-spots/{spotId}` | 포토 스팟 상세 (officialPhotos, userPhotos) |
| POST | `/api/v1/photo-spots/{spotId}/submissions` | 포토 제출 (인증) |
| GET | `/api/v1/photo-spots/my-submissions` | 내 포토 제출 목록 (인증) |

### 2.6 관리자 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/admin/photo-submissions` | 검증 대기 목록 (PENDING) |
| PATCH | `/api/v1/admin/photo-submissions/{submissionId}` | 승인/거절 (`action: APPROVE | REJECT`) — 승인 시 mint_token 자동 생성·저장 |

---

## 3. 변경·추가 파일

### 3.1 엔티티

| 파일 | 설명 |
|------|------|
| `UserPhotoSubmission.java` | 포토 제출 (status, reject_reason, verified_at, is_public, mint_token) |
| `PhotoSubmissionStatus.java` | PENDING, APPROVED, REJECTED |
| `TourSpot` | title_kr, pronunciation_url, address 컬럼 추가 |

### 3.2 도메인 메서드

| 엔티티 | 메서드 | 용도 |
|--------|--------|------|
| `UserSpotProgress` | `unlock()` | 50m 근접 시 Place unlock |
| `UserTreasureStatus` | `unlock()` | 50m 근접 시 Treasure 발견 |
| `UserTreasureStatus` | `collect()` | Collect Treasure 클릭 시 수집 |

### 3.3 리포지토리

| 파일 | 설명 |
|------|------|
| `UserTreasureStatusRepository.java` | 신규 (findByTourRunIdAndTreasureSpotId, findByUserIdAndCollected 등) |
| `UserPhotoSubmissionRepository.java` | 신규 (findByStatus, findBySpot_IdAndStatus 등) |
| `UserSpotProgressRepository` | `findByUserIdAndUnlockedPlaces` 추가 |
| `TourRunRepository` | `findDistinctTourIdsByUserId` 추가 |
| `SpotAssetRepository` | `findFirstBySpot_IdAndUsageOrderBySortOrderAsc`, `findBySpot_IdOrderBySortOrderAsc` 추가 |

### 3.4 서비스

| 파일 | 설명 |
|------|------|
| `ProximityService.java` | Treasure/Photo 근접 처리, UserSpotProgress/UserTreasureStatus unlock 로직 추가 |
| `CollectionService.java` | 신규 (Place/Treasure 컬렉션, Collect Treasure) |
| `PhotoSpotService.java` | 신규 (포토 스팟 목록·상세, 제출, 내 제출 목록) |
| `AdminPhotoSubmissionService.java` | 신규 (검증 대기, 승인/거절 — 승인 시 MINT-{id}-{uuid8} 생성·저장) |
| `SpotGuideService.java` | `getSpotDetail()` 추가 (titleKr, pronunciationUrl, address 포함) |
| `AdminTourSpotService.java` | create/update 시 titleKr, pronunciationUrl, address 처리 |

### 3.5 컨트롤러

| 파일 | 설명 |
|------|------|
| `CollectionController.java` | 신규 (Place/Treasure 컬렉션 API) |
| `PhotoSpotController.java` | 신규 (포토 스팟 목록·상세, 제출 API) |
| `AdminPhotoSubmissionController.java` | 신규 (관리자 포토 검증) |
| `TourRunController.java` | `POST .../treasures/{spotId}/collect` 추가 |
| `SpotController.java` | `GET /spots/{spotId}` 추가 (상세) |

### 3.6 DTO

| 디렉터리 | 파일 |
|----------|------|
| `dto/collection/` | PlaceCollectionItemDto, PlaceCollectionResponse, PlaceCollectionSummaryResponse, TreasureCollectionItemDto, TreasureCollectionResponse, TreasureCollectionSummaryResponse |
| `dto/photo/` | PhotoSubmissionRequest, PhotoSubmissionResponse, PhotoSpotItemDto, PhotoSpotDetailResponse, MyPhotoSubmissionItemDto |
| `dto/tour/` | SpotDetailResponse |
| `dto/admin/` | PhotoSubmissionVerifyRequest |

---

## 4. DB 변경

- **user_photo_submissions** 테이블 추가 (`ddl-auto=update`로 자동 생성)
- **tour_spots**: title_kr, pronunciation_url, address 컬럼 추가
- 기존 `user_treasure_status` 테이블 스키마 변경 없음. (`user_spot_progress.lock_state`는 2026-02-16에 제거됨, CHANGELOG_2026-02-16 참조)

---

## 5. 문서 반영

| 문서 | 변경 내용 |
|------|----------|
| `API.md` | API_COLLECTIONS 내용 통합 (수집 API 섹션 5.x), Proximity 확장, Collect Treasure, 스팟 상세, photo-spots/{spotId}, TourSpot 확장 |
| `ERD.md` | user_photo_submissions, tour_spots(title_kr 등) 반영, 수집·포토 스팟 섹션 확정 |
| `API_COLLECTIONS.md` | 제거 (API.md로 통합됨) |

---

## 6. 프론트엔드 (관리자)

| 파일 | 변경 내용 |
|------|----------|
| `tour.ts` | SpotAdminResponse, SpotCreateRequest, SpotUpdateRequest에 titleKr, pronunciationUrl, address 추가 |
| `ToursPage.tsx` | Spot 폼에 한글 제목·발음 URL·주소 입력, 지도 클릭 시 역지오코딩으로 주소 자동 입력 |

---

## 7. 테스트

```bash
cd spring-boot/questofseoul && ./gradlew test
```

**결과:** BUILD SUCCESSFUL
