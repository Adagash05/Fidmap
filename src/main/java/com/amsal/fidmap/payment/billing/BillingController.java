package com.amsal.fidmap.payment.billing;

import com.amsal.fidmap.config.FidmapConfig;
import com.amsal.fidmap.payment.subscription.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final FidmapConfig fidmapConfig;

    /**
     * Get the current billing/subscription status
     * of a workspace.
     *
     * OWNER access required.
     */
    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> subscription(
            @RequestParam UUID workspaceId
    ) {

        return ResponseEntity.ok(
                billingService.getSubscription(workspaceId)
        );
    }

    /**
     * Get the number of days remaining in the
     * workspace's free trial.
     *
     * OWNER access required.
     */
    @GetMapping("/trial")
    public ResponseEntity<Long> trialDaysRemaining(
            @RequestParam UUID workspaceId
    ) {

        return ResponseEntity.ok(
                billingService.getTrialDaysRemaining(workspaceId)
        );
    }

    /**
     * Get the effective feature limits and capabilities
     * of the workspace's current plan.
     *
     * Any authenticated staff member belonging to the
     * workspace may read this information.
     */
    @GetMapping("/entitlements")
    public ResponseEntity<PlanEntitlementResponse> entitlements(
            @RequestParam UUID workspaceId
    ) {

        return ResponseEntity.ok(
                billingService.getEntitlements(workspaceId)
        );
    }

    /**
     * Change the plan of an existing Startup or Business
     * recurring subscription.
     *
     * Polar performs the billing change.
     *
     * The Polar webhook updates the local subscription
     * after Polar confirms the change.
     */
    @PatchMapping("/subscription/plan")
    public ResponseEntity<Void> changePlan(
            @RequestParam UUID workspaceId,
            @RequestParam BillingPlan plan
    ) {

        billingService.changePlan(
                workspaceId,
                plan
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a Polar hosted checkout session.
     *
     * This endpoint is not used for the local FIDMAP
     * 7-day trial.
     */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestParam BillingPlan plan,
            @RequestParam UUID workspaceId
    ) {

        return ResponseEntity.ok(
                billingService.createCheckout(
                        workspaceId,
                        plan
                )
        );
    }

    /**
     * Schedules a recurring subscription for cancellation
     * at the end of the current billing period.
     *
     * Lifetime plans cannot be canceled because they
     * are one-time purchases.
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(
            @RequestParam UUID workspaceId
    ) {

        billingService.cancelSubscription(workspaceId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/success")
    public ResponseEntity<Void> paymentSuccess(
            @RequestParam(required = false) String checkoutId
    ) {

        String frontendUrl = fidmapConfig.getFrontendUrl();

        String redirectUrl = frontendUrl + "/settings/billing?payment=success";

        return ResponseEntity
                .status(302)
                .header(
                        "Location",
                        redirectUrl
                )
                .build();
    }
}
