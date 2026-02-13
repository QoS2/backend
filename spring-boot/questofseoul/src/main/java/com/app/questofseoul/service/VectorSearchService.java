package com.app.questofseoul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pgvector 유사도 검색. ai-server의 VectorRetriever가 동일한 테이블을 직접 조회하므로
 * Spring Boot에서는 관리용 또는 테스트용으로 사용.
 */
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;

    public List<String> search(String query, int limit) {
        float[] embedding = embeddingService.embed(query);
        if (embedding == null || embedding.length == 0) {
            return List.of();
        }
        String vectorStr = "[" + java.util.stream.IntStream.range(0, embedding.length)
            .mapToObj(i -> String.valueOf(embedding[i]))
            .collect(Collectors.joining(",")) + "]";
        String sql = "SELECT content FROM tour_knowledge_embeddings " +
            "WHERE embedding IS NOT NULL ORDER BY embedding <=> ?::vector LIMIT ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, vectorStr, limit);
        List<String> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object c = row.get("content");
            if (c != null) result.add(c.toString());
        }
        return result;
    }
}
