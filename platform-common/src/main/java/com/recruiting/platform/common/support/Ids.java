package com.recruiting.platform.common.support;

import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String requestId() {
        return "req_" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String workflowId() {
        return "wf_" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String stepId() {
        return "step_" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String auditId() {
        return "aud_" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String hitlId() {
        return "hitl_" + UUID.randomUUID().toString().replace("-", "");
    }
}
