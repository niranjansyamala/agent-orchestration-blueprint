package com.recruiting.platform.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Map;

public record SubmitAgentRequest(
        @NotBlank String query,
        @NotBlank String channel,
        @NotNull Map<String, Object> metadata
) implements Serializable {
}
