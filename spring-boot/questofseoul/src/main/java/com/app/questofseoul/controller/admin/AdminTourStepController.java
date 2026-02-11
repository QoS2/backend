package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.StepAdminResponse;
import com.app.questofseoul.dto.admin.StepCreateRequest;
import com.app.questofseoul.dto.admin.StepUpdateRequest;
import com.app.questofseoul.service.admin.AdminTourStepService;
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
@RequestMapping("/api/v1/admin/tours/{tourId}/steps")
@RequiredArgsConstructor
@Tag(name = "관리자 - Tour Step", description = "Tour Step(Place) CRUD")
public class AdminTourStepController {

    private final AdminTourStepService adminTourStepService;

    @Operation(summary = "Step 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<StepAdminResponse>> list(@PathVariable Long tourId) {
        return ResponseEntity.ok(adminTourStepService.list(tourId));
    }

    @Operation(summary = "Step 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{stepId}")
    public ResponseEntity<StepAdminResponse> get(@PathVariable Long tourId, @PathVariable Long stepId) {
        return ResponseEntity.ok(adminTourStepService.get(tourId, stepId));
    }

    @Operation(summary = "Step 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<StepAdminResponse> create(
            @PathVariable Long tourId,
            @Valid @RequestBody StepCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTourStepService.create(tourId, request));
    }

    @Operation(summary = "Step 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{stepId}")
    public ResponseEntity<StepAdminResponse> update(
            @PathVariable Long tourId,
            @PathVariable Long stepId,
            @RequestBody StepUpdateRequest request) {
        return ResponseEntity.ok(adminTourStepService.update(tourId, stepId, request));
    }

    @Operation(summary = "Step 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{stepId}")
    public ResponseEntity<Void> delete(@PathVariable Long tourId, @PathVariable Long stepId) {
        adminTourStepService.delete(tourId, stepId);
        return ResponseEntity.noContent().build();
    }
}
