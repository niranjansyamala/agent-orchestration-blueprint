package com.recruiting.platform.common.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record PauseWorkflowRequest(@NotBlank String reason) implements Serializable {
}
