package com.app.questofseoul.controller;

import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.enums.MarkerType;
import com.app.questofseoul.dto.tour.MarkerResponse;
import com.app.questofseoul.dto.tour.RunRequest;
import com.app.questofseoul.dto.tour.RunResponse;
import com.app.questofseoul.dto.tour.TourDetailResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.UserRepository;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.TourAccessService;
import com.app.questofseoul.service.TourDetailService;
import com.app.questofseoul.service.TourMarkerService;
import com.app.questofseoul.service.TourRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "투어", description = "Tour 디테일 / 접근 / Run API")
public class TourController {

    private final TourDetailService tourDetailService;
    private final TourAccessService tourAccessService;
    private final TourRunService tourRunService;
    private final TourMarkerService tourMarkerService;
    private final com.app.questofseoul.repository.TourRepository tourRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "투어 목록")
    @GetMapping("/tours")
    public ResponseEntity<java.util.List<com.app.questofseoul.dto.tour.TourListItem>> listTours() {
        return ResponseEntity.ok(tourRepository.findAll().stream()
                .map(t -> new com.app.questofseoul.dto.tour.TourListItem(
                        t.getId(),
                        t.getExternalKey(),
                        t.getDisplayTitle()))
                .toList());
    }

    @Operation(summary = "마커 목록", description = "맵용 마커. filter: STEP, WAYPOINT, PHOTO_SPOT, TREASURE")
    @GetMapping("/tours/{tourId}/markers")
    public ResponseEntity<List<MarkerResponse>> getMarkers(
            @PathVariable Long tourId,
            @RequestParam(required = false) MarkerType filter) {
        return ResponseEntity.ok(tourMarkerService.getMarkers(tourId, filter));
    }

    @Operation(summary = "투어 디테일 조회", description = "접근권한, currentRun, actions 포함. 프론트는 이 응답으로 UNLOCK/START/CONTINUE 결정")
    @GetMapping("/tours/{tourId}")
    public ResponseEntity<TourDetailResponse> getTourDetail(@PathVariable Long tourId) {
        UUID userId = getUserIdOrNull();
        return ResponseEntity.ok(tourDetailService.getTourDetail(tourId, userId));
    }

    @Operation(summary = "Unlock", description = "user_tour_access를 UNLOCKED로 upsert")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tours/{tourId}/access/unlock")
    public ResponseEntity<Void> unlockTour(@PathVariable Long tourId) {
        UUID userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        tourAccessService.unlockTour(user, tourId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Run 처리", description = "mode: START | CONTINUE | RESTART. 유저당 투어별 IN_PROGRESS 1개")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tours/{tourId}/runs")
    public ResponseEntity<RunResponse> handleRun(
            @PathVariable Long tourId,
            @RequestParam(required = false) RunRequest.RunMode mode,
            @RequestBody(required = false) @Valid RunRequest body) {
        UUID userId = authService.getCurrentUserId();
        RunRequest.RunMode runMode = mode != null ? mode : (body != null ? body.getMode() : RunRequest.RunMode.START);
        RunResponse response = tourRunService.handleRun(userId, tourId, runMode);
        return ResponseEntity.ok(response);
    }

    private UUID getUserIdOrNull() {
        try {
            return authService.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
