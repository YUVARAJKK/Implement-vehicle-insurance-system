package com.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Insurance plan catalog managed by Admin.
 * Plans define coverage types and base premium rates used by the
 * premium calculation Strategy pattern.
 */
@Entity
@Table(
    name = "insurance_plans",
    indexes = {
        @Index(name = "idx_plan_type", columnList = "plan_type"),
        @Index(name = "idx_plan_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "plan_code", unique = true, nullable = false, length = 30)
    private String planCode;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 30)
    private Policy.PolicyType planType;

    @Column(name = "base_premium_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal basePremiumRate;  // e.g. 0.0250 = 2.5% of market value

    @Column(name = "max_coverage_amount", precision = 12, scale = 2)
    private BigDecimal maxCoverageAmount;

    @Column(name = "min_coverage_amount", precision = 12, scale = 2)
    private BigDecimal minCoverageAmount;

    @Column(name = "deductible_percentage", precision = 5, scale = 4)
    private BigDecimal deductiblePercentage;  // % of claimed amount

    @Column(name = "validity_months", nullable = false)
    @Builder.Default
    private int validityMonths = 12;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "features", length = 1000)
    private String features;  // JSON string of plan features
}
