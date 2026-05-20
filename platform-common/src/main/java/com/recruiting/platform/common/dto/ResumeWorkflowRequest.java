package com.recruiting.platform.common.dto;

import java.io.Serializable;

public record ResumeWorkflowRequest(boolean approved, String approvedBy) implements Serializable {
}
