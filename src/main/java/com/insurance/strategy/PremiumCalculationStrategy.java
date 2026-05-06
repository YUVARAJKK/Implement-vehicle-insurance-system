package com.insurance.strategy;

import com.insurance.entity.InsurancePlan;
import com.insurance.entity.User;
import com.insurance.entity.Vehicle;

import java.math.BigDecimal;

/**
 * Strategy interface for premium calculation.
 * Each policy type implements its own calculation algorithm.
 *
 * Design: Open/Closed Principle — add new strategies without modifying existing ones.
 */
public interface PremiumCalculationStrategy {

    /**
     * Calculates the annual premium based on vehicle, plan, and user risk.
     *
     * @param vehicle  the vehicle being insured
     * @param plan     the chosen insurance plan with base rate
     * @param user     the policyholder (for risk profile weighting)
     * @return         calculated annual premium amount
     */
    BigDecimal calculatePremium(Vehicle vehicle, InsurancePlan plan, User user);
}
