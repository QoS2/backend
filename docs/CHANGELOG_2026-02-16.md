# 작업 리포트 (2026-02-16)

## 개요

ERD/투어 디테일 API 기준 구현 검증 및 갭 분석을 반영하여 lock_state 제거, tour_assets 추가, goodToKnow·썸네일·S3·미션 관련 개선을 적용했습니다.

---

## 1. user_spot_progress – lock_state 제거

### 변경 사항

- **UserSpotProgress** 엔티티에서 `lock_state` 컬럼 제거
- **SpotLockState** enum 삭제
- **progress_status**로 통합: PENDING(아직 안열림), ACTIVE(열림), COMPLETED(완료), SKIPPED(미션 스킵)
- **UserSpotProgressRepository**: `findByUserIdAndUnlockedPlaces`를 `progress_status <> PENDING` 조건으로 변경
- **UserSpotProgress**: `complete()`, `skip()` 메서드 추가
- **마이그레이션**: `scripts/migrations/V002__remove_lock_state_add_tour_assets.sql` (lock_state DROP)

### 배경

ERD 문서에 "lock_state는 progress_status와 중복이므로 제거 가능"이라고 명시됨.

---

## 2. tour_assets 테이블 및 투어 레벨 썸네일

### 변경 사항

- **tour_assets** 테이블 추가 (tour_id, asset_id, usage, sort_order, caption)
- **TourAsset** 엔티티, **TourAssetUsage** enum (THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE)
- **TourAssetRepository**
- **AdminTourAssetController** / **AdminTourAssetService**: GET/POST/DELETE
- **TourDetailService**: 투어 썸네일을 tour_assets 우선, 없으면 메인 플레이스 이미지 fallback
- **TourDetailResponse**: `thumbnails`, `mainPlaceThumbnails` 필드 추가

### Admin API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/admin/tours/{tourId}/assets` | 에셋 목록 |
| POST | `/api/v1/admin/tours/{tourId}/assets` | 에셋 추가 (url, usage, sortOrder, caption) |
| DELETE | `/api/v1/admin/tours/{tourId}/assets/{tourAssetId}` | 에셋 삭제 |

### Admin UI (Frontend)

관리자 Tours 페이지에서 각 투어 행의 **이미지 아이콘** 버튼으로 Tour Assets Drawer를 열어 썸네일/히어로/갤러리 이미지를 추가·삭제할 수 있습니다.

---

## 3. goodToKnow 스키마 개선

- **TourDetailService**: `parseGoodToKnow()` 추가
- `{"tips": ["a","b"]}` 및 루트 배열/숫자 키 형식 모두 지원

---

## 4. S3 업로드 – category 파라미터

- **FileUploadController** / **FileUploadService**: `category` 쿼리 파라미터 추가
- 경로: `images/{category}/`, `audio/{category}/` (기본 category: general)
- 예: `?type=image&category=tour` → `images/tour/uuid.jpg`

---

## 5. 미션 – options_json 및 API

- **MISSION_SCHEMA.md**: options_json, answer_json 권장 구조 문서화
- **GET /api/v1/content-steps/{stepId}/mission**: prompt, optionsJson 반환 API 추가
- **MissionStepService**, **MissionStepDetailResponse**

### Mission Admin API 및 UI

- **AdminMissionStepController** / **AdminMissionStepService**: MISSION 스텝 CRUD
- **Mission** 엔티티: `setPrompt`, `setOptionsJson`, `setAnswerJson` 등 setter 추가
- **SpotContentStep**: `setMission` 추가
- **DTO**: `MissionStepCreateRequest`, `MissionStepUpdateRequest`, `MissionStepResponse`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps` | MISSION 스텝 목록 |
| POST | `/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps` | MISSION 스텝 추가 |
| PATCH | `/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps/{stepId}` | MISSION 스텝 수정 |
| DELETE | `/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps/{stepId}` | MISSION 스텝 삭제 |

- **관리자 UI**: SpotsDrawer 내 Spot별 "미션" 버튼 → MissionEditor (미션 목록, 추가/수정/삭제)
- **options_json**, **answer_json**: JSON 텍스트로 편집 (MISSION_SCHEMA.md 구조 준수)

---

## 6. Spot Assets Admin

- **AdminSpotAssetController** / **AdminSpotAssetService**: 스팟별 에셋 CRUD
- **SpotAsset**: `create(spot, asset, usage, sortOrder)`, `setCaption` 추가
- **API**: `GET/POST/DELETE /api/v1/admin/tours/{tourId}/spots/{spotId}/assets`
- **관리자 UI**: SpotsDrawer 내 Spot별 이미지 아이콘 → SpotAssetsDrawer (썸네일/히어로/갤러리)
- **mainPlaceThumbnails**: spot_assets 기반으로 표시

---

## 7. 포토 제출 검토 UI

- **PhotoSubmissionsPage**: PENDING 제출 목록, 승인/거절 (거절 시 사유 입력 모달)
- 사이드바 "포토 검토" 메뉴, `/photo-submissions` 라우트

---

## 8. Mission 이미지 업로드 UX

- Mission 편집 폼에 "문제 이미지 업로드", "보기 N 이미지" 버튼 추가
- S3 업로드 (`category=mission`) 후 options_json에 URL 자동 삽입
  - `questionImageUrl`: 문제 이미지
  - `choices[i].imageUrl`: QUIZ 보기별 이미지

---

## 9. 문서 업데이트

- **ERD.md**: lock_state 제거, tour_assets 추가, ProgressStatus·TourAssetUsage enum (이미 반영됨)
- **API.md**: Spot Assets 4.5, Mission Steps 4.6, enum spotAssetUsage 추가
- **IMPLEMENTATION_GAP_ANALYSIS.md**: 섹션 5 "추가 완료" (Spot Assets Admin, Mission Admin, Mission 이미지 업로드 UX)

---

## 10. 마이그레이션

`scripts/migrations/V002__remove_lock_state_add_tour_assets.sql` 실행 시:

- `user_spot_progress.lock_state` 컬럼 삭제
- `tour_assets` 테이블 생성

*ddl-auto=update* 사용 시 Hibernate가 lock_state 삭제를 자동으로 하지 않을 수 있으므로, 수동 마이그레이션 실행을 권장합니다.
