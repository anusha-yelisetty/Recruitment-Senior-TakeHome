package com.automwrite.assessment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "processing")
public class AsyncConfig {
    private String mode = "synchronous"; // Default to "synchronous"

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
