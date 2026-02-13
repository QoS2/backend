-- 경복궁 투어 init 데이터
-- 실행: psql -h localhost -U postgres -d questofseoul -f src/main/resources/db/data.sql
-- 또는: cd spring-boot/questofseoul && psql $DATABASE_URL -f src/main/resources/db/data.sql

INSERT INTO tours (external_key, title, title_en, description, description_en, info_json, good_to_know_json, is_published, version, created_at, updated_at)
VALUES ('gyeongbokgung', '경복궁 투어', 'Gyeongbokgung Palace Tour',
       '조선 왕조의 정궁인 경복궁을 둘러보는 투어입니다. 광화문·해태상부터 근정전, 경회루, 자경전·십장생굴뚝까지 왕의 동선을 따라갑니다.',
       'Explore the main royal palace of the Joseon Dynasty.',
       '{"durationMinutes":90,"distanceKm":1.5,"difficulty":"easy"}'::jsonb,
       '{"openingHours":"09:00~18:00 (계절별 변동)","closed":"매주 화요일","admission":"성인 3,000원 (한복 착용 시 무료)"}'::jsonb,
       true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- tour_spots (Guide List MAIN+SUB)
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'MAIN', '광화문', '자, 여기가 경복궁의 정문 광화문이에요. 먼저 정면을 보면 문이 가운데+양옆, 이렇게 세 칸 구조로 나뉘어 보이죠?', 37.576044, 126.977019, 60, 0, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'MAIN', '흥례문·영제교 라인', '이제 이 구간은 궁의 중심으로 들어가는 진입축이에요. 걸어가면서 바닥을 보면 길이 몇 갈래로 나뉘는지가 눈에 들어올 거예요.', 37.576940, 126.976940, 70, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'SUB', '흥례문 안 어도(가운데 길)', '여기서 가운데 길을 흔히 어도라고 부르죠. 가장 격식 있는 중심 동선으로 설명되는 구간이에요.', 37.576940, 126.976940, 35, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'SUB', '영제교(금천교)·돌짐승', '자, 지금 보이는 다리가 영제교(금천교)예요. 다리 근처를 잘 보면 다리 아래나 가장자리 쪽에 돌짐승 조각들이 숨어 있어요.', 37.577334, 126.976942, 35, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'SUB', '근정문', '여기가 근정문, 근정전 앞의 중요한 문턱이에요. 정면에서 보면 여기 역시 가운데+양옆의 구조가 또렷해요.', 37.577727, 126.976944, 40, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'MAIN', '근정전', '드디어 경복궁의 중심, 근정전이에요. 먼저 마당을 보세요. 좌우로 줄지어 있는 돌 표지들이 보이죠? 이게 바로 품계석과 연결되는 장치예요.', 37.578610, 126.976940, 80, 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'SUB', '근정전 월대 답도(조각 길)', '계단 가운데에 조각이 새겨진 그 경사면, 이걸 답도라고 불러요. 말 그대로 가운데 길의 상징이죠.', 37.578610, 126.976940, 35, 6, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'MAIN', '사정전', '근정전이 공식 행사 무대라면, 사정전은 실제로 일을 보던 공간에 가깝다고 느끼면 좋아요. 여기서는 건물 자체보다도, 아래 받쳐주는 석대(돌 단)를 한번 봐주세요.', 37.579170, 126.976940, 70, 7, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'MAIN', '경회루', '자, 여기 경회루는 경복궁에서 가장 유명한 장면 중 하나죠. 섬으로 들어가는 돌다리가 몇 개 보이나요? 그리고 다리 폭이 다 똑같아 보이나요?', 37.579750, 126.975889, 90, 8, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'SUB', '경회루 왕의 다리(남쪽·폭 넓은 다리)', '경회루로 이어지는 돌다리들이 보이죠. 여기서 포인트는 가장 넓어 보이는 다리가 어느 쪽인지 눈으로 고르는 거예요.', 37.579750, 126.975889, 45, 9, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'MAIN', '자경전 권역', '이제 자경전 권역이에요. 오늘의 마지막 하이라이트는 십장생굴뚝이에요. 이건 가까이서 봐야 진짜 가치가 있어요.', 37.579617, 126.977689, 90, 10, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';

-- TREASURE + PHOTO
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'TREASURE', '보물: 품계석', '지금 보이는 마당의 돌 표지들이 품계석이에요. 의식이 열릴 때 신하들이 어디에 서야 하는지를 표시하던 기준점이죠.', 37.578610, 126.976940, 30, 11, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'TREASURE', '보물: 풍기대', '풍기대는 바람과 관련된 시설로 소개되곤 하죠. 주변을 둘러보고 왜 이 위치에 이런 게 있을까를 생각해보는 거예요.', 37.580847, 126.974278, 30, 12, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'TREASURE', '보물: 십장생굴뚝(자경전 굴뚝)', '십장생굴뚝은 가까이 가면 표면에 장수 상징들이 촘촘히 들어가 있어요. 동물 하나, 하늘/자연 요소 하나—이렇게 두 가지만 찾아보세요.', 37.579650, 126.977650, 30, 13, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'PHOTO', '포토스팟: 광화문 정면', '광화문은 정면에서 찍을 때 가장 궁 입구 느낌이 살아나요. 정면 대칭, 문 전체가 프레임에 들어오게 한 걸음 뒤로.', 37.576044, 126.977019, 35, 14, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'PHOTO', '포토스팟: 근정전 대칭샷', '근정전은 대칭이 곧 정답이에요. 마당–계단–건물이 일직선으로 쌓이게 잡아보세요.', 37.578610, 126.976940, 35, 15, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'PHOTO', '포토스팟: 경회루 반영샷', '경회루는 반영(리플렉션)이 핵심이에요. 카메라를 살짝 낮추고, 수평만 맞춰도 사진이 안정적으로 나와요.', 37.579750, 126.975889, 40, 16, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'PHOTO', '포토스팟: 십장생굴뚝 디테일샷', '여기서는 풍경샷보다 디테일샷이 훨씬 예뻐요. 굴뚝 무늬를 한 칸씩 나눠서 찍는 느낌으로 가까이에서 여러 장 찍어보세요.', 37.579650, 126.977650, 30, 17, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';
INSERT INTO tour_spots (tour_id, type, title, description, latitude, longitude, radius_m, order_index, ai_chat_enabled, created_at, updated_at)
SELECT t.id, 'PHOTO', '포토스팟: 향원정', '향원정은 감성샷이 잘 나와요. 가능하면 다리까지 한 프레임에 넣어보세요. 연못 가장자리에서 낮은 앵글로 찍으면 물과 풍경이 같이 담겨요.', 37.582330, 126.977019, 45, 18, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM tours t WHERE t.external_key = 'gyeongbokgung';

-- start_spot_id 업데이트 (광화문 = order_index 0인 첫 번째 스팟)
UPDATE tours t SET start_spot_id = (SELECT id FROM tour_spots WHERE tour_id = t.id ORDER BY order_index LIMIT 1), updated_at = CURRENT_TIMESTAMP WHERE t.external_key = 'gyeongbokgung';

-- spot_content_steps + spot_script_lines (각 스팟별 가이드)
INSERT INTO spot_content_steps (spot_id, language, step_index, kind, title, is_published, created_at, updated_at)
SELECT s.id, 'ko', 0, 'GUIDE', s.title, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tour_spots s JOIN tours t ON s.tour_id = t.id WHERE t.external_key = 'gyeongbokgung';

INSERT INTO spot_script_lines (step_id, seq, role, text, created_at)
SELECT scs.id, 1, 'GUIDE', ts.description, CURRENT_TIMESTAMP
FROM spot_content_steps scs
JOIN tour_spots ts ON scs.spot_id = ts.id AND scs.title = ts.title
JOIN tours t ON ts.tour_id = t.id
WHERE t.external_key = 'gyeongbokgung';
