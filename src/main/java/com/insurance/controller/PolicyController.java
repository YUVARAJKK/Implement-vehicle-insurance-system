package com.insurance.controller;

import com.insurance.dto.request.PolicyRequest;
import com.insurance.dto.response.ApiResponse;
import com.insurance.dto.response.PolicyResponse;
import com.insurance.entity.Policy;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Insurance policy purchase, renewal, cancellation")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @Operation(summary = "Purchase a new insurance policy")
    public ResponseEntity<ApiResponse<Policy>> purchasePolicy(
            @Valid @RequestBody PolicyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Policy policy = policyService.purchasePolicy(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(policy, "Policy submitted for approval"));
    }

    @GetMapping
    @Operation(summary = "Get all policies for the authenticated user")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getMyPolicies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                policyService.getUserPolicies(userDetails.getUserId(), pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicy(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getRole() == com.insurance.entity.User.Role.ADMIN;
        return ResponseEntity.ok(ApiResponse.success(
                policyService.getPolicyById(id, userDetails.getUserId(), isAdmin)));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew an existing policy")
    public ResponseEntity<ApiResponse<Policy>> renewPolicy(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                policyService.renewPolicy(id, userDetails.getUserId()), "Policy renewal submitted"));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an active policy")
    public ResponseEntity<ApiResponse<Void>> cancelPolicy(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        policyService.cancelPolicy(id, userDetails.getUserId(), reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Policy cancelled"));
    }
}
