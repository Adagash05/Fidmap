package com.amsal.fidmap.payment.billing;

import java.util.UUID;

public record CheckoutResponse(
        UUID workspaceId,
        BillingPlan plan,
        String checkoutId,
        String checkoutUrl
) {
}