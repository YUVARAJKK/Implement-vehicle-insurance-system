package com.insurance.dto.response;

import com.insurance.entity.Policy;
import com.insurance.entity.Vehicle;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private Policy.PolicyType policyType;
    private Policy.PolicyStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal premiumAmount;
    private BigDecimal coverageAmount;
    private BigDecimal deductibleAmount;
    private int renewalCount;
    private String adminRemarks;
    private boolean expiringSoon;

    // Embedded vehicle summary
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String vehicleMakeModel;
    private Vehicle.VehicleType vehicleType;

    // Embedded plan summary
    private Long planId;
    private String planName;

    private LocalDateTime createdAt;
}
