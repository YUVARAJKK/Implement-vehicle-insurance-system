package com.insurance.controller;

import com.insurance.dto.response.ApiResponse;
import com.insurance.dto.response.ClaimResponse;
import com.insurance.dto.response.DashboardResponse;
import com.insurance.dto.response.PolicyResponse;
import com.insurance.entity.Claim;
import com.insurance.entity.InsurancePlan;
import com.insurance.entity.Policy;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.repository.InsurancePlanRepository;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.AdminService;
import com.insurance.service.ClaimService;
import com.insurance.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only endpoints for policy/claim management and analytics")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final PolicyService policyService;
    private final ClaimService claimService;
    private final InsurancePlanRepository planRepository;

    @GetMapping("/dashboard")
    @Operation(summary = "Get analytics dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    // ─── Policy Management ───────────────────────────────────────────────────

    @GetMapping("/policies/pending")
    @Operation(summary = "Get all pending approval policies")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getPendingPolicies(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                policyService.getUserPolicies(0L, pageable)));  // Admin sees all
    }

    @PostMapping("/policies/{id}/approve")
    @Operation(summary = "Approve a policy")
    public ResponseEntity<ApiResponse<Policy>> approvePolicy(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Approved by admin") String remarks,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                policyService.approvePolicy(id, userDetails.getUserId(), remarks),
                "Policy approved"));
    }

    @PostMapping("/policies/{id}/reject")
    @Operation(summary = "Reject a policy")
    public ResponseEntity<ApiResponse<Policy>> rejectPolicy(
            @PathVariable Long id,
            @RequestParam String remarks,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                policyService.rejectPolicy(id, userDetails.getUserId(), remarks),
                "Policy rejected"));
    }

    // ─── Claim Management ────────────────────────────────────────────────────

    @GetMapping("/claims")
    @Operation(summary = "Get all claims with optional status filter")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getAllClaims(
            @RequestParam(required = false) Claim.ClaimStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ClaimResponse> claims = status != null
                ? claimService.getClaimsByStatus(status, pageable)
                : claimService.getAllClaims(pageable);
        return ResponseEntity.ok(ApiResponse.success(claims));
    }

    @PostMapping("/claims/{id}/review")
    @Operation(summary = "Review and approve/reject a claim")
    public ResponseEntity<ApiResponse<Claim>> reviewClaim(
            @PathVariable Long id,
            @RequestParam Claim.ClaimStatus status,
            @RequestParam(required = false) BigDecimal approvedAmount,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Claim claim = claimService.reviewClaim(id, userDetails.getUserId(),
                status, approvedAmount, remarks);
        return ResponseEntity.ok(ApiResponse.success(claim, "Claim reviewed"));
    }

    // ─── Plan Management ─────────────────────────────────────────────────────

    @PostMapping("/plans")
    @Operation(summary = "Create a new insurance plan")
    public ResponseEntity<ApiResponse<InsurancePlan>> createPlan(
            @RequestBody InsurancePlan plan) {
        return ResponseEntity.ok(ApiResponse.success(planRepository.save(plan), "Plan created"));
    }

    @PutMapping("/plans/{id}")
    @Operation(summary = "Update an existing plan")
    public ResponseEntity<ApiResponse<InsurancePlan>> updatePlan(
            @PathVariable Long id,
            @RequestBody InsurancePlan updatedPlan) {
        InsurancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
        updatedPlan.setId(plan.getId());
        return ResponseEntity.ok(ApiResponse.success(planRepository.save(updatedPlan), "Plan updated"));
    }
}
