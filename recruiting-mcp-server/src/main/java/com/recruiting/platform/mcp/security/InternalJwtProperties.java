package com.recruiting.platform.mcp.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.security")
public class InternalJwtProperties {

    private String issuer = "recruiting-platform";
    private String secret = "changeit-changeit-changeit-changeit";
    private String expectedAudience = "recruiting-mcp-server";

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getExpectedAudience() { return expectedAudience; }
    public void setExpectedAudience(String expectedAudience) { this.expectedAudience = expectedAudience; }
}
