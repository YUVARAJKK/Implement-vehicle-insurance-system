package com.insurance.controller;

import com.insurance.dto.request.ClaimRequest;
import com.insurance.dto.response.ApiResponse;
import com.insurance.dto.response.ClaimResponse;
import com.insurance.entity.Claim;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.ClaimService;
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
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "File and track insurance claims")
@SecurityRequirement(name = "bearerAuth")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @Operation(summary = "Submit a new claim")
    public ResponseEntity<ApiResponse<Claim>> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Claim claim = claimService.submitClaim(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(claim, "Claim submitted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all claims for the authenticated user")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getMyClaims(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                claimService.getUserClaims(userDetails.getUserId(), pageable)));
    }
}
