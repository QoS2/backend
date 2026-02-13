-- 경복궁 투어 init용 핵심 테이블
-- 실행: psql -U postgres -d questofseoul -f schema.sql
-- 또는 Spring Boot: spring.sql.init.mode=always, spring.sql.init.schema-locations=classpath:db/schema.sql

-- pgvector 확장 (이미 있으면 스킵)
CREATE EXTENSION IF NOT EXISTS vector;

-- tours (start_spot_id는 tour_spots 생성 후 UPDATE)
CREATE TABLE IF NOT EXISTS tours (
    id BIGSERIAL PRIMARY KEY,
    external_key VARCHAR(255) UNIQUE,
    title VARCHAR(255),
    title_en VARCHAR(255),
    description TEXT,
    description_en TEXT,
    info_json JSONB,
    good_to_know_json JSONB,
    start_spot_id BIGINT,
    is_published BOOLEAN DEFAULT true,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- tour_spots
CREATE TABLE IF NOT EXISTS tour_spots (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    parent_spot_id BIGINT REFERENCES tour_spots(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    radius_m INT NOT NULL DEFAULT 50,
    order_index INT NOT NULL DEFAULT 0,
    ai_chat_enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE tours DROP CONSTRAINT IF EXISTS fk_tours_start_spot;
ALTER TABLE tours ADD CONSTRAINT fk_tours_start_spot
    FOREIGN KEY (start_spot_id) REFERENCES tour_spots(id) ON DELETE SET NULL;

-- spot_content_steps
CREATE TABLE IF NOT EXISTS spot_content_steps (
    id BIGSERIAL PRIMARY KEY,
    spot_id BIGINT NOT NULL REFERENCES tour_spots(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL DEFAULT 'ko',
    step_index INT NOT NULL DEFAULT 0,
    kind VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    mission_id BIGINT,
    is_published BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- spot_script_lines
CREATE TABLE IF NOT EXISTS spot_script_lines (
    id BIGSERIAL PRIMARY KEY,
    step_id BIGINT NOT NULL REFERENCES spot_content_steps(id) ON DELETE CASCADE,
    seq INT NOT NULL DEFAULT 1,
    role VARCHAR(50) NOT NULL DEFAULT 'GUIDE',
    text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tour_spots_tour_id ON tour_spots(tour_id);
CREATE INDEX IF NOT EXISTS idx_spot_content_steps_spot_id ON spot_content_steps(spot_id);
CREATE INDEX IF NOT EXISTS idx_spot_script_lines_step_id ON spot_script_lines(step_id);
