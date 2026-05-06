package com.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an insurance policy purchased by a user for a vehicle.
 *
 * State Machine Transitions:
 *   PENDING_APPROVAL → ACTIVE → (EXPIRED | CANCELLED)
 *   ACTIVE → SUSPENDED → ACTIVE (on payment)
 */
@Entity
@Table(
    name = "policies",
    indexes = {
        @Index(name = "idx_policy_number", columnList = "policy_number", unique = true),
        @Index(name = "idx_policy_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_policy_status", columnList = "status"),
        @Index(name = "idx_policy_expiry", columnList = "end_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 30)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.PENDING_APPROVAL;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "premium_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "coverage_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "deductible_amount", precision = 10, scale = 2)
    private BigDecimal deductibleAmount;

    @Column(name = "renewal_count")
    @Builder.Default
    private int renewalCount = 0;

    @Column(name = "admin_remarks", length = 500)
    private String adminRemarks;

    @Column(name = "cancellation_reason", length = 300)
    private String cancellationReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private InsurancePlan insurancePlan;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Claim> claims = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    public enum PolicyType {
        THIRD_PARTY,         // Covers damage to others only
        COMPREHENSIVE,       // Full coverage
        THIRD_PARTY_FIRE_THEFT,
        ZERO_DEPRECIATION    // Premium comprehensive
    }

    public enum PolicyStatus {
        PENDING_APPROVAL,
        ACTIVE,
        EXPIRED,
        CANCELLED,
        SUSPENDED,
        REJECTED
    }

    public boolean isActive() {
        return status == PolicyStatus.ACTIVE && LocalDate.now().isBefore(endDate);
    }

    public boolean isExpiringSoon() {
        return isActive() && endDate.minusDays(30).isBefore(LocalDate.now());
    }
}
