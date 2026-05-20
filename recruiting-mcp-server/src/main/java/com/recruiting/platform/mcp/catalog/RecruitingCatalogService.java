package com.recruiting.platform.mcp.catalog;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RecruitingCatalogService {

    public Map<String, Object> candidate(String candidateId) {
        return Map.of(
                "candidateId", candidateId,
                "name", "Alex Morgan",
                "skills", "Java, Spring Boot, Workflow Design, Recruiting Platforms",
                "experienceYears", 8,
                "summary", "Strong platform engineer with hiring workflow and integration experience."
        );
    }

    public Map<String, Object> requisition(String requisitionId) {
        return Map.of(
                "requisitionId", requisitionId,
                "title", "Senior Recruiting Platform Engineer",
                "requirements", "Java, distributed systems, integrations, hiring workflow automation",
                "preferred", "AI platform design, observability, human approval workflow experience",
                "summary", "Platform role focused on recruiting workflows and multi-agent operations."
        );
    }

    public Map<String, Object> jobApplication(String applicationId) {
        return Map.of(
                "applicationId", applicationId,
                "candidateId", "candidate-1001",
                "requisitionId", "req-42",
                "status", "IN_REVIEW",
                "summary", "Application is active and awaiting recruiter next-step action."
        );
    }
}
