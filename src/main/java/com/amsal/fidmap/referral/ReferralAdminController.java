package com.amsal.fidmap.referral;

import com.amsal.fidmap.apiResponse.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/referrals")
@RequiredArgsConstructor
public class ReferralAdminController {

    private final ReferralAdminService adminService;
    private final ReferralInvitationService referralInvitationService;
    private final ReferralPayoutService referralPayoutService;

    @PostMapping("/partners")
    public ResponseEntity<ApiResponse<ReferralPartnerResponse>>
    createPartner(
            @Valid @RequestBody CreateReferralPartnerRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral partner created successfully",
                        adminService.createPartner(request)
                )
        );
    }

    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<List<ReferralPartnerResponse>>>
    getPartners() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral partners retrieved successfully",
                        adminService.getPartners()
                )
        );
    }

    @GetMapping("/partners/{id}")
    public ResponseEntity<ApiResponse<ReferralPartnerResponse>>
    getPartner(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral partner retrieved successfully",
                        adminService.getPartner(id)
                )
        );
    }

    @PutMapping("/partners/{id}")
    public ResponseEntity<ApiResponse<ReferralPartnerResponse>>
    updatePartner(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReferralPartnerRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral partner updated successfully",
                        adminService.updatePartner(
                                id,
                                request
                        )
                )
        );
    }

    @GetMapping("/partners/{id}/conversions")
    public ResponseEntity<ApiResponse<List<ReferralConversionResponse>>>
    getConversions(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral conversions retrieved successfully",
                        adminService.getConversions(id)
                )
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReferralAdminSummaryResponse>>
    getSummary() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral summary retrieved successfully",
                        adminService.getSummary()
                )
        );
    }

    @PostMapping("/partners/{id}/invitation")
    public ResponseEntity<ReferralInvitationResponse>
    invitePartner(
            @PathVariable UUID id
    ) {

        ReferralPartner partner =
                adminService.getPartnerEntity(id);

        ReferralInvitation invitation =
                referralInvitationService.createInvitation(
                        partner
                );

        String invitationUrl =
                "https://app.fidmap.co/partner/accept-invitation?token="
                        + invitation.getToken();

        return ResponseEntity.ok(
                new ReferralInvitationResponse(
                        invitation.getId(),
                        invitation.getEmail(),
                        invitationUrl,
                        invitation.getExpiresAt(),
                        invitation.isAccepted()
                )
        );
    }

    @PutMapping("/conversions/{conversionId}/payout")
    public ResponseEntity<ApiResponse<ReferralConversionResponse>>
    updatePayout(
            @PathVariable UUID conversionId,
            @Valid @RequestBody UpdateReferralPayoutRequest request
    ) {

        ReferralConversion conversion =
                referralPayoutService.updatePayoutStatus(
                        conversionId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Referral payout status updated successfully",
                        toResponse(conversion)
                )
        );
    }

    private ReferralConversionResponse toResponse(
            ReferralConversion conversion
    ) {

        return ReferralConversionResponse.builder()
                .id(conversion.getId())
                .partnerId(
                        conversion.getPartner().getId()
                )
                .workspaceId(
                        conversion.getWorkspace().getId()
                )
                .providerOrderId(
                        conversion.getProviderOrderId()
                )
                .providerSubscriptionId(
                        conversion.getProviderSubscriptionId()
                )
                .providerProductId(
                        conversion.getProviderProductId()
                )
                .product(
                        conversion.getProduct()
                )
                .revenueAmountMinor(
                        conversion.getRevenueAmountMinor()
                )
                .currency(
                        conversion.getCurrency()
                )
                .commissionPercentage(
                        conversion.getCommissionPercentage()
                )
                .commissionAmountMinor(
                        conversion.getCommissionAmountMinor()
                )
                .status(
                        conversion.getStatus()
                )
                .createdAt(
                        conversion.getCreatedAt()
                )
                .build();
    }
}