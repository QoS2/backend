# Quest of Seoul ERD (Entity Relationship Diagram)

## 개요

이 문서는 Quest of Seoul 백엔드의 주요 엔티티와 관계를 설명합니다.

---

## 도메인 구분

1. **인증/사용자**: User
2. **퀘스트 시스템**: Quest, QuestNode, NodeContent, NodeAction, ActionEffect, NodeTransition, UserQuestState, UserQuestHistory
3. **투어 시스템**: Tour, Step, Waypoint, TourRun, ChatSession, ChatTurn, GuideContent, GuideSegment, Quiz, PhotoSpot, Treasure, MediaAsset, Tag, TourTag, UserTreasure, SegmentMediaMap

---

## ER 다이어그램 (Mermaid)

```mermaid
erDiagram
    users ||--o{ user_quest_state : "has"
    users ||--o{ user_quest_history : "has"
    users ||--o{ tour_runs : "starts"
    users ||--o{ user_treasures : "claims"

    quests ||--o{ quest_nodes : "contains"
    quests ||--o{ user_quest_state : "tracks"
    quests ||--o{ user_quest_history : "records"

    quest_nodes ||--o{ node_contents : "has"
    quest_nodes ||--o{ node_actions : "has"
    quest_nodes ||--o{ node_transitions : "from"
    quest_nodes ||--o{ node_transitions : "to"
    quest_nodes ||--o{ user_quest_state : "current"
    quest_nodes ||--o{ user_quest_history : "at"

    node_actions ||--o{ action_effects : "has"
    node_actions ||--o{ user_quest_history : "records"

    tours ||--o{ steps : "has"
    tours ||--o{ waypoints : "has"
    tours ||--o{ photo_spots : "has"
    tours ||--o{ treasures : "has"
    tours ||--o{ tour_tags : "tagged"
    tours ||--o{ tour_runs : "run"

    tour_runs ||--o{ chat_sessions : "has"

    steps ||--o{ waypoints : "contains"
    steps ||--o{ treasures : "at"
    steps ||--o{ photo_spots : "at"
    steps ||--o{ guide_contents : "has"
    steps ||--o{ quizzes : "has"

    chat_sessions ||--o{ chat_turns : "has"
    chat_sessions }o--|| tour_runs : "belongs"
    chat_sessions }o--|| users : "belongs"

    guide_contents ||--o{ guide_segments : "has"
    guide_segments ||--o{ segment_media_map : "maps"
    segment_media_map }o--|| media_assets : "references"
    waypoints }o--o| media_assets : "references"

    tags ||--o{ tour_tags : "tags"
    tour_tags }o--|| tours : "tags"

    treasures ||--o{ user_treasures : "claimed_by"

    users {
        uuid id PK
        string google_sub UK
        string email UK
        string password_hash
        string nickname
        string language
        timestamp created_at
        timestamp updated_at
    }

    quests {
        uuid id PK
        string title
        string subtitle
        enum theme
        enum tone
        enum difficulty
        int estimated_minutes
        geography start_location
        boolean is_active
        timestamp created_at
    }

    quest_nodes {
        uuid id PK
        uuid quest_id FK
        enum node_type
        string title
        int order_index
        geography geo
        jsonb unlock_condition
        timestamp created_at
    }

    node_contents {
        uuid id PK
        uuid node_id FK
        int content_order
        enum content_type
        enum language
        text body
        string audio_url
        enum display_mode
        timestamp created_at
    }

    node_actions {
        uuid id PK
        uuid node_id FK
        enum action_type
        text prompt
        jsonb options
        timestamp created_at
    }

    action_effects {
        uuid id PK
        uuid action_id FK
        enum effect_type
        jsonb effect_value
        timestamp created_at
    }

    node_transitions {
        uuid id PK
        uuid from_node_id FK
        uuid to_node_id FK
        int transition_order
        enum message_type
        text text_content
        string audio_url
        enum language
        timestamp created_at
    }

    user_quest_state {
        uuid id PK
        uuid user_id FK
        uuid quest_id FK
        uuid current_node_id FK
        int current_content_order
        jsonb state
        enum status
        timestamp started_at
        timestamp completed_at
    }

    user_quest_history {
        uuid id PK
        uuid user_id FK
        uuid quest_id FK
        uuid node_id FK
        uuid action_id FK
        enum action_type
        text user_input
        string photo_url
        jsonb selected_option
        timestamp created_at
    }

    tours {
        long id PK
        string external_key UK
        string title_en
        text description_en
        jsonb info_json
        jsonb good_to_know_json
        int version
        timestamp updated_at
    }

    steps {
        long id PK
        string external_key UK
        long tour_id FK
        int step_order
        string title_en
        text short_desc_en
        decimal latitude
        decimal longitude
        int radius_m
        int version
        timestamp updated_at
    }

    waypoints {
        long id PK
        string external_key UK
        long tour_id FK
        long step_id FK
        string title_en
        text message_en
        decimal latitude
        decimal longitude
        int radius_m
        long media_asset_id FK
        int version
        timestamp updated_at
    }

    tour_runs {
        long id PK
        uuid user_id FK
        long tour_id FK
        enum status
        timestamp started_at
        timestamp ended_at
    }

    chat_sessions {
        long id PK
        uuid user_id FK
        long tour_run_id FK
        enum session_kind
        enum context_ref_type
        long context_ref_id
        string status
        timestamp created_at
        timestamp last_active_at
    }

    chat_turns {
        long id PK
        long session_id FK
        int turn_idx
        enum role
        enum source
        text text
        jsonb action_json
        jsonb meta_json
        string turn_key
        timestamp created_at
    }

    guide_contents {
        long id PK
        string external_key UK
        long step_id FK UK
        int version
        timestamp updated_at
    }

    guide_segments {
        long id PK
        long guide_content_id FK
        int seg_idx
        text text_en
        string trigger_key
        timestamp updated_at
    }

    segment_media_map {
        long id PK
        long guide_segment_id FK
        long media_asset_id FK
        int sort_order
        jsonb rule_json
        timestamp updated_at
    }

    media_assets {
        long id PK
        string external_key UK
        enum type
        string url_or_key
        jsonb meta_json
        timestamp updated_at
    }

    quizzes {
        long id PK
        string external_key UK
        long step_id FK
        enum type
        text prompt_en
        jsonb spec_json
        string answer_key_hash
        text hint_en
        int mint_reward
        int version
        timestamp updated_at
    }

    photo_spots {
        long id PK
        string external_key UK
        long tour_id FK
        long step_id FK
        string title_en
        text desc_en
        decimal latitude
        decimal longitude
        int radius_m
        int mint_reward
        int version
        timestamp updated_at
    }

    treasures {
        long id PK
        string external_key UK
        long tour_id FK
        long step_id FK
        string title_en
        text desc_en
        decimal latitude
        decimal longitude
        int radius_m
        int mint_reward
        int version
        timestamp updated_at
    }

    tags {
        long id PK
        string name UK
        string slug UK
        timestamp created_at
    }

    tour_tags {
        long id PK
        long tour_id FK
        long tag_id FK
        timestamp created_at
    }

    user_treasures {
        long id PK
        uuid user_id FK
        long treasure_id FK
        timestamp claimed_at
    }
```

---

## 엔티티 요약

| 테이블 | 설명 |
|--------|------|
| users | 사용자 (OAuth2/JWT 로그인) |
| quests | 퀘스트 정의 |
| quest_nodes | 퀘스트 내 노드 (순차적) |
| node_contents | 노드별 콘텐츠 (텍스트/오디오 등) |
| node_actions | 노드별 액션 (선택/사진/입력 등) |
| action_effects | 액션 결과 (노드 전환 등) |
| node_transitions | 노드 간 이동 메시지 |
| user_quest_state | 사용자별 퀘스트 진행 상태 |
| user_quest_history | 사용자별 퀘스트 액션 이력 |
| tours | 투어 정의 |
| steps | 투어 스텝(Place) |
| waypoints | 경로상 경유지(Sub Place) |
| tour_runs | 사용자별 투어 실행 |
| chat_sessions | 투어 중 AI 채팅 세션 |
| chat_turns | 채팅 턴(유저/AI 메시지) |
| guide_contents | 스텝별 가이드 |
| guide_segments | 가이드 세그먼트 |
| segment_media_map | 세그먼트-미디어 매핑 |
| media_assets | 미디어 에셋(이미지/오디오 등) |
| quizzes | 스텝별 퀴즈 |
| photo_spots | 포토 스팟 |
| treasures | 보물(수집 아이템) |
| tags | 태그 |
| tour_tags | 투어-태그 매핑 |
| user_treasures | 사용자 보물 획득 내역 |

---

## 주요 Enum

- **Quest**: QuestTheme, QuestTone, Difficulty
- **QuestNode**: NodeType
- **NodeContent**: ContentType, DisplayMode, Language
- **NodeAction**: ActionType
- **ActionEffect**: EffectType
- **NodeTransition**: TransitionMessageType
- **UserQuestState**: QuestStatus
- **UserQuestHistory**: ActionType
- **ChatSession**: SessionKind, ChatRefType
- **ChatTurn**: ChatRole, ChatSource
- **MediaAsset**: MediaAssetType
- **Quiz**: QuizType
- **TourRun**: TourRunStatus
