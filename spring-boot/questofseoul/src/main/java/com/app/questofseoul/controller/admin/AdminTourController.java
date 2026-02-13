package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.TourAdminResponse;
import com.app.questofseoul.dto.admin.TourCreateRequest;
import com.app.questofseoul.dto.admin.TourUpdateRequest;
import com.app.questofseoul.service.admin.AdminTourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tours")
@RequiredArgsConstructor
@Tag(name = "관리자 - Tour", description = "Tour CRUD")
public class AdminTourController {

    private final AdminTourService adminTourService;

    @Operation(summary = "Tour 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<Page<TourAdminResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminTourService.list(pageable));
    }

    @Operation(summary = "Tour 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{tourId}")
    public ResponseEntity<TourAdminResponse> get(@PathVariable Long tourId) {
        return ResponseEntity.ok(adminTourService.get(tourId));
    }

    @Operation(summary = "Tour 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<TourAdminResponse> create(@Valid @RequestBody TourCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTourService.create(request));
    }

    @Operation(summary = "Tour 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{tourId}")
    public ResponseEntity<TourAdminResponse> update(
            @PathVariable Long tourId,
            @RequestBody TourUpdateRequest request) {
        return ResponseEntity.ok(adminTourService.update(tourId, request));
    }

    @Operation(summary = "Tour 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{tourId}")
    public ResponseEntity<Void> delete(@PathVariable Long tourId) {
        adminTourService.delete(tourId);
        return ResponseEntity.noContent().build();
    }
}
