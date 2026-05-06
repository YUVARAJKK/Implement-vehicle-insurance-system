package com.insurance.dto.request;

import com.insurance.entity.Policy;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PolicyRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Insurance plan ID is required")
    private Long planId;

    @NotNull(message = "Policy type is required")
    private Policy.PolicyType policyType;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    // Duration is driven by the plan's validityMonths; endDate auto-calculated.
    // Optionally override:
    private Integer durationMonths;  // defaults to plan's validityMonths if null
}
