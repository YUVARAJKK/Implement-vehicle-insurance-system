package com.insurance.strategy.impl;

import com.insurance.entity.InsurancePlan;
import com.insurance.entity.User;
import com.insurance.entity.Vehicle;
import com.insurance.strategy.PremiumCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Third Party insurance premium strategy.
 *
 * Formula:
 *   basePremium = vehicleMarketValue × basePremiumRate
 *   ageLoad    = max(0, vehicleAge - 3) × 1.5%   // surcharge for older vehicles
 *   riskLoad   = basePremium × riskMultiplier
 *   premium    = basePremium + ageLoad + riskLoad
 *
 * Third-party coverage is the minimum legal requirement → lowest base rate.
 */
@Component("thirdPartyStrategy")
public class ThirdPartyPremiumStrategy implements PremiumCalculationStrategy {

    @Override
    public BigDecimal calculatePremium(Vehicle vehicle, InsurancePlan plan, User user) {
        BigDecimal marketValue = vehicle.getMarketValue();
        BigDecimal basePremium = marketValue.multiply(plan.getBasePremiumRate());

        // Age surcharge: 1.5% per year beyond 3 years
        int extraYears = Math.max(0, vehicle.getVehicleAge() - 3);
        BigDecimal ageSurcharge = marketValue
                .multiply(BigDecimal.valueOf(extraYears * 0.015));

        // Risk multiplier
        BigDecimal riskMultiplier = getRiskMultiplier(user.getRiskProfile());
        BigDecimal riskLoad = basePremium.multiply(riskMultiplier);

        return basePremium.add(ageSurcharge).add(riskLoad)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRiskMultiplier(User.RiskProfile profile) {
        return switch (profile) {
            case LOW    -> BigDecimal.ZERO;
            case MEDIUM -> BigDecimal.valueOf(0.10);  // +10%
            case HIGH   -> BigDecimal.valueOf(0.25);  // +25%
        };
    }
}
