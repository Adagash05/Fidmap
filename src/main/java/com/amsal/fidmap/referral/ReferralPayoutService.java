package com.amsal.fidmap.referral;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralPayoutService {

    private final ReferralConversionRepository conversionRepository;
    private final ReferralPayoutEventRepository payoutEventRepository;

    @Transactional
    public ReferralConversion updatePayoutStatus(
            UUID conversionId,
            UpdateReferralPayoutRequest request
    ) {

        ReferralConversion conversion =
                conversionRepository.findById(conversionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Referral conversion not found"
                                )
                        );

        ReferralPayoutStatus previousStatus =
                conversion.getPayoutStatus();

        ReferralPayoutStatus newStatus =
                request.status();

        if (conversion.getStatus()
                == ReferralConversionStatus.REVERSED) {

            throw new IllegalStateException(
                    "A reversed conversion cannot receive a payout"
            );
        }

        if (newStatus == ReferralPayoutStatus.PAID
                && conversion.getCommissionAmountMinor() <= 0) {

            throw new IllegalStateException(
                    "Cannot pay a zero-value commission"
            );
        }

        if (previousStatus == ReferralPayoutStatus.PAID
                && newStatus != ReferralPayoutStatus.PAID) {

            throw new IllegalStateException(
                    "A paid referral payout cannot be changed. " +
                            "Create a reversal process instead."
            );
        }

        conversion.setPayoutStatus(newStatus);

        if (newStatus == ReferralPayoutStatus.PAID) {

            conversion.setPaidAt(
                    Instant.now()
            );

            conversion.setPayoutReference(
                    request.payoutReference()
            );

            conversion.setPayoutNote(
                    request.payoutNote()
            );

        } else {

            conversion.setPaidAt(null);

            conversion.setPayoutReference(
                    request.payoutReference()
            );

            conversion.setPayoutNote(
                    request.payoutNote()
            );
        }

        ReferralPayoutEvent event =
                ReferralPayoutEvent.builder()
                        .conversion(conversion)
                        .previousStatus(previousStatus)
                        .newStatus(newStatus)
                        .payoutReference(
                                request.payoutReference()
                        )
                        .note(
                                request.payoutNote()
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        payoutEventRepository.save(event);

        return conversionRepository.save(conversion);
    }
}