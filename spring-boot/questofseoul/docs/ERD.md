# Quest of Seoul ERD (Entity Relationship Diagram)

## 개요

투어(Tour) 중심 아키텍처의 주요 엔티티와 관계를 설명합니다.

---

## 도메인 구분

1. **인증/사용자:** users, tags
2. **투어 코어:** tours, tour_spots, tour_tags, user_tour_access, tour_runs
3. **스팟 콘텐츠:** spot_content_steps, spot_script_lines, script_line_assets, missions, media_assets, spot_assets
4. **진행 상태:** user_spot_progress, user_treasure_status, user_mission_attempts
5. **채팅/AI:** chat_sessions, chat_turns, chat_turn_assets, ai_call_logs
6. **RAG:** tour_knowledge_embeddings (Pgvector, Spring Boot ↔ ai-server 공유)

---

## ER 다이어그램 (Mermaid)

```mermaid
erDiagram
    users ||--o{ user_tour_access : "has"
    users ||--o{ tour_runs : "starts"

    tours ||--o{ tour_spots : "has"
    tours ||--o{ tour_tags : "tagged"
    tours ||--o{ user_tour_access : "grants"
    tours ||--o{ tour_runs : "run"

    tour_spots ||--o{ spot_assets : "has"
    tour_spots ||--o{ spot_content_steps : "has"
    tour_spots ||--o{ user_spot_progress : "progress"
    tour_spots ||--o{ user_treasure_status : "treasure"
    tour_spots ||--o{ chat_sessions : "at"

    tags ||--o{ tour_tags : "tags"
    tour_tags }o--|| tours : "tags"

    tour_runs ||--o{ user_spot_progress : "tracks"
    tour_runs ||--o{ user_treasure_status : "tracks"
    tour_runs ||--o{ user_mission_attempts : "attempts"
    tour_runs ||--o{ chat_sessions : "has"

    spot_content_steps ||--o{ spot_script_lines : "has"
    spot_content_steps ||--o{ user_mission_attempts : "submits"
    missions ||--o{ spot_content_steps : "defines"
    missions ||--o{ user_mission_attempts : "attempts"

    spot_script_lines ||--o{ script_line_assets : "has"
    media_assets ||--o{ spot_assets : "referenced"
    media_assets ||--o{ script_line_assets : "referenced"
    media_assets ||--o{ chat_turn_assets : "referenced"

    chat_sessions ||--o{ chat_turns : "has"
    chat_turns ||--o{ chat_turn_assets : "has"
    chat_sessions ||--o{ ai_call_logs : "logs"

    tours ||--o{ tour_knowledge_embeddings : "embeds"

    users {
        uuid id PK
        string google_sub UK
        string email UK
        string nickname
        string language
        timestamp created_at
        timestamp updated_at
    }

    tags {
        long id PK
        string name UK
        string slug UK
        timestamp created_at
    }

    tours {
        long id PK
        string external_key UK
        string title
        string title_en
        text description
        text description_en
        jsonb info_json
        jsonb good_to_know_json
        long start_spot_id FK
        boolean is_published
        int version
        timestamp created_at
        timestamp updated_at
    }

    tour_spots {
        long id PK
        long tour_id FK
        string type
        long parent_spot_id FK
        string title
        string description
        double latitude
        double longitude
        int radius_m
        int order_index
        boolean ai_chat_enabled
        timestamp created_at
        timestamp updated_at
    }

    tour_tags {
        long id PK
        long tour_id FK
        long tag_id FK
        timestamp created_at
    }

    user_tour_access {
        long id PK
        uuid user_id FK
        long tour_id FK
        string status
        string method
        timestamp granted_at
        timestamp expires_at
        timestamp updated_at
    }

    tour_runs {
        long id PK
        uuid user_id FK
        long tour_id FK
        string status
        timestamp started_at
        timestamp ended_at
        timestamp created_at
    }

    media_assets {
        long id PK
        string asset_type
        text url
        string mime_type
        bigint bytes
        int width
        int height
        int duration_ms
        jsonb metadata_json
        timestamp created_at
    }

    spot_assets {
        long id PK
        long spot_id FK
        long asset_id FK
        string usage
        int sort_order
        string caption
        timestamp created_at
    }

    user_spot_progress {
        long id PK
        long tour_run_id FK
        long spot_id FK
        string lock_state
        string progress_status
        timestamp unlocked_at
        timestamp completed_at
        timestamp updated_at
    }

    user_treasure_status {
        long id PK
        long tour_run_id FK
        long treasure_spot_id FK
        string status
        timestamp unlocked_at
        timestamp got_at
    }

    missions {
        long id PK
        string mission_type
        text prompt
        jsonb options_json
        jsonb answer_json
        jsonb meta_json
        timestamp created_at
        timestamp updated_at
    }

    spot_content_steps {
        long id PK
        long spot_id FK
        string language
        int step_index
        string kind
        string title
        long mission_id FK
        string next_action
        boolean is_published
        timestamp created_at
        timestamp updated_at
    }

    spot_script_lines {
        long id PK
        long step_id FK
        int seq
        string role
        text text
        timestamp created_at
    }

    script_line_assets {
        long id PK
        long script_line_id FK
        long asset_id FK
        string usage
        int sort_order
        timestamp created_at
    }

    user_mission_attempts {
        long id PK
        long tour_run_id FK
        long step_id FK
        long mission_id FK
        int attempt_no
        string status
        jsonb answer_json
        jsonb submission_assets_json
        boolean is_correct
        int score
        text feedback
        timestamp started_at
        timestamp submitted_at
        timestamp graded_at
    }

    chat_sessions {
        long id PK
        long tour_run_id FK
        long spot_id FK
        string language
        boolean allow_user_question
        int cursor_step_index
        boolean is_active
        timestamp last_activity_at
        timestamp created_at
        timestamp updated_at
    }

    chat_turns {
        long id PK
        long session_id FK
        string source
        string role
        text text
        long step_id FK
        long script_line_id FK
        long mission_id FK
        jsonb action_json
        jsonb context_json
        timestamp created_at
    }

    chat_turn_assets {
        long id PK
        long turn_id FK
        long asset_id FK
        string usage
        int sort_order
        jsonb meta_json
        timestamp created_at
    }

    ai_call_logs {
        long id PK
        long session_id FK
        long user_turn_id FK
        long llm_turn_id FK
        string model
        jsonb request_json
        jsonb response_json
        int latency_ms
        int token_in
        int token_out
        string error_code
        timestamp created_at
    }

    tour_knowledge_embeddings {
        long id PK
        string source_type
        long source_id
        long tour_id
        long spot_id
        text content
        string title
        vector embedding
        timestamp created_at
        timestamp updated_at
    }
```

