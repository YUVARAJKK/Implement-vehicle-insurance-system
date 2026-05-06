package com.insurance.dto.request;

import com.insurance.entity.Claim;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClaimRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    private LocalDate incidentDate;

    @NotBlank(message = "Incident description is required")
    @Size(min = 20, max = 1000, message = "Description must be 20-1000 characters")
    private String incidentDescription;

    @Size(max = 255)
    private String incidentLocation;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(value = "500.00", message = "Minimum claim amount is 500")
    private BigDecimal claimedAmount;

    @NotNull(message = "Claim type is required")
    private Claim.ClaimType claimType;
}
