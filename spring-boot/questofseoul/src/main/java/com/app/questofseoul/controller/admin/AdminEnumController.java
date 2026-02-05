package com.app.questofseoul.controller.admin;

import com.app.questofseoul.domain.enums.*;
import com.app.questofseoul.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/enums")
@RequiredArgsConstructor
@Tag(name = "관리자 - Enum", description = "관리자 폼용 Enum 값 조회")
public class AdminEnumController {

    private static final Map<String, Class<? extends Enum<?>>> ENUM_MAP = Map.ofEntries(
        Map.entry("questTheme", QuestTheme.class),
        Map.entry("questTone", QuestTone.class),
        Map.entry("difficulty", Difficulty.class),
        Map.entry("nodeType", NodeType.class),
        Map.entry("contentType", ContentType.class),
        Map.entry("actionType", ActionType.class),
        Map.entry("effectType", EffectType.class),
        Map.entry("language", Language.class),
        Map.entry("displayMode", DisplayMode.class),
        Map.entry("transitionMessageType", TransitionMessageType.class)
    );

    @Operation(summary = "Enum 값 목록 조회", description = "프론트 폼용 Enum 상수 목록을 조회합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{enumName}")
    public ResponseEntity<List<String>> getEnumValues(
            @Parameter(description = "Enum 이름 (questTheme, difficulty, nodeType 등)") @PathVariable String enumName) {
        Class<? extends Enum<?>> enumClass = ENUM_MAP.get(enumName);
        if (enumClass == null) {
            throw new ResourceNotFoundException("Enum", enumName);
        }
        List<String> values = Arrays.stream(enumClass.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toList());
        return ResponseEntity.ok(values);
    }
}
