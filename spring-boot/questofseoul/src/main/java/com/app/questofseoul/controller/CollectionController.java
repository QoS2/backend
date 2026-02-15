package com.app.questofseoul.controller;

import com.app.questofseoul.dto.collection.*;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@Tag(name = "컬렉션", description = "Place/Treasure 도감 API")
public class CollectionController {

    private final CollectionService collectionService;
    private final AuthService authService;

    @Operation(summary = "Place 컬렉션 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/places")
    public ResponseEntity<PlaceCollectionResponse> getPlaceCollection(
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false, defaultValue = "ko") String lang) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(collectionService.getPlaceCollection(userId, tourId));
    }

    @Operation(summary = "Place 컬렉션 요약")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/places/summary")
    public ResponseEntity<PlaceCollectionSummaryResponse> getPlaceCollectionSummary() {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(collectionService.getPlaceCollectionSummary(userId));
    }

    @Operation(summary = "Treasure 컬렉션 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/treasures")
    public ResponseEntity<TreasureCollectionResponse> getTreasureCollection(
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false, defaultValue = "ko") String lang) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(collectionService.getTreasureCollection(userId, tourId));
    }

    @Operation(summary = "Treasure 컬렉션 요약")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/treasures/summary")
    public ResponseEntity<TreasureCollectionSummaryResponse> getTreasureCollectionSummary() {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(collectionService.getTreasureCollectionSummary(userId));
    }
}
