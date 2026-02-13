package com.app.questofseoul.dto.tour;

import org.hibernate.validator.constraints.Length;

import java.util.Map;

public record MissionSubmitRequest(
    String userInput,
    String photoUrl,
    Map<String, Object> selectedOption
) {}
