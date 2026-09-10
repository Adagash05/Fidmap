package com.amsal.fidmap.payment.billing;

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
     * Paddle performs the billing change and the webhook
     * updates the local subscription record.
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
     * Create Paddle Checkout information for
     * Startup, Business, or Lifetime.
     *
     * This endpoint is NOT used for the free trial.
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
     * Cancel a recurring Startup or Business subscription.
     *
     * Lifetime plans cannot be canceled because they are
     * one-time purchases.
     */
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(
            @RequestParam UUID workspaceId
    ) {

        billingService.cancelSubscription(workspaceId);

        return ResponseEntity.noContent().build();
    }


}