package com.recruiting.platform.execution.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.security")
public class InternalJwtProperties {

    private String issuer = "recruiting-platform";
    private String secret = "changeit-changeit-changeit-changeit";
    private String expectedAudience = "execution-service";
    private long ttlSeconds = 300;
    private String serviceName = "execution-service";

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getExpectedAudience() { return expectedAudience; }
    public void setExpectedAudience(String expectedAudience) { this.expectedAudience = expectedAudience; }
    public long getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
}
