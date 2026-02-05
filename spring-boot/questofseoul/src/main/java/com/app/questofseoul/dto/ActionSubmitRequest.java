package com.app.questofseoul.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class ActionSubmitRequest {
    private String userInput;
    private String photoUrl;
    private Map<String, Object> selectedOption;
}
