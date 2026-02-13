package com.app.questofseoul.dto.tour;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RunRequest {
    @NotNull(message = "mode is required")
    private RunMode mode;

    public enum RunMode {
        START,
        CONTINUE,
        RESTART
    }
}
