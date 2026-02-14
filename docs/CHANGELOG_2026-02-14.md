# 작업 리포트 (2026-02-14)

## 개요

투어 디테일·마커·스팟 콘텐츠 관련 요구사항을 반영하여 API·ERD·관리자 UI를 개선했습니다.

---

## 1. GET /api/v1/spots/{spotId}/guide — 인증 필요로 변경

### 변경 사항

- **SecurityConfig**: `GET /api/v1/spots/*/guide`를 인증 필수(`authenticated`)로 변경
- **API.md**: 해당 엔드포인트 설명을 "인증 필요 (투어 진행 중 스크립트 제공 목적)"으로 수정

### 배경

- 가이드 API는 투어 진행 중 스크립트 제공에 사용되므로, 인증이 필요한 경로로 조정했습니다.
- `requestMatchers` 순서상 `/spots/*/guide`를 `/spots/**`보다 먼저 매칭하도록 설정했습니다.

### 영향

- 클라이언트는 가이드 조회 시 JWT Bearer 또는 세션 인증을 전달해야 합니다.
- 인증되지 않은 요청은 401을 반환합니다.

---

## 2. API 문서 보강

### 2.1 MarkerType ↔ SpotType 매핑

- **API.md** 3.3 마커 목록에 매핑 테이블 추가:

| MarkerType (API) | SpotType (DB) | 설명     |
|------------------|---------------|----------|
| STEP             | MAIN          | 핵심 장소 |
| WAYPOINT         | SUB           | 서브 장소 |
| PHOTO_SPOT       | PHOTO         | 포토 스팟 |
| TREASURE         | TREASURE      | 보물 찾기  |

### 2.2 tours.info_json / good_to_know_json 스키마

- 투어 디테일 응답 필드 설명에 `info_json`, `good_to_know_json` 예시 스키마 추가:

```json
// info_json
{
  "entrance_fee": { "adult": 3000, "child": 1500 },
  "available_hours": [{ "day": "weekday", "open": "09:00", "close": "18:00" }],
  "estimated_duration_min": 90
}

// good_to_know_json
{
  "tips": ["한복 입장 무료", "편한 신발 추천", "화요일 휴궁"]
}
```

---

## 3. chat_turns — action vs mission 용어 정리

### ERD.md 보강

- **chat_turns** 테이블 요약에 action / mission 구분 설명 추가
- **용어 정리 섹션** 추가:

| 용어   | 필드       | 설명                                                                 |
|--------|------------|----------------------------------------------------------------------|
| action | action_json| UI 동작. `type`: `NEXT`, `MISSION_START`, `SKIP`                    |
| mission| mission_id | 게임/미션 정의 FK. action이 MISSION_START일 때 연결되는 missions 레코드 |

---

## 4. spot_content_steps.next_action 추가

### 변경 사항

- **StepNextAction** enum: `NEXT`, `MISSION_CHOICE`
- **SpotContentStep**: `nextAction` 컬럼(선택) 추가
- **마이그레이션**: `scripts/migrations/V001__add_spot_content_steps_next_action.sql`
- **ERD.md**: `next_action` 컬럼 및 `StepNextAction` enum 반영

### 용도

- **NEXT**: 다음 컨텐츠로 이동 (NEXT 버튼)
- **MISSION_CHOICE**: 게임 스타트 / 스킵 선택 UI

---

## 5. TourDetailResponse.mainQuestPath 추가

### 변경 사항

- **TourDetailResponse**: `mainQuestPath` 필드 추가
  - `MainQuestPathItemDto`: `spotId`, `spotTitle`, `orderIndex`, `games`
  - `QuestGameDto`: `stepId`, `missionId`, `title`
- **TourDetailService**: MAIN 스팟별 MISSION 스텝을 조회해 `mainQuestPath` 구성

### 응답 예시

```json
"mainQuestPath": [
  {
    "spotId": 1,
    "spotTitle": "Gwanghwamun Gate",
    "orderIndex": 1,
    "games": [
      { "stepId": 101, "missionId": 1, "title": "Game 1" },
      { "stepId": 102, "missionId": 2, "title": "Game 2" }
    ]
  }
]
```

---

## 6. 관리자 가이드 UI — next_action 설정

### 변경 사항

- **GuideSaveRequest** / **GuideAdminResponse**: `nextAction` 필드 추가
- **AdminGuideService**: `nextAction` 저장·조회 처리
- **프론트 GuideEditor**: "컨텐츠 후 버튼" Select 추가
  - (선택 안 함) / NEXT / MISSION_CHOICE

---

## 7. 런타임 next_action 반영 (추가 완료)

### 7.1 GuideSegmentResponse

- `nextAction` 필드 추가
- **SpotGuideService**: 마지막 GUIDE 스텝의 `next_action` 값을 응답에 포함

### 7.2 ProximityService

- **resolveAction()**: `next_action` 기반 ActionDto 생성
  - `NEXT`: 다음 컨텐츠/세그먼트
  - `MISSION_CHOICE`: 게임 시작/스킵 (stepId는 MISSION 스텝 ID)
- 중간 턴: `NEXT` (다음 세그먼트)
- 마지막 턴: `next_action`이 `MISSION_CHOICE`면 `MISSION_CHOICE`, 아니면 `NEXT`
- **buildProximityResponse**: 기존 턴 재사용 시에도 마지막 턴에 올바른 action 적용

### 7.3 API 문서

- GuideSegmentResponse에 `nextAction` 예시 추가
- ProximityResponse `action.type` 규칙 문서화

---

## 8. 파일 변경 목록

| 경로 | 변경 유형 |
|------|----------|
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/config/SecurityConfig.java` | 수정 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/domain/enums/StepNextAction.java` | 신규 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/domain/entity/SpotContentStep.java` | 수정 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/dto/tour/TourDetailResponse.java` | 수정 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/dto/admin/GuideSaveRequest.java` | 수정 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/dto/admin/GuideAdminResponse.java` | 수정 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/service/TourDetailService.java` | 수정 |
| `spring-boot/questofseoul/src/main/java/com/app/questofseoul/service/admin/AdminGuideService.java` | 수정 |
| `spring-boot/questofseoul/scripts/migrations/V001__add_spot_content_steps_next_action.sql` | 신규 |
| `spring-boot/questofseoul/docs/API.md` | 수정 |
| `spring-boot/questofseoul/docs/ERD.md` | 수정 |
| `frontend/administration-page/src/api/tour.ts` | 수정 |
| `frontend/administration-page/src/pages/ToursPage.tsx` | 수정 |
| `spring-boot/.../dto/tour/GuideSegmentResponse.java` | 수정 (nextAction) |
| `spring-boot/.../service/SpotGuideService.java` | 수정 (nextAction 반환) |
| `spring-boot/.../service/ProximityService.java` | 수정 (resolveAction, buildProximityResponse) |

---

## 9. 검증

- Spring Boot: `./gradlew test` 통과
- Hibernate `ddl-auto=update` 사용 시 `next_action` 컬럼 자동 추가
- 수동 마이그레이션: `V001__add_spot_content_steps_next_action.sql` 실행 가능

---

## 10. 후속 검토 사항

1. **GET /spots/{id}/guide**: 현재 미리보기용으로만 사용하는지 재확인 후, 공개용 경로가 필요하면 별도 엔드포인트 검토
2. **관리자 MISSION 스텝 관리**: 현재 GUIDE 스텝만 관리. MISSION 스텝 CRUD/순서 관리 UI 확장 검토
3. **mainQuestPath 다국어**: 현재 `"ko"` 고정. `lang` 파라미터 기반 다국어 지원 검토
