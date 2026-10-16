package com.amsal.fidmap.feedbackPost;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ImpactScoreService {

    public int calculate(
            int votes,
            int payingCustomers,
            BigDecimal affectedMrr,
            int salesOpportunities,
            int churnRiskCustomers,
            int estimatedEffort,
            int demandGrowth
    ) {

        int voteScore = Math.min(votes * 2, 20);

        int customerScore =
                Math.min(payingCustomers * 4, 20);

        int revenueScore =
                Math.min(affectedMrr.divide(
                        BigDecimal.valueOf(500),
                        RoundingMode.DOWN
                ).intValue(), 25);

        int salesScore =
                Math.min(salesOpportunities * 3, 10);

        int churnScore =
                Math.min(churnRiskCustomers * 3, 10);

        int growthScore =
                Math.min(demandGrowth, 10);

        int effortPenalty =
                switch (estimatedEffort) {
                    case 1 -> 0;
                    case 2 -> 2;
                    case 3 -> 5;
                    case 4 -> 8;
                    case 5 -> 12;
                    default -> 5;
                };

        int score =
                voteScore
                        + customerScore
                        + revenueScore
                        + salesScore
                        + churnScore
                        + growthScore
                        - effortPenalty;

        return Math.max(0, Math.min(score, 100));
    }
}