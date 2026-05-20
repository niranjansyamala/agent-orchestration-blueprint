package com.recruiting.platform.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.recruiting.platform")
public class RecruitingMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitingMcpServerApplication.class, args);
    }
}
