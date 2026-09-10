package com.amsal.fidmap.payment.billing;

import lombok.Getter;

@Getter
public enum BillingPlan {

    STARTUP_MONTHLY("Startup", "Monthly"),
    STARTUP_YEARLY("Startup", "Yearly"),
    BUSINESS_MONTHLY("Business", "Monthly"),
    BUSINESS_YEARLY("Business", "Yearly"),
    LIFETIME("Lifetime", null);

    private final String displayName;
    private final String billingInterval;

    BillingPlan(String displayName, String billingInterval) {
        this.displayName = displayName;
        this.billingInterval = billingInterval;
    }


}