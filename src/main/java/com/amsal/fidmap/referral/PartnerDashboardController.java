package com.amsal.fidmap.referral;

import com.amsal.fidmap.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partner/referrals")
@RequiredArgsConstructor
public class PartnerDashboardController {

    private final PartnerDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<PartnerDashboardResponse> dashboard(
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();

        assert user != null;
        return ResponseEntity.ok(
                dashboardService.getDashboard(user)
        );
    }

    @GetMapping("/conversions")
    public ResponseEntity<List<PartnerConversionResponse>> conversions(
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();

        assert user != null;
        return ResponseEntity.ok(
                dashboardService.getConversions(user)
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<ReferralPartnerProfileResponse> profile(
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                dashboardService.getProfile(user)
        );
    }
}