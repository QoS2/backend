-- Quest of Seoul Initial Data
-- 5개 퀘스트 데이터

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- QUEST 1: 경복궁 "왕의 하루"
-- 구조: 프롤로그 → 광화문 → 이동 거리 → 근정전 → 사정전 → 경회루 → 에필로그
-- ============================================
INSERT INTO quests (id, title, subtitle, theme, tone, difficulty, estimated_minutes, start_location, is_active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'A Day at the Palace',
    'Walk a Day in the King''s Shoes',
    'HISTORY',
    'EMOTIONAL',
    'EASY',
    70,
    ST_SetSRID(ST_MakePoint(126.9769, 37.5759), 4326),
    true,
    NOW()
);

-- Nodes
INSERT INTO quest_nodes (id, quest_id, node_type, title, order_index, geo, unlock_condition, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'REFLECTION', '오늘, 당신은 왕입니다', 0, NULL, NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440002'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'LOCATION', '광화문 - 시작의 문', 1, ST_SetSRID(ST_MakePoint(126.9769, 37.5759), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440001"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440003'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'WALK', '결정은 혼자 내려야 했다', 2, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440002"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440004'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'VIEW', '근정전 - 공식 업무의 공간', 3, ST_SetSRID(ST_MakePoint(126.9766, 37.5765), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440003"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440005'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'REFLECTION', '사정전 - 결정을 내리는 방', 4, ST_SetSRID(ST_MakePoint(126.9762, 37.5769), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440004"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440006'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'VIEW', '경회루 - 휴식, 그리고 외교', 5, ST_SetSRID(ST_MakePoint(126.9770, 37.5772), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440005"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440007'::uuid, '550e8400-e29b-41d4-a716-446655440000'::uuid, 'REFLECTION', '왕의 하루가 끝났습니다', 6, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440006"]}}'::jsonb, NOW());

