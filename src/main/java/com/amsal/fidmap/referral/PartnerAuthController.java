package com.amsal.fidmap.referral;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner/auth")
@RequiredArgsConstructor
public class PartnerAuthController {

    private final PartnerAuthService partnerAuthService;

    @PostMapping("/accept-invitation")
    public ResponseEntity<Void> acceptInvitation(
            @Valid
            @RequestBody
            AcceptReferralInvitationRequest request
    ) {

        partnerAuthService.acceptInvitation(
                request.token(),
                request.password()
        );

        return ResponseEntity.ok().build();
    }
}