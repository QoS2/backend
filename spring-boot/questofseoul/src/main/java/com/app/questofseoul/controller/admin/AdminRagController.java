package com.app.questofseoul.controller.admin;

import com.app.questofseoul.service.TourKnowledgeSyncService;
import com.app.questofseoul.service.VectorSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/rag")
@RequiredArgsConstructor
@Tag(name = "관리자 - RAG", description = "투어 지식 벡터 RAG 동기화 및 검색")
public class AdminRagController {

    private final TourKnowledgeSyncService syncService;
    private final VectorSearchService vectorSearchService;

    @Operation(summary = "투어 지식 벡터 동기화")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync(
            @RequestParam(required = false) Long tourId) {
        int count = tourId != null ? syncService.syncTour(tourId) : syncService.syncAll();
        return ResponseEntity.ok(Map.of("embeddingsCount", count));
    }

    @Operation(summary = "벡터 유사도 검색 테스트")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/search")
    public ResponseEntity<List<String>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(vectorSearchService.search(q, Math.min(limit, 20)));
    }
}
