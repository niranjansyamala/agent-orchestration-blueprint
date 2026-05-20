package com.recruiting.platform.registry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "agent_registry")
public class AgentRegistryEntity {

    @Id
    @Column(name = "agent_id", nullable = false, length = 100)
    private String agentId;

    @Column(name = "agent_name", nullable = false, length = 200)
    private String agentName;

    @Column(name = "version", nullable = false, length = 40)
    private String version;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "supported_intents", nullable = false, length = 4000)
    private String supportedIntents;

    @Column(name = "capabilities", nullable = false, length = 4000)
    private String capabilities;

    @Column(name = "dispatch_type", nullable = false, length = 30)
    private String dispatchType;

    @Column(name = "dispatch_target", nullable = false, length = 255)
    private String dispatchTarget;

    @Column(name = "tenant_scope", nullable = false, length = 4000)
    private String tenantScope;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "health_status", length = 30)
    private String healthStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSupportedIntents() {
        return supportedIntents;
    }

    public void setSupportedIntents(String supportedIntents) {
        this.supportedIntents = supportedIntents;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public String getDispatchType() {
        return dispatchType;
    }

    public void setDispatchType(String dispatchType) {
        this.dispatchType = dispatchType;
    }

    public String getDispatchTarget() {
        return dispatchTarget;
    }

    public void setDispatchTarget(String dispatchTarget) {
        this.dispatchTarget = dispatchTarget;
    }

    public String getTenantScope() {
        return tenantScope;
    }

    public void setTenantScope(String tenantScope) {
        this.tenantScope = tenantScope;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
