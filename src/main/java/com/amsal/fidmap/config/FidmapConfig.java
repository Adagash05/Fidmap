package com.amsal.fidmap.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "fidmap")
public class FidmapConfig {

    private String frontendUrl;

    /**
     * One-time secret that allows creating the FIRST super admin when none exists
     * (header X-Bootstrap-Token). Leave empty to disable bootstrap entirely.
     */
    private String adminBootstrapToken;
}