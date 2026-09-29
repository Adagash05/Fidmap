package com.amsal.fidmap.referral;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateReferralPartnerRequest {

    @Size(max = 120)
    private String name;

    @Size(max = 255)
    private String email;

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal commissionPercentage;

    private ReferralPartnerStatus status;
}