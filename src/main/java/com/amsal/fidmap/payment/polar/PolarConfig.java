package com.amsal.fidmap.payment.polar;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "polar")
public class PolarConfig {

    /**
     * Polar API base URL.
     *
     * Production:
     * https://api.polar.sh
     *
     * Sandbox:
     * https://sandbox-api.polar.sh
     */
    private String apiUrl;

    /**
     * Polar organization access token.
     *
     * Backend only.
     * NEVER expose this to the frontend.
     */
    private String accessToken;

    /**
     * Polar webhook signing secret.
     *
     * Backend only.
     */
    private String webhookSecret;

    private Products products = new Products();

    @Getter
    @Setter
    public static class Products {

        private String startupMonthly;

        private String startupYearly;

        private String businessMonthly;

        private String businessYearly;

        private String lifetime;
    }
}