package com.app.questofseoul.dto.tour;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PreviewChatRequest(
    @NotBlank String text,
    List<ChatHistoryItem> history
) {
    public record ChatHistoryItem(String role, String content) {}
}
