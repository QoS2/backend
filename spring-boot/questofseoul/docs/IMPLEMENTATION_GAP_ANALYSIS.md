# 구현 검증 및 갭 분석 (ERD / 투어 디테일 API 기준)

작성일: 2026-02-16  
기준 문서: ERD 306ae6009a6680d59367ee5d4326e342.md, 투어 디테일 관련 api 306ae6009a668059bdc5f56e967ce36c.md

---

## 1. 현재 구현 vs 문서 비교 요약

| 항목 | 문서(ERD/API) | 현재 구현 | 상태 |
|------|--------------|----------|------|
| tours, tour_runs, user_tour_access | ✅ 정의됨 | ✅ 구현됨 | OK |
| user_spot_progress.progress_status | PENDING, ACTIVE, COMPLETED, SKIPPED | 동일 | OK |
| goodToKnow | 배열 `["...","..."]` | parseGoodToKnow()로 둘 다 지원 | ✅ OK |
| 투어 디테일 썸네일 | API.md 반영 | tour_assets 우선, 메인플레이스 fallback | ✅ OK |
| 메인 플레이스 썸네일 | mainPlaceThumbnails | spot_assets 기반 | ✅ OK |
| S3 폴더 구조 | category 파라미터 | images/{category}/, audio/{category}/ | ✅ OK |
| Mission 문제/보기/이미지 | MISSION_SCHEMA.md | options_json 스키마, GET /content-steps/{id}/mission | ✅ OK |
| lock_state | ERD: 제거 | progress_status로 통합, SpotLockState 삭제 | ✅ 완료 |
| tour_assets | 신규 | 테이블·엔티티·Admin API·TourDetailService | ✅ 완료 |
| spot_assets Admin | - | Admin API·UI (mainPlaceThumbnails용) | ✅ 완료 |

---

## 2. 상세 분석

### 2.1 user_spot_progress – progress_status 4종

문서: `progress_status: PENDING(아직 안열림), ACTIVE(열림), COMPLETED(완료), SKIPPED(미션 스킵)`

현재 `ProgressStatus` enum:
```java
PENDING, ACTIVE, COMPLETED, SKIPPED  // ✅ 문서와 일치
```

**결론:** 이미 4개 상태가 올바르게 적용됨. 추가 작업 불필요.

---

### 2.2 goodToKnow 스키마

**API 문서 예시:**
```json
"goodToKnow": ["주말엔 대기 줄이 길 수 있어요.", "편한 신발 추천!"]
```

**현재 TourDetailService:**
```java
Object tips = tour.getGoodToKnowJson().get("tips");
if (tips instanceof List) { ... }
```
→ `{"tips": ["a","b"]}` 형태만 지원. 루트 배열 `["a","b"]` 미지원.

**ERD:** `good_to_know_json JSONB default '[]'` → 배열 형태 권장.

**완료:** TourDetailService에 `parseGoodToKnow()` 추가. `{"tips": [...]}` 및 루트 배열 형식 지원.

---

### 2.3 투어 디테일 썸네일

**요구사항:**
- 투어 디테일 페이지: 썸네일 여러 개
- 메인 플레이스들의 썸네일

**ERD:** `tour_assets` 테이블 없음. `spot_assets`만 존재 (THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE 등).

**접근:**
1. **투어 레벨 썸네일:**  
   - ERD에 `tour_assets` 없음 → 메인 스팟 썸네일/히어로 이미지로 대체 또는  
   - 별도 `tour_assets` 테이블 추가(스키마 변경 필요)
2. **메인 플레이스 썸네일:**  
   - `spot_assets` (usage=THUMBNAIL, HERO_IMAGE) 조회 → TourDetailResponse에 `mainPlaceThumbnails` 추가

**완료:** TourDetailResponse에 `thumbnails`, `mainPlaceThumbnails` 추가. `tour_assets` 테이블 추가 및 우선 사용, 없으면 메인 플레이스 이미지 fallback.

---

### 2.4 S3 폴더 구조

**현재:** `images/`, `audio/` 만 사용. UUID 파일명.

**요구사항:**
- 이미지 / 오디오 / 썸네일 분리
- 카테고리별 폴더 (예: tour, spot, mission)
- 썸네일 전용 폴더

**권장 구조 (예시):**
```
{bucket}/
  images/
    tour/
    spot/
    mission/
  audio/
    intro/
    ambient/
```
(썸네일 폴더 등 추가 구조는 필요 시 프로젝트에서 자유롭게 정의)

**조치:** `FileUploadController`/`FileUploadService`에 `category` 파라미터 추가, 경로 `images/{category}/`, `audio/{category}/` 반영. 타입은 **image | audio** 만 지원.

---

### 2.5 Mission – 문제/보기/이미지

**ERD missions:**
- `prompt`: 문제 텍스트
- `options_json`: 보기 (QUIZ용)
- `answer_json`: 정답
- `meta_json`: 메타

**현재:** `Mission` 엔티티에 `options_json`, `answer_json`, `meta_json` (Map) 존재. 구조는 미정의.

**권장 options_json 예시 (QUIZ):**
```json
{
  "choices": [
    {"id": "a", "text": "보기1", "imageUrl": "https://..."},
    {"id": "b", "text": "보기2"}
  ],
  "questionImageUrl": "https://..."
}
```

**완료:** `MISSION_SCHEMA.md` 작성. `GET /api/v1/content-steps/{stepId}/mission` API 추가 (prompt, optionsJson 반환).

---

### 2.6 tours / tour_runs / user_tour_access

- `tours`: 마스터 데이터
- `tour_runs`: 유저별 1회 플레이 세션 (IN_PROGRESS/COMPLETED/ABANDONED)
- `user_tour_access`: 접근 권한 (LOCKED/UNLOCKED)

**현재 구현:** TourRunService의 START/CONTINUE/RESTART, TourAccessService.unlockTour 등 문서 정의와 일치.

---

## 3. 우선순위 작업 목록 (전체 완료)

| # | 작업 | 상태 |
|---|------|------|
| 1 | goodToKnow 배열 형식 지원 | ✅ 완료 |
| 2 | TourDetailResponse thumbnails, mainPlaceThumbnails | ✅ 완료 |
| 3 | S3 폴더 구조 확장 (category 파라미터) | ✅ 완료 |
| 4 | Mission options_json 스키마 (MISSION_SCHEMA.md, GET /content-steps/{id}/mission) | ✅ 완료 |
| 5 | lock_state 제거 (progress_status로 통합) | ✅ 완료 |
| 6 | tour_assets 테이블·Admin API | ✅ 완료 |

---

## 4. 미션 개발 진행 방향

- `Mission` 엔티티: 이미 `options_json`, `answer_json`, `meta_json` 보유
- `MissionService`: 제출/채점 로직 존재
- **완료:** 
  - QUIZ 보기(choices) + 이미지 URL 구조 → MISSION_SCHEMA.md
  - `UserSpotProgress.skip()` → progress_status = SKIPPED
  - `GET /content-steps/{stepId}/mission` → prompt, optionsJson 노출

---

## 5. 추가 완료 (선택 구현)

| # | 작업 | 상태 |
|---|------|------|
| 1 | Spot Assets Admin API/UI | ✅ 완료 |
| 2 | Mission Admin API/UI (MISSION 스텝 CRUD) | ✅ 완료 |
| 3 | Mission 이미지 업로드 UX (S3 category=mission, options_json URL 자동 삽입) | ✅ 완료 |
