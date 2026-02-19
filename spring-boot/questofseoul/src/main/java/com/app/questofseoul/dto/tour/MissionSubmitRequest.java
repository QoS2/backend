package com.app.questofseoul.dto.tour;

import com.app.questofseoul.domain.enums.MissionType;
import jakarta.validation.constraints.NotNull;

public record MissionSubmitRequest(
    @NotNull MissionType missionType,
    String userInput,
    String photoUrl,
    String selectedOptionId
) {}
