package com.app.questofseoul.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PgvectorInit {

    private static final Logger log = LoggerFactory.getLogger(PgvectorInit.class);
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("Pgvector extension initialized");
        } catch (Exception e) {
            log.warn("Could not create vector extension: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_tour_knowledge_embeddings_vector
                ON tour_knowledge_embeddings USING hnsw (embedding vector_cosine_ops)
                """);
            log.info("Pgvector HNSW index ready");
        } catch (Exception e) {
            log.debug("HNSW index (table may not exist yet): {}", e.getMessage());
        }
    }
}
