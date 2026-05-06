package com.insurance.strategy.impl;

import com.insurance.entity.InsurancePlan;
import com.insurance.entity.User;
import com.insurance.entity.Vehicle;
import com.insurance.strategy.PremiumCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Zero Depreciation premium strategy — the premium plan.
 * No depreciation is applied to claim amounts.
 * Carries a 40% premium surcharge over comprehensive.
 */
@Component("zeroDepreciationStrategy")
public class ZeroDepreciationPremiumStrategy implements PremiumCalculationStrategy {

    private final ComprehensivePremiumStrategy comprehensiveStrategy;

    public ZeroDepreciationPremiumStrategy(ComprehensivePremiumStrategy comprehensiveStrategy) {
        this.comprehensiveStrategy = comprehensiveStrategy;
    }

    @Override
    public BigDecimal calculatePremium(Vehicle vehicle, InsurancePlan plan, User user) {
        // Only eligible for vehicles up to 5 years old
        if (vehicle.getVehicleAge() > 5) {
            throw new IllegalStateException(
                "Zero Depreciation cover is only available for vehicles up to 5 years old");
        }

        BigDecimal comprehensivePremium = comprehensiveStrategy.calculatePremium(vehicle, plan, user);
        // 40% surcharge for zero-dep coverage
        return comprehensivePremium.multiply(BigDecimal.valueOf(1.40))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
