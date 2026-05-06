package com.insurance.strategy.impl;

import com.insurance.entity.InsurancePlan;
import com.insurance.entity.User;
import com.insurance.entity.Vehicle;
import com.insurance.strategy.PremiumCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Comprehensive insurance premium strategy.
 *
 * Covers own damage + third party. Higher base rate.
 * Additional factor: vehicle type modifier.
 */
@Component("comprehensiveStrategy")
public class ComprehensivePremiumStrategy implements PremiumCalculationStrategy {

    @Override
    public BigDecimal calculatePremium(Vehicle vehicle, InsurancePlan plan, User user) {
        BigDecimal marketValue = vehicle.getMarketValue();

        // Base premium
        BigDecimal basePremium = marketValue.multiply(plan.getBasePremiumRate());

        // Vehicle type loading
        BigDecimal typeMultiplier = getVehicleTypeMultiplier(vehicle.getVehicleType());
        BigDecimal typePremium = basePremium.multiply(typeMultiplier);

        // Age depreciation benefit: newer vehicles get a discount
        BigDecimal ageDiscount = getAgeDiscount(vehicle.getVehicleAge(), marketValue);

        // Risk loading
        BigDecimal riskMultiplier = getRiskMultiplier(user.getRiskProfile());
        BigDecimal riskLoad = basePremium.multiply(riskMultiplier);

        return basePremium.add(typePremium).subtract(ageDiscount).add(riskLoad)
                .max(BigDecimal.valueOf(500))  // floor of ₹500
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getVehicleTypeMultiplier(Vehicle.VehicleType type) {
        return switch (type) {
            case TWO_WHEELER        -> BigDecimal.valueOf(0.05);
            case FOUR_WHEELER       -> BigDecimal.valueOf(0.10);
            case COMMERCIAL_VEHICLE -> BigDecimal.valueOf(0.20);
            case HEAVY_VEHICLE      -> BigDecimal.valueOf(0.30);
            case ELECTRIC_VEHICLE   -> BigDecimal.valueOf(0.08);  // green discount
        };
    }

    private BigDecimal getAgeDiscount(int vehicleAge, BigDecimal marketValue) {
        if (vehicleAge <= 1) return marketValue.multiply(BigDecimal.valueOf(0.005)); // 0.5% discount
        if (vehicleAge <= 3) return BigDecimal.ZERO;
        if (vehicleAge <= 5) return marketValue.multiply(BigDecimal.valueOf(-0.005)); // surcharge
        return marketValue.multiply(BigDecimal.valueOf(-0.015)); // older vehicle: higher risk
    }

    private BigDecimal getRiskMultiplier(User.RiskProfile profile) {
        return switch (profile) {
            case LOW    -> BigDecimal.valueOf(0.00);
            case MEDIUM -> BigDecimal.valueOf(0.15);
            case HIGH   -> BigDecimal.valueOf(0.35);
        };
    }
}
