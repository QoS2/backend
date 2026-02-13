package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.SpotAdminResponse;
import com.app.questofseoul.dto.admin.SpotCreateRequest;
import com.app.questofseoul.dto.admin.SpotUpdateRequest;
import com.app.questofseoul.service.admin.AdminTourSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tours/{tourId}/spots")
@RequiredArgsConstructor
@Tag(name = "관리자 - Tour Spot", description = "Tour Spot(MAIN/SUB/PHOTO/TREASURE) CRUD")
public class AdminTourSpotController {

    private final AdminTourSpotService adminTourSpotService;

    @Operation(summary = "Spot 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<SpotAdminResponse>> list(@PathVariable Long tourId) {
        return ResponseEntity.ok(adminTourSpotService.list(tourId));
    }

    @Operation(summary = "Spot 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{spotId}")
    public ResponseEntity<SpotAdminResponse> get(@PathVariable Long tourId, @PathVariable Long spotId) {
        return ResponseEntity.ok(adminTourSpotService.get(tourId, spotId));
    }

    @Operation(summary = "Spot 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<SpotAdminResponse> create(
            @PathVariable Long tourId,
            @Valid @RequestBody SpotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTourSpotService.create(tourId, request));
    }

    @Operation(summary = "Spot 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{spotId}")
    public ResponseEntity<SpotAdminResponse> update(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @RequestBody SpotUpdateRequest request) {
        return ResponseEntity.ok(adminTourSpotService.update(tourId, spotId, request));
    }

    @Operation(summary = "Spot 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{spotId}")
    public ResponseEntity<Void> delete(@PathVariable Long tourId, @PathVariable Long spotId) {
        adminTourSpotService.delete(tourId, spotId);
        return ResponseEntity.noContent().build();
    }
}
