package com.insurance.dto.response;

import com.insurance.entity.Claim;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private LocalDate incidentDate;
    private String incidentDescription;
    private String incidentLocation;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal settledAmount;
    private Claim.ClaimStatus status;
    private Claim.ClaimType claimType;
    private String rejectionReason;
    private String surveyorRemarks;
    private boolean fraudulent;
    private Long policyId;
    private String policyNumber;
    private LocalDateTime createdAt;
}