-- Contents
INSERT INTO node_contents (id, node_id, content_order, content_type, language, body, audio_url, voice_style, display_mode, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440010'::uuid, '550e8400-e29b-41d4-a716-446655440001'::uuid, 1, 'TEXT', 'KO', '오늘 하루, 당신은 이 나라의 왕이 됩니다. 전쟁도, 정복도 아닌 하루를 살아보는 경험입니다.', NULL, 'soft_emotional', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440011'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 1, 'TEXT', 'KO', '이 문은 단순한 출입구가 아닙니다. 왕에게도, 백성에게도 하루가 시작되는 경계였습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440012'::uuid, '550e8400-e29b-41d4-a716-446655440003'::uuid, 1, 'TEXT', 'KO', '공식적인 자리는 끝났지만, 진짜 어려운 일은 사람들이 없는 곳에서 시작됐습니다.', NULL, 'reflective', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440013'::uuid, '550e8400-e29b-41d4-a716-446655440004'::uuid, 1, 'TEXT', 'KO', '이곳에서 왕은 혼자가 아니었습니다. 수많은 눈 앞에서 국가의 얼굴이 되어야 했죠.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440014'::uuid, '550e8400-e29b-41d4-a716-446655440005'::uuid, 1, 'TEXT', 'KO', '이곳은 회의실이 아니라 결단의 공간이었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440015'::uuid, '550e8400-e29b-41d4-a716-446655440006'::uuid, 1, 'TEXT', 'KO', '경회루는 쉬는 곳이었지만, 완전히 쉴 수 있는 곳은 아니었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440016'::uuid, '550e8400-e29b-41d4-a716-446655440007'::uuid, 1, 'TEXT', 'KO', '오늘 당신은 명령하지도, 정복하지도 않았습니다. 다만 하루를 선택하며 살아봤을 뿐입니다.', NULL, 'emotional', 'PARAGRAPH', NOW());

-- Actions
INSERT INTO node_actions (id, node_id, action_type, prompt, options, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440020'::uuid, '550e8400-e29b-41d4-a716-446655440001'::uuid, 'CHOICE', '이 이야기를 어떻게 경험하고 싶으신가요?', '{"options": [{"label": "짧고 쉽게", "value": "low"}, {"label": "조금 더 깊게", "value": "high"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440021'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, 'TEXT_INPUT', '광화문은 왕이 세상과 ( )을/를 만나는 장소였다.', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440022'::uuid, '550e8400-e29b-41d4-a716-446655440004'::uuid, 'TEXT_INPUT', '근정전은 왕이 개인이 아니라 ( )으로 행동해야 했던 공간이었다.', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440023'::uuid, '550e8400-e29b-41d4-a716-446655440005'::uuid, 'CHOICE', '당신이 왕이라면 더 중요하게 생각했을 것은 무엇일까요?', '{"options": [{"label": "안정", "value": "stability"}, {"label": "변화", "value": "change"}, {"label": "백성의 목소리", "value": "people"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440024'::uuid, '550e8400-e29b-41d4-a716-446655440006'::uuid, 'PHOTO', '왕의 시선에서 이 풍경을 바라보세요.', NULL, NOW());

-- Effects
INSERT INTO action_effects (id, action_id, effect_type, effect_value, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440030'::uuid, '550e8400-e29b-41d4-a716-446655440020'::uuid, 'MEMORY', '{"detail_level": "low"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440031'::uuid, '550e8400-e29b-41d4-a716-446655440020'::uuid, 'MEMORY', '{"detail_level": "high"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440032'::uuid, '550e8400-e29b-41d4-a716-446655440021'::uuid, 'MEMORY', '{"note": "중요한 건 단어보다 왕도 세상 속으로 나가야 했다는 사실입니다."}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440033'::uuid, '550e8400-e29b-41d4-a716-446655440022'::uuid, 'MEMORY', '{"note": "맞아요. 이곳에서는 감정보다 역할이 먼저였습니다."}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440034'::uuid, '550e8400-e29b-41d4-a716-446655440023'::uuid, 'MEMORY', '{"note": "많은 왕들이 바로 이 선택 앞에서 고민했습니다.", "decision": "{{choice}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440035'::uuid, '550e8400-e29b-41d4-a716-446655440024'::uuid, 'MEMORY', '{"note": "이 풍경은 권력이 아니라 책임의 무게를 상징했습니다."}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440036'::uuid, '550e8400-e29b-41d4-a716-446655440024'::uuid, 'PROGRESS', '{"unlock_node": "550e8400-e29b-41d4-a716-446655440007"}'::jsonb, NOW());

-- Transitions
INSERT INTO node_transitions (id, from_node_id, to_node_id, transition_order, message_type, text_content, audio_url, language, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440040'::uuid, '550e8400-e29b-41d4-a716-446655440002'::uuid, '550e8400-e29b-41d4-a716-446655440003'::uuid, 1, 'TEXT', '공식적인 자리는 끝났지만, 진짜 어려운 일은 사람들이 없는 곳에서 시작됐습니다.', NULL, 'KO', NOW());

-- ============================================
-- QUEST 2: 남산 타워
-- 구조: 프롤로그 → 남산은 왜 중요한 산 → 남산타워는 언제 왜 → 진짜 원래 목적 → 퀘스트 포인트 → 전환점 → 자물쇠 존 → 지금의 남산타워
-- ============================================
INSERT INTO quests (id, title, subtitle, theme, tone, difficulty, estimated_minutes, start_location, is_active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440100'::uuid,
    '왜 서울은 산 위에 탑을 세웠을까',
    'From Control to Symbol',
    'HISTORY',
    'SERIOUS',
    'NORMAL',
    60,
    ST_SetSRID(ST_MakePoint(126.9882, 37.5512), 4326),
    true,
    NOW()
);

-- Nodes
INSERT INTO quest_nodes (id, quest_id, node_type, title, order_index, geo, unlock_condition, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440101'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'REFLECTION', '남산에 오르며', 0, NULL, NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440102'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'VIEW', '남산은 왜 중요한 산이었을까', 1, ST_SetSRID(ST_MakePoint(126.9882, 37.5512), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440101"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440103'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'VIEW', '남산타워는 언제, 왜 지어졌을까', 2, ST_SetSRID(ST_MakePoint(126.9882, 37.5512), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440102"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440104'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'VIEW', '남산타워의 진짜 원래 목적', 3, ST_SetSRID(ST_MakePoint(126.9882, 37.5512), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440103"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440105'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'REFLECTION', '이게 타워처럼 생긴 이유', 4, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440104"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440106'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'VIEW', '언제 사람을 들이기 시작했을까', 5, ST_SetSRID(ST_MakePoint(126.9882, 37.5512), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440105"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440107'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'REFLECTION', '자물쇠 존은 어떻게 생겼을까', 6, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440106"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440108'::uuid, '550e8400-e29b-41d4-a716-446655440100'::uuid, 'REFLECTION', '지금의 남산타워가 의미하는 것', 7, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440107"]}}'::jsonb, NOW());

-- Contents
INSERT INTO node_contents (id, node_id, content_order, content_type, language, body, audio_url, voice_style, display_mode, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440110'::uuid, '550e8400-e29b-41d4-a716-446655440101'::uuid, 1, 'TEXT', 'KO', '지금 남산은 산책하고, 데이트하고, 사진 찍는 곳입니다. 하지만 이 산은 오랫동안 서울에서 가장 민감한 장소였습니다. 왜냐하면 서울 전체가 한눈에 보이는 곳이었기 때문입니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440111'::uuid, '550e8400-e29b-41d4-a716-446655440102'::uuid, 1, 'TEXT', 'KO', '남산은 쉬라고 있는 산이 아니라, 항상 위에서 아래를 보는 자리였습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440112'::uuid, '550e8400-e29b-41d4-a716-446655440103'::uuid, 1, 'TEXT', 'KO', '1960년대 말, TV와 라디오 보급이 폭발했지만 신호가 약했습니다. 방송국마다 안테나가 난립해 도시 미관과 효율이 엉망이었습니다. 그래서 안테나를 한 곳에, 가장 높은 곳에, 하나로 모으자는 아이디어가 나왔습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440113'::uuid, '550e8400-e29b-41d4-a716-446655440104'::uuid, 1, 'TEXT', 'KO', '남산타워는 서울 시민에게 잘 보이려고 만든 건물이 아닙니다. 서울 전체에 목소리와 화면을 뿌리기 위해 만든 시설이었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440114'::uuid, '550e8400-e29b-41d4-a716-446655440105'::uuid, 1, 'TEXT', 'KO', '이 건물은 왜 이렇게 단순하고, 왜 이렇게 높을까요?', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440115'::uuid, '550e8400-e29b-41d4-a716-446655440106'::uuid, 1, 'TEXT', 'KO', '1980-90년대, 방송 기술이 안정되고 도시가 성장하면서 서울은 이렇게 생각했습니다. 어차피 이 탑이 도시 한가운데 서 있다면, 이제는 보여줘도 되지 않을까?', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440116'::uuid, '550e8400-e29b-41d4-a716-446655440107'::uuid, 1, 'TEXT', 'KO', '공식 계획이 아니라 시민들이 자발적으로 시작했습니다. 사람들은 이 높은 곳에서 자기 이야기를 남기고 싶어 했습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440117'::uuid, '550e8400-e29b-41d4-a716-446655440108'::uuid, 1, 'TEXT', 'KO', '남산타워는 처음엔 서울을 통제하기 위해 세워졌고, 지금은 서울을 보여주기 위해 사용됩니다. 같은 구조물, 완전히 다른 의미. 이것이 서울이라는 도시가 변해온 방식입니다.', NULL, 'reflective', 'PARAGRAPH', NOW());

-- Actions
INSERT INTO node_actions (id, node_id, action_type, prompt, options, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440120'::uuid, '550e8400-e29b-41d4-a716-446655440105'::uuid, 'CHOICE', '이 건물은 왜 이렇게 단순하고, 왜 이렇게 높을까요?', '{"options": [{"label": "멋있어 보이려고", "value": "style"}, {"label": "상징적으로", "value": "symbol"}, {"label": "전파 때문에", "value": "signal"}, {"label": "그냥 예산이 없어서", "value": "budget"}]}'::jsonb, NOW());

-- Effects
INSERT INTO action_effects (id, action_id, effect_type, effect_value, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440130'::uuid, '550e8400-e29b-41d4-a716-446655440120'::uuid, 'MEMORY', '{"note": "전파는 장애물이 없을수록, 높을수록 멀리 갑니다. 그래서 남산타워는 디자인보다 물리 법칙이 먼저였습니다.", "answer": "{{choice}}"}'::jsonb, NOW());

-- ============================================
-- QUEST 3: 명동
-- 구조: 프롤로그 → 메인 스트리트 → 이동 중 서사 → 명동성당 → 쇼윈도 거리 → 명동 골목 → 에필로그
-- ============================================
INSERT INTO quests (id, title, subtitle, theme, tone, difficulty, estimated_minutes, start_location, is_active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440200'::uuid,
    'The European Dream in Seoul',
    'How Myeongdong Became Korea''s Window to the West',
    'ARCHITECTURE',
    'FRIENDLY',
    'EASY',
    75,
    ST_SetSRID(ST_MakePoint(126.9850, 37.5636), 4326),
    true,
    NOW()
);

-- Nodes
INSERT INTO quest_nodes (id, quest_id, node_type, title, order_index, geo, unlock_condition, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440201'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'REFLECTION', '명동은 처음부터 쇼핑 거리가 아니었다', 0, NULL, NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440202'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'LOCATION', '명동 메인 스트리트', 1, ST_SetSRID(ST_MakePoint(126.9850, 37.5636), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440201"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440203'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'WALK', '왜 명동이었을까', 2, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440202"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440204'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'VIEW', '명동성당', 3, ST_SetSRID(ST_MakePoint(126.9874, 37.5639), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440203"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440205'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'VIEW', '쇼윈도 거리', 4, ST_SetSRID(ST_MakePoint(126.9850, 37.5636), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440204"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440206'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'VIEW', '명동 골목', 5, ST_SetSRID(ST_MakePoint(126.9850, 37.5636), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440205"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440207'::uuid, '550e8400-e29b-41d4-a716-446655440200'::uuid, 'REFLECTION', '명동이 한국에 남긴 것', 6, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440206"]}}'::jsonb, NOW());

-- Contents
INSERT INTO node_contents (id, node_id, content_order, content_type, language, body, audio_url, voice_style, display_mode, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440210'::uuid, '550e8400-e29b-41d4-a716-446655440201'::uuid, 1, 'TEXT', 'KO', '지금 당신이 서 있는 이 거리는 처음부터 관광지를 목표로 만들어진 곳이 아닙니다. 19세기 말-20세기 초, 한국이 처음으로 바깥 세계와 마주하던 시기, 명동은 외국 문화를 시험해보던 도시 실험실이었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440211'::uuid, '550e8400-e29b-41d4-a716-446655440202'::uuid, 1, 'TEXT', 'KO', '이 길은 수백 년 동안 이어진 서울의 골목 구조가 아니라, 1900년대 초, 유럽식 상업 도시를 모델로 새롭게 설계된 공간입니다. 사람을 걷게 하고, 보게 하고, 사게 만드는 근대 도시의 문법이 처음 적용된 거리였죠.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440212'::uuid, '550e8400-e29b-41d4-a716-446655440203'::uuid, 1, 'TEXT', 'KO', '20세기 초, 서울에는 이미 사람이 많은 곳이 많았습니다. 하지만 명동은 궁궐 중심의 전통 공간과 시장 중심의 상업 공간 사이에 놓인 완충 지대였습니다. 그래서 외국 문화와 새로운 제도가 가장 먼저 들어올 수 있었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440213'::uuid, '550e8400-e29b-41d4-a716-446655440204'::uuid, 1, 'TEXT', 'KO', '1898년, 이 건물이 세워졌을 때 서울 사람들에게 이 모습은 완전히 새로운 풍경이었습니다. 명동성당은 종교 시설을 넘어 서구적 가치가 처음으로 형태를 가진 상징이었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440214'::uuid, '550e8400-e29b-41d4-a716-446655440205'::uuid, 1, 'TEXT', 'KO', '특히 1960-70년대, 명동은 한국에서 쇼핑이 경험이 되기 시작한 거의 첫 번째 공간이었습니다. 이곳에서 소비는 생존이 아니라 선택할 수 있다는 감각이었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440215'::uuid, '550e8400-e29b-41d4-a716-446655440206'::uuid, 1, 'TEXT', 'KO', '명동은 20세기 초에는 유럽을 따라가려 했습니다. 하지만 서울은 파리처럼 광장 중심의 도시도, 런던처럼 계급 중심의 도시도 아니었습니다. 이 도시는 사람의 밀도와 골목의 리듬으로 작동하는 곳이었죠.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440216'::uuid, '550e8400-e29b-41d4-a716-446655440207'::uuid, 1, 'TEXT', 'KO', '명동은 한 거리의 이야기가 아닙니다. 이곳은 한국이 세계를 처음 마주했을 때 어떻게 반응했는지를 보여주는 공간입니다.', NULL, 'reflective', 'PARAGRAPH', NOW());

-- Actions
INSERT INTO node_actions (id, node_id, action_type, prompt, options, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440220'::uuid, '550e8400-e29b-41d4-a716-446655440202'::uuid, 'PHOTO', '가장 직선적·개방적으로 느껴지는 장면을 촬영해보세요.', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440221'::uuid, '550e8400-e29b-41d4-a716-446655440202'::uuid, 'TEXT_INPUT', '명동의 길은 머무르기보다 ( )을/를 유도하는 구조다.', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440222'::uuid, '550e8400-e29b-41d4-a716-446655440204'::uuid, 'TEXT_INPUT', '명동성당은 신앙뿐 아니라 ( )을 상징하는 공간이 되었다.', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440223'::uuid, '550e8400-e29b-41d4-a716-446655440205'::uuid, 'CHOICE', '이 거리에서 가장 많이 보이는 것은 무엇인가요?', '{"options": [{"label": "사람", "value": "people"}, {"label": "언어", "value": "language"}, {"label": "브랜드", "value": "brand"}, {"label": "빛", "value": "light"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440224'::uuid, '550e8400-e29b-41d4-a716-446655440206'::uuid, 'PHOTO', '이 골목에서 유럽과 가장 다른 서울의 방식이 보이는 장면을 찍어보세요.', NULL, NOW());

-- Effects
INSERT INTO action_effects (id, action_id, effect_type, effect_value, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440230'::uuid, '550e8400-e29b-41d4-a716-446655440221'::uuid, 'MEMORY', '{"note": "명동의 도시 구조에 대해 생각했다."}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440231'::uuid, '550e8400-e29b-41d4-a716-446655440222'::uuid, 'MEMORY', '{"note": "명동성당의 상징성에 대해 생각했다."}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440232'::uuid, '550e8400-e29b-41d4-a716-446655440223'::uuid, 'MEMORY', '{"note": "명동은 물건보다 사람의 흐름이 만들어낸 공간입니다.", "observation": "{{choice}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440233'::uuid, '550e8400-e29b-41d4-a716-446655440224'::uuid, 'MEMORY', '{"note": "명동은 유럽처럼 넓어지는 것보다, 서울답게 촘촘해지는 것이 더 강력하다는 것을 보여줍니다."}'::jsonb, NOW());

-- ============================================
-- QUEST 4: 인사동 1919
-- 구조: 프롤로그 → 천도교 중앙대교당 → 우정총국·보성사·조계사 → 승동교회 → 탑골공원 → 에필로그
-- ============================================
INSERT INTO quests (id, title, subtitle, theme, tone, difficulty, estimated_minutes, start_location, is_active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440300'::uuid,
    '1919: 선택의 골목',
    'The Day Ordinary People Chose History',
    'HISTORY',
    'EMOTIONAL',
    'DEEP',
    90,
    ST_SetSRID(ST_MakePoint(126.9857, 37.5745), 4326),
    true,
    NOW()
);

-- Nodes
INSERT INTO quest_nodes (id, quest_id, node_type, title, order_index, geo, unlock_condition, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440301'::uuid, '550e8400-e29b-41d4-a716-446655440300'::uuid, 'REFLECTION', '이 거리는 평범했다', 0, NULL, NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440302'::uuid, '550e8400-e29b-41d4-a716-446655440300'::uuid, 'VIEW', '천도교 중앙대교당 - 말하지 않고 준비한 사람들', 1, ST_SetSRID(ST_MakePoint(126.9857, 37.5745), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440301"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440303'::uuid, '550e8400-e29b-41d4-a716-446655440300'::uuid, 'WALK', '우정총국·보성사·조계사 - 준비는 흔적을 남기지 않는다', 2, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440302"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440304'::uuid, '550e8400-e29b-41d4-a716-446655440300'::uuid, 'VIEW', '승동교회 - 누가 참여할 수 있었는가', 3, ST_SetSRID(ST_MakePoint(126.9870, 37.5740), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440303"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440305'::uuid, '550e8400-e29b-41d4-a716-446655440300'::uuid, 'VIEW', '탑골공원 - 처음 외친 사람들', 4, ST_SetSRID(ST_MakePoint(126.9890, 37.5700), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440304"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440306'::uuid, '550e8400-e29b-41d4-a716-446655440300'::uuid, 'REFLECTION', '지금의 나에게 남는 질문', 5, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440305"]}}'::jsonb, NOW());

-- Contents
INSERT INTO node_contents (id, node_id, content_order, content_type, language, body, audio_url, voice_style, display_mode, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440310'::uuid, '550e8400-e29b-41d4-a716-446655440301'::uuid, 1, 'TEXT', 'KO', '100년 전, 이 골목은 관광지도, 기념지도 아니었습니다. 사람들이 살고, 장사를 하고, 모임을 하던 아주 평범한 생활 공간이었습니다. 그리고 바로 그 점이, 이곳을 역사로 만들었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440311'::uuid, '550e8400-e29b-41d4-a716-446655440302'::uuid, 1, 'TEXT', 'KO', '이곳은 종교 시설이었지만, 동시에 가장 조심스러운 결정을 내리던 공간이었습니다. 독립은 외치는 순간보다, 말하지 않는 시간이 더 길었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440312'::uuid, '550e8400-e29b-41d4-a716-446655440303'::uuid, 1, 'TEXT', 'KO', '독립선언서는 공개적으로 만들어지지 않았습니다. 인쇄는 몰래, 전달은 조심스럽게, 장소는 분산되었습니다. 이 골목들이 선택된 이유는 숨길 수 있었기 때문입니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440313'::uuid, '550e8400-e29b-41d4-a716-446655440304'::uuid, 1, 'TEXT', 'KO', '이곳은 3·1운동이 특정 계층의 일이 아니었음을 보여주는 장소입니다. 백정 출신 장로, 그의 아들, 그리고 이름 없이 참여한 수많은 사람들. 독립은 신분이 아니라 결심으로 연결되었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440314'::uuid, '550e8400-e29b-41d4-a716-446655440305'::uuid, 1, 'TEXT', 'KO', '1919년 3월 1일, 이곳에서 처음 만세가 울려 퍼졌습니다. 그 순간, 돌아갈 수 있는 선택지는 사라졌습니다. 이 외침은 승리를 약속하지 않았습니다. 하지만 되돌릴 수 없는 기준이 되었습니다.', NULL, 'emotional', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440315'::uuid, '550e8400-e29b-41d4-a716-446655440306'::uuid, 1, 'TEXT', 'KO', '이 운동은 성공하지 못했습니다. 하지만 사람들의 선택은 사라지지 않았습니다. 오늘, 당신이 이 길을 걷는 것 역시 그 선택의 연장선입니다.', NULL, 'reflective', 'PARAGRAPH', NOW());

-- Actions
INSERT INTO node_actions (id, node_id, action_type, prompt, options, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440320'::uuid, '550e8400-e29b-41d4-a716-446655440302'::uuid, 'CHOICE', '당신이 이 시대에 있었다면 어떤 역할을 선택했을까요?', '{"options": [{"label": "계획자", "value": "planner"}, {"label": "전달자", "value": "messenger"}, {"label": "침묵하는 동조자", "value": "supporter"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440321'::uuid, '550e8400-e29b-41d4-a716-446655440303'::uuid, 'PHOTO', '이 중에서 가장 비밀스러워 보이는 장소를 촬영해보세요.', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440322'::uuid, '550e8400-e29b-41d4-a716-446655440304'::uuid, 'CHOICE', '당신은 이 운동에 어떤 이유로 참여했을 것 같나요?', '{"options": [{"label": "신념", "value": "belief"}, {"label": "연대", "value": "solidarity"}, {"label": "분노", "value": "anger"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440323'::uuid, '550e8400-e29b-41d4-a716-446655440305'::uuid, 'TEXT_INPUT', '당신이라면 이 순간, 외칠 수 있었을까요?', NULL, NOW());

-- Effects
INSERT INTO action_effects (id, action_id, effect_type, effect_value, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440330'::uuid, '550e8400-e29b-41d4-a716-446655440320'::uuid, 'MEMORY', '{"role": "{{choice}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440331'::uuid, '550e8400-e29b-41d4-a716-446655440321'::uuid, 'MEMORY', '{"note": "역사는 가장 눈에 띄지 않는 곳에서 준비됩니다."}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440332'::uuid, '550e8400-e29b-41d4-a716-446655440322'::uuid, 'MEMORY', '{"motivation": "{{choice}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440333'::uuid, '550e8400-e29b-41d4-a716-446655440323'::uuid, 'MEMORY', '{"reflection": "{{user_input}}"}'::jsonb, NOW());

-- ============================================
-- QUEST 5: 정동
-- 구조: 프롤로그 → 정관헌 → 석조전 → 돈덕전 → 구세군 중앙회관 → 정동제일교회 → 성공회 서울 주교좌 성당 → 에필로그
-- ============================================
INSERT INTO quests (id, title, subtitle, theme, tone, difficulty, estimated_minutes, start_location, is_active, created_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440400'::uuid,
    '서울이 처음으로 유럽을 시험한곳',
    'Where Seoul Quietly Learned the Language of the West',
    'ARCHITECTURE',
    'SERIOUS',
    'NORMAL',
    90,
    ST_SetSRID(ST_MakePoint(126.9700, 37.5650), 4326),
    true,
    NOW()
);

-- Nodes
INSERT INTO quest_nodes (id, quest_id, node_type, title, order_index, geo, unlock_condition, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440401'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'REFLECTION', '정동에 들어서며', 0, NULL, NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440402'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'VIEW', '정관헌 - 커피 한 잔으로 세계를 읽다', 1, ST_SetSRID(ST_MakePoint(126.9700, 37.5650), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440401"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440403'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'VIEW', '석조전 - 우리는 근대 국가입니다', 2, ST_SetSRID(ST_MakePoint(126.9740, 37.5655), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440402"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440404'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'VIEW', '돈덕전 - 환대의 공간, 그러나 상처의 장소', 3, ST_SetSRID(ST_MakePoint(126.9710, 37.5650), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440403"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440405'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'VIEW', '옛 구세군 중앙회관 - 서양식 구원이 들어오다', 4, ST_SetSRID(ST_MakePoint(126.9705, 37.5652), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440404"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440406'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'VIEW', '정동제일교회 - 사람을 바꾼 건축', 5, ST_SetSRID(ST_MakePoint(126.9708, 37.5653), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440405"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440407'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'VIEW', '성공회 서울 주교좌 성당 - 정동의 모든 이야기가 모인 곳', 6, ST_SetSRID(ST_MakePoint(126.9712, 37.5655), 4326), '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440406"]}}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440408'::uuid, '550e8400-e29b-41d4-a716-446655440400'::uuid, 'REFLECTION', '정동은 유럽이 되지 않았다', 7, NULL, '{"requires": {"completed_nodes": ["550e8400-e29b-41d4-a716-446655440407"]}}'::jsonb, NOW());

-- Contents
INSERT INTO node_contents (id, node_id, content_order, content_type, language, body, audio_url, voice_style, display_mode, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440410'::uuid, '550e8400-e29b-41d4-a716-446655440401'::uuid, 1, 'TEXT', 'KO', '정동은 조용한 동네로 알려져 있지만, 100년 전에는 서울에서 가장 국제적인 공간이었습니다. 왕, 외교관, 선교사, 군인, 학생들이 이 작은 동네에 동시에 모여 있었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440411'::uuid, '550e8400-e29b-41d4-a716-446655440402'::uuid, 1, 'TEXT', 'KO', '고종이 이곳에서 커피를 마셨습니다. 단순한 취향이 아니라 외국 외교관을 비공식적으로 만나는 장소였습니다. 조선 왕은 공식 회의실이 아니라 커피 테이블에서 세계 정세를 들었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440412'::uuid, '550e8400-e29b-41d4-a716-446655440403'::uuid, 1, 'TEXT', 'KO', '석조전은 우리는 문명국입니다라는 국가의 자기소개서였습니다. 하지만 동시에 인정받고 싶다는 불안도 담겨 있었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440413'::uuid, '550e8400-e29b-41d4-a716-446655440404'::uuid, 1, 'TEXT', 'KO', '이곳을 다녀간 외국 인사 중 대한제국을 진심으로 도운 사람은 거의 없었습니다. 고종이 외교적으로 가장 큰 배신감을 느낀 공간이었습니다. 환대는 힘이 없으면 협상이 아니라 노출이 됩니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440414'::uuid, '550e8400-e29b-41d4-a716-446655440405'::uuid, 1, 'TEXT', 'KO', '구세군 중앙회관은 한국 사회에 종교는 삶을 바꾼다라는 개념을 처음 들여온 공간이었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440415'::uuid, '550e8400-e29b-41d4-a716-446655440406'::uuid, 1, 'TEXT', 'KO', '정동제일교회는 예배당이 아니라 사람들이 새로운 생각을 배운 장소였습니다. 평등, 개인의 양심, 교육. 이후 독립운동·민주주의로 연결되었습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440416'::uuid, '550e8400-e29b-41d4-a716-446655440407'::uuid, 1, 'TEXT', 'KO', '이 건물은 서울 근현대사 100년을 한 번에 압축한 공간입니다. 대한제국, 일제강점기, 한국전쟁, 1987 민주화 운동, 영국 왕실 방문. 모든 것이 이곳에서 일어났습니다.', NULL, 'narrative', 'PARAGRAPH', NOW()),
('550e8400-e29b-41d4-a716-446655440417'::uuid, '550e8400-e29b-41d4-a716-446655440408'::uuid, 1, 'TEXT', 'KO', '정동은 유럽을 복사하지 않았습니다. 대신 받아들이고, 거부하고, 변형하며 서울만의 방식을 만들었습니다.', NULL, 'reflective', 'PARAGRAPH', NOW());

-- Actions
INSERT INTO node_actions (id, node_id, action_type, prompt, options, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440420'::uuid, '550e8400-e29b-41d4-a716-446655440402'::uuid, 'CHOICE', '당신에게 커피는 쉬는 시간인가요, 중요한 이야기가 시작되는 순간인가요?', '{"options": [{"label": "쉬는 시간", "value": "break"}, {"label": "이야기가 시작되는 순간", "value": "conversation"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440421'::uuid, '550e8400-e29b-41d4-a716-446655440403'::uuid, 'CHOICE', '이 건물에서 느껴지는 건 자신감일까요, 초조함일까요?', '{"options": [{"label": "자신감", "value": "confidence"}, {"label": "초조함", "value": "anxiety"}]}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440422'::uuid, '550e8400-e29b-41d4-a716-446655440404'::uuid, 'TEXT_INPUT', '친절함은 언제 약점이 될까요?', NULL, NOW()),
('550e8400-e29b-41d4-a716-446655440423'::uuid, '550e8400-e29b-41d4-a716-446655440407'::uuid, 'TEXT_INPUT', '이 건물이 아직 남아 있는 이유는 무엇일까요?', NULL, NOW());

-- Effects
INSERT INTO action_effects (id, action_id, effect_type, effect_value, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440430'::uuid, '550e8400-e29b-41d4-a716-446655440420'::uuid, 'MEMORY', '{"note": "전통적인 통치 방식이 이미 흔들리고 있었다는 것을 깨달았다.", "coffee_meaning": "{{choice}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440431'::uuid, '550e8400-e29b-41d4-a716-446655440421'::uuid, 'MEMORY', '{"feeling": "{{choice}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440432'::uuid, '550e8400-e29b-41d4-a716-446655440422'::uuid, 'MEMORY', '{"reflection": "{{user_input}}"}'::jsonb, NOW()),
('550e8400-e29b-41d4-a716-446655440433'::uuid, '550e8400-e29b-41d4-a716-446655440423'::uuid, 'MEMORY', '{"reflection": "{{user_input}}"}'::jsonb, NOW());
