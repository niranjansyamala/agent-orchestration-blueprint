package com.recruiting.platform.tools;

import com.recruiting.platform.persistence.entity.UserRecordStatusEntity;
import com.recruiting.platform.persistence.repository.UserRecordStatusRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class RecruitingToolsService {

    private final UserRecordStatusRepository userRecordStatusRepository;

    public RecruitingToolsService(UserRecordStatusRepository userRecordStatusRepository) {
        this.userRecordStatusRepository = userRecordStatusRepository;
    }

    @Tool("Fetch a recruiting candidate profile")
    public String candidateProfile(String candidateId) {
        return """
                Candidate %s
                Skills: Java, Spring Boot, Recruiting Domain APIs
                Experience: 8 years
                Strengths: stakeholder communication, platform engineering, workflow design
                Risks: needs validation on compensation expectations
                """.formatted(candidateId);
    }

    @Tool("Fetch a job requisition summary")
    public String requisitionSummary(String requisitionId) {
        return """
                Requisition %s
                Role: Senior Recruiting Platform Engineer
                Requirements: Java, distributed systems, integrations, hiring workflow automation
                Preferred: AI platform design, observability, human approval workflow experience
                """.formatted(requisitionId);
    }

    @Tool("Suggest interview time slots")
    public List<String> suggestInterviewSlots(String candidateId, String panelName) {
        return List.of(
                "2026-04-30T10:00:00Z with " + panelName,
                "2026-04-30T13:00:00Z with " + panelName,
                "2026-05-01T09:00:00Z with " + panelName
        );
    }

    @Tool("Update the durable candidate workflow status")
    public Map<String, Object> updateCandidateStatus(String tenantId,
                                                     String candidateId,
                                                     String newStatus,
                                                     String updatedBy,
                                                     String reason) {
        UserRecordStatusEntity entity = userRecordStatusRepository.findById(candidateId)
                .orElseGet(UserRecordStatusEntity::new);
        entity.setRecordId(candidateId);
        entity.setTenantId(tenantId);
        entity.setStatusCode(newStatus);
        entity.setStatusReason(reason);
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(Instant.now());
        userRecordStatusRepository.save(entity);
        return Map.of(
                "recordId", candidateId,
                "status", newStatus,
                "reason", reason
        );
    }
}
