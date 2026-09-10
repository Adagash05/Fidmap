package com.amsal.fidmap.payment.paddle;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "paddle")
public class PaddleConfig {

    private String apiUrl;

    /**
     * Paddle API key.
     *
     * Backend only.
     * NEVER expose this to the frontend.
     */
    private String apiKey;

    /**
     * Paddle webhook secret.
     *
     * Backend only.
     * Used to verify Paddle webhook signatures.
     */
    private String webhookSecret;

    /**
     * Paddle client-side token.
     *
     * This can be exposed to the frontend because Paddle
     * uses it for Paddle.js initialization.
     */
    private String clientToken;

    private Prices prices = new Prices();

    @Getter
    @Setter
    public static class Prices {

        /**
         * Recurring monthly Startup price.
         */
        private String startupMonthly;

         /**
         * Recurring yearly Startup price.
         */
        private String startupYearly;



        /**
         * Recurring monthly Business price.
         */
        private String businessMonthly;

        /**
         * Recurring yearly Business price.
         */
        private String businessYearly;

        /**
         * One-time Lifetime price.
         */
        private String lifetime;
    }

//    public record Prices(
//            String startupMonthly,
//            String startupYearly,
//            String businessMonthly,
//            String businessYearly,
//            String lifetime
//    ) {}

}