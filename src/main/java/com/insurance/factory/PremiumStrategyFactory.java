package com.insurance.factory;

import com.insurance.entity.Policy;
import com.insurance.strategy.PremiumCalculationStrategy;
import com.insurance.strategy.impl.ComprehensivePremiumStrategy;
import com.insurance.strategy.impl.ThirdPartyPremiumStrategy;
import com.insurance.strategy.impl.ZeroDepreciationPremiumStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern — resolves the correct PremiumCalculationStrategy
 * based on the policy type requested.
 *
 * Decouples callers from concrete strategy implementations.
 */
@Component
@RequiredArgsConstructor
public class PremiumStrategyFactory {

    private final ThirdPartyPremiumStrategy thirdPartyStrategy;
    private final ComprehensivePremiumStrategy comprehensiveStrategy;
    private final ZeroDepreciationPremiumStrategy zeroDepreciationStrategy;

    public PremiumCalculationStrategy getStrategy(Policy.PolicyType policyType) {
        return switch (policyType) {
            case THIRD_PARTY, THIRD_PARTY_FIRE_THEFT -> thirdPartyStrategy;
            case COMPREHENSIVE                        -> comprehensiveStrategy;
            case ZERO_DEPRECIATION                    -> zeroDepreciationStrategy;
        };
    }
}
