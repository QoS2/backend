-- spot_content_steps.next_action 컬럼 추가
-- 컨텐츠 끝난 후 버튼 유형: NEXT (다음 컨텐츠) | MISSION_CHOICE (게임 스타트/스킵)
-- Hibernate ddl-auto=update 사용 시 자동 적용됨. 수동 마이그레이션 시에만 실행.

ALTER TABLE spot_content_steps
ADD COLUMN IF NOT EXISTS next_action VARCHAR(20);

COMMENT ON COLUMN spot_content_steps.next_action IS 'NEXT | MISSION_CHOICE';
