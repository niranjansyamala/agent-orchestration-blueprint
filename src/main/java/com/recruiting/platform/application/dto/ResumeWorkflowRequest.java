package com.recruiting.platform.application.dto;

import java.io.Serializable;

public record ResumeWorkflowRequest(
        boolean approved,
        String approvedBy
) implements Serializable {
}
