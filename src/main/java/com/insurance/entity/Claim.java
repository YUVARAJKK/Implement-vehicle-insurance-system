package com.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents an insurance claim filed by a user against an active policy.
 *
 * Workflow:
 *   SUBMITTED → UNDER_REVIEW → APPROVED / REJECTED
 *   APPROVED → SETTLED (after payment disbursement)
 */
@Entity
@Table(
    name = "claims",
    indexes = {
        @Index(name = "idx_claim_number", columnList = "claim_number", unique = true),
        @Index(name = "idx_claim_policy", columnList = "policy_id"),
        @Index(name = "idx_claim_status", columnList = "status"),
        @Index(name = "idx_claim_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 30)
    private String claimNumber;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "incident_description", nullable = false, length = 1000)
    private String incidentDescription;

    @Column(name = "incident_location", length = 255)
    private String incidentLocation;

    @Column(name = "claimed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "settled_amount", precision = 12, scale = 2)
    private BigDecimal settledAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", length = 30)
    private ClaimType claimType;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "surveyor_remarks", length = 500)
    private String surveyorRemarks;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private java.time.LocalDateTime reviewedAt;

    @Column(name = "is_fraudulent")
    @Builder.Default
    private boolean fraudulent = false;

    @Column(name = "fraud_reason", length = 300)
    private String fraudReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum ClaimStatus {
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        SETTLED,
        WITHDRAWN
    }

    public enum ClaimType {
        ACCIDENT,
        THEFT,
        FIRE,
        NATURAL_DISASTER,
        VANDALISM,
        THIRD_PARTY_DAMAGE,
        MEDICAL
    }
}