---

## 테이블 요약

| 테이블 | 설명 |
|--------|------|
| users | 사용자 (OAuth2/JWT 로그인) |
| tags | 태그 |
| tours | 투어 정의 (start_spot_id로 시작 스팟 연결) |
| tour_spots | 스팟 통합 (type: MAIN, SUB, PHOTO, TREASURE) |
| tour_tags | 투어-태그 N:M |
| user_tour_access | 사용자별 투어 접근 (LOCKED → UNLOCKED) |
| tour_runs | 사용자별 투어 실행 (IN_PROGRESS, COMPLETED, ABANDONED) |
| media_assets | 미디어 에셋 (이미지/오디오) |
| spot_assets | 스팟-미디어 매핑 |
| user_spot_progress | Run별 스팟 진행 상태 |
| user_treasure_status | Run별 보물 스팟 상태 |
| missions | 미션 정의 (QUIZ, INPUT, PHOTO_CHECK 등) |
| spot_content_steps | 스팟별 콘텐츠 스텝 (GUIDE, MISSION). next_action: NEXT \| MISSION_CHOICE |
| spot_script_lines | 가이드 스텝의 문장 |
| script_line_assets | 문장-미디어 매핑 |
| user_mission_attempts | Run별 미션 제출 이력 |
| chat_sessions | Run+스팟별 AI 채팅 세션 |
| chat_turns | 채팅 턴 (유저/AI 메시지). **action**(action_json): UI 동작(NEXT/MISSION_START/SKIP). **mission**(mission_id): 게임·미션 정의 참조 |
| chat_turn_assets | 채팅 턴 첨부 미디어 |
| ai_call_logs | AI 호출 로그 |
| tour_knowledge_embeddings | RAG용 투어·가이드 지식 임베딩 (Pgvector, source_type: TOUR/SPOT/GUIDE_LINE) |

### 수집·포토 스팟 (추가 예정)

| 테이블 | 설명 |
|--------|------|
| user_photo_submissions | 포토 스팟 유저 제출 (status: PENDING/APPROVED/REJECTED, mint_token, is_public) |

---

## 주요 Enum

- **SpotType:** MAIN, SUB, PHOTO, TREASURE
- **MarkerType:** STEP, WAYPOINT, PHOTO_SPOT, TREASURE
- **StepKind:** GUIDE, MISSION
- **StepNextAction:** NEXT, MISSION_CHOICE
- **Language:** ko, en, ja
- **TourAccessStatus:** LOCKED, UNLOCKED
- **TourRunStatus:** IN_PROGRESS, COMPLETED, ABANDONED
- **ChatRole:** USER, ASSISTANT
- **ChatSource:** USER, GUIDE, AI
- **MissionAttemptStatus:** STARTED, SUBMITTED, CORRECT, INCORRECT

### chat_turns 용어 정리 (action vs mission)

| 용어 | 필드 | 설명 |
|------|------|------|
| **action** | action_json | UI에서 사용자에게 제공하는 동작. `type`: `NEXT`(다음 컨텐츠), `MISSION_START`(게임 시작), `SKIP`(미션 스킵) |
| **mission** | mission_id | 게임/미션 정의 FK. action이 MISSION_START일 때 연결되는 missions 레코드 |
- **MissionType:** QUIZ, INPUT, PHOTO_CHECK
