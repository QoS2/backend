-- lock_state 제거 (progress_status로 통합)
ALTER TABLE user_spot_progress DROP COLUMN IF EXISTS lock_state;

-- tour_assets 테이블 (투어 레벨 전용 에셋)
CREATE TABLE IF NOT EXISTS tour_assets (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    asset_id BIGINT NOT NULL REFERENCES media_assets(id) ON DELETE CASCADE,
    usage VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 1,
    caption VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (tour_id, usage, sort_order)
);

CREATE INDEX IF NOT EXISTS idx_tour_assets_tour_id ON tour_assets(tour_id);
CREATE INDEX IF NOT EXISTS idx_tour_assets_asset_id ON tour_assets(asset_id);

COMMENT ON TABLE tour_assets IS '투어 레벨 이미지/썸네일 (THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE)';
