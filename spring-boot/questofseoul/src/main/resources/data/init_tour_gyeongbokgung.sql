-- Gyeongbokgung Tour Initial Data
-- Load with data.sql (after schema creation)
-- Separated from init_quest_data.sql

INSERT INTO tours (external_key, title_en, description_en, info_json, good_to_know_json, version, updated_at)
SELECT 'gyeongbokgung', '경복궁 투어',
  '경복궁은 조선 시대의 법궁으로, 광화문, 근정전, 경회루 등 역사적 명소를 탐방합니다.',
  '{"admissionFee": "3000원", "operatingHours": "09:00~18:00"}'::jsonb,
  '{"tips": ["편한 신발을 권합니다", "충분한 시간을 두고 관람하세요"]}'::jsonb,
  1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tours WHERE external_key = 'gyeongbokgung');

INSERT INTO steps (external_key, tour_id, step_order, title_en, short_desc_en, latitude, longitude, radius_m, version, updated_at)
SELECT 'gwanghwamun', t.id, 1, '광화문', '경복궁의 정문', 37.5720, 126.9769, 50, 1, NOW()
FROM tours t WHERE t.external_key = 'gyeongbokgung'
AND NOT EXISTS (SELECT 1 FROM steps WHERE external_key = 'gwanghwamun');

INSERT INTO steps (external_key, tour_id, step_order, title_en, short_desc_en, latitude, longitude, radius_m, version, updated_at)
SELECT 'geunjeongmun', t.id, 2, '근정문', '근정전으로 통하는 문', 37.5795, 126.9768, 50, 1, NOW()
FROM tours t WHERE t.external_key = 'gyeongbokgung'
AND NOT EXISTS (SELECT 1 FROM steps WHERE external_key = 'geunjeongmun');

INSERT INTO steps (external_key, tour_id, step_order, title_en, short_desc_en, latitude, longitude, radius_m, version, updated_at)
SELECT 'geunjeongjeon', t.id, 3, '근정전', '조선의 정전', 37.5798, 126.9769, 50, 1, NOW()
FROM tours t WHERE t.external_key = 'gyeongbokgung'
AND NOT EXISTS (SELECT 1 FROM steps WHERE external_key = 'geunjeongjeon');

-- ChatContent + ChatMessage (광화문 스텝 50m 진입 시 준비된 대사)
INSERT INTO chat_contents (ref_type, ref_id, language, version, updated_at)
SELECT 'STEP', s.id, 'KO', 1, NOW()
FROM steps s JOIN tours t ON s.tour_id = t.id
WHERE t.external_key = 'gyeongbokgung' AND s.external_key = 'gwanghwamun'
AND NOT EXISTS (SELECT 1 FROM chat_contents cc WHERE cc.ref_type = 'STEP' AND cc.ref_id = s.id AND cc.language = 'KO');

INSERT INTO chat_messages (chat_content_id, msg_idx, role, text_en, action_json, updated_at)
SELECT cc.id, 1, 'GUIDE', '광화문에 오신 것을 환영합니다! 이곳은 경복궁의 정문으로, 조선시대 왕과 신하들이 공식 행차를 하던 곳입니다.',
  ('{"type": "OPEN_STEP_PAGE", "label": "자세히 보기", "step_id": ' || (SELECT id FROM steps WHERE external_key = 'gwanghwamun' LIMIT 1) || '}')::jsonb,
  NOW()
FROM chat_contents cc
JOIN steps s ON cc.ref_id = s.id AND cc.ref_type = 'STEP'
WHERE s.external_key = 'gwanghwamun' AND cc.language = 'KO'
AND NOT EXISTS (SELECT 1 FROM chat_messages WHERE chat_content_id = cc.id);

-- GuideContent + GuideSegment (광화문 스텝 페이지 가이드)
INSERT INTO guide_contents (external_key, step_id, version, updated_at)
SELECT 'guide-gwanghwamun', s.id, 1, NOW()
FROM steps s JOIN tours t ON s.tour_id = t.id
WHERE t.external_key = 'gyeongbokgung' AND s.external_key = 'gwanghwamun'
AND NOT EXISTS (SELECT 1 FROM guide_contents gc WHERE gc.step_id = s.id);

INSERT INTO guide_segments (guide_content_id, seg_idx, text_en, trigger_key, updated_at)
SELECT gc.id, 1, '오른쪽에는 3개의 문이 있어요. 오른쪽은 신하문, 중간은 왕이 이동할 때 사용한 어도(御道)입니다. 가운데 문은 왕만 사용할 수 있었죠.', NULL, NOW()
FROM guide_contents gc JOIN steps s ON gc.step_id = s.id
WHERE s.external_key = 'gwanghwamun'
AND NOT EXISTS (SELECT 1 FROM guide_segments WHERE guide_content_id = gc.id);
