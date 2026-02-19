-- missions.mission_type 표준값 통일:
-- INPUT -> TEXT_INPUT
-- PHOTO_CHECK -> PHOTO
-- 그리고 user_spot_progress.lock_state 잔존 컬럼 제거

-- 1) lock_state 제거 (이미 없으면 skip)
ALTER TABLE IF EXISTS public.user_spot_progress
DROP COLUMN IF EXISTS lock_state;

-- 2) missions 기존 데이터 정규화
UPDATE public.missions
SET mission_type = 'TEXT_INPUT'
WHERE mission_type = 'INPUT';

UPDATE public.missions
SET mission_type = 'PHOTO'
WHERE mission_type = 'PHOTO_CHECK';

-- 3) attempts.answer_json 하위호환 값도 정규화
UPDATE public.user_mission_attempts
SET answer_json = jsonb_set(answer_json, '{missionType}', '"TEXT_INPUT"'::jsonb, true)
WHERE answer_json IS NOT NULL
  AND answer_json->>'missionType' = 'INPUT';

UPDATE public.user_mission_attempts
SET answer_json = jsonb_set(answer_json, '{missionType}', '"PHOTO"'::jsonb, true)
WHERE answer_json IS NOT NULL
  AND answer_json->>'missionType' = 'PHOTO_CHECK';

-- 4) mission_type 체크 제약 재정의 (환경마다 제약명 달라질 수 있어 정의문 기준으로 제거)
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT c.conname
        FROM pg_constraint c
                 JOIN pg_class t ON t.oid = c.conrelid
                 JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE n.nspname = 'public'
          AND t.relname = 'missions'
          AND c.contype = 'c'
          AND pg_get_constraintdef(c.oid) LIKE '%mission_type%'
        LOOP
            EXECUTE format('ALTER TABLE public.missions DROP CONSTRAINT IF EXISTS %I', r.conname);
        END LOOP;
END $$;

ALTER TABLE public.missions
    ADD CONSTRAINT missions_mission_type_check
        CHECK (mission_type::text = ANY (ARRAY[
            'QUIZ'::character varying,
            'OX'::character varying,
            'PHOTO'::character varying,
            'TEXT_INPUT'::character varying
            ]::text[]));
