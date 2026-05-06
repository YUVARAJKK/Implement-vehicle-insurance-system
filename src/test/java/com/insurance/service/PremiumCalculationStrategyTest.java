package com.insurance.service;

import com.insurance.entity.InsurancePlan;
import com.insurance.entity.Policy;
import com.insurance.entity.User;
import com.insurance.entity.Vehicle;
import com.insurance.strategy.impl.ComprehensivePremiumStrategy;
import com.insurance.strategy.impl.ThirdPartyPremiumStrategy;
import com.insurance.strategy.impl.ZeroDepreciationPremiumStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Premium Calculation Strategy Tests")
class PremiumCalculationStrategyTest {

    private Vehicle vehicle;
    private InsurancePlan plan;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .riskProfile(User.RiskProfile.LOW)
                .build();

        vehicle = Vehicle.builder()
                .id(1L)
                .make("Toyota")
                .model("Corolla")
                .manufactureYear(2022)
                .marketValue(new BigDecimal("800000"))
                .vehicleType(Vehicle.VehicleType.FOUR_WHEELER)
                .fuelType(Vehicle.FuelType.PETROL)
                .build();

        plan = InsurancePlan.builder()
                .id(1L)
                .planName("Basic Third Party")
                .basePremiumRate(new BigDecimal("0.0200"))
                .maxCoverageAmount(new BigDecimal("500000"))
                .deductiblePercentage(new BigDecimal("0.05"))
                .validityMonths(12)
                .build();
    }

    @Test
    @DisplayName("ThirdParty: low risk new vehicle calculates correctly")
    void thirdParty_lowRisk_correctPremium() {
        ThirdPartyPremiumStrategy strategy = new ThirdPartyPremiumStrategy();
        BigDecimal premium = strategy.calculatePremium(vehicle, plan, user);

        // 800000 * 0.02 = 16000 (base), no age surcharge (new vehicle), no risk load
        assertThat(premium).isEqualByComparingTo(new BigDecimal("16000.00"));
    }

    @Test
    @DisplayName("ThirdParty: high risk user gets surcharge")
    void thirdParty_highRisk_increasesPremium() {
        user = User.builder().riskProfile(User.RiskProfile.HIGH).build();
        ThirdPartyPremiumStrategy strategy = new ThirdPartyPremiumStrategy();

        BigDecimal lowRiskPremium = strategy.calculatePremium(vehicle, plan,
                User.builder().riskProfile(User.RiskProfile.LOW).build());
        BigDecimal highRiskPremium = strategy.calculatePremium(vehicle, plan, user);

        assertThat(highRiskPremium).isGreaterThan(lowRiskPremium);
    }

    @Test
    @DisplayName("Comprehensive: premium higher than ThirdParty for same vehicle")
    void comprehensive_greaterThanThirdParty() {
        ThirdPartyPremiumStrategy thirdParty = new ThirdPartyPremiumStrategy();
        ComprehensivePremiumStrategy comprehensive = new ComprehensivePremiumStrategy();

        plan.setBasePremiumRate(new BigDecimal("0.0400"));
        BigDecimal thirdPartyPremium = thirdParty.calculatePremium(vehicle, plan, user);
        BigDecimal comprehensivePremium = comprehensive.calculatePremium(vehicle, plan, user);

        assertThat(comprehensivePremium).isGreaterThan(thirdPartyPremium);
    }

    @Test
    @DisplayName("ZeroDepreciation: rejects vehicles older than 5 years")
    void zeroDep_oldVehicle_throwsException() {
        vehicle = Vehicle.builder()
                .manufactureYear(2015)
                .marketValue(new BigDecimal("400000"))
                .vehicleType(Vehicle.VehicleType.FOUR_WHEELER)
                .build();

        ComprehensivePremiumStrategy comprehensive = new ComprehensivePremiumStrategy();
        ZeroDepreciationPremiumStrategy zeroDep = new ZeroDepreciationPremiumStrategy(comprehensive);

        assertThatThrownBy(() -> zeroDep.calculatePremium(vehicle, plan, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("5 years old");
    }
}
