package com.insurance.service;

import com.insurance.dto.request.ClaimRequest;
import com.insurance.dto.response.ClaimResponse;
import com.insurance.entity.*;
import com.insurance.exception.BusinessException;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.exception.UnauthorizedException;
import com.insurance.repository.ClaimRepository;
import com.insurance.repository.PolicyRepository;
import com.insurance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final FraudDetectionService fraudDetectionService;
    private final EmailService emailService;
    private final AuditService auditService;

    @Transactional
    public Claim submitClaim(ClaimRequest request, Long userId) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + request.getPolicyId()));

        if (!policy.getVehicle().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Policy does not belong to requesting user");
        }
        if (!policy.isActive()) {
            throw new BusinessException("Claims can only be filed on active policies");
        }
        if (request.getClaimedAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new BusinessException("Claimed amount exceeds policy coverage");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Claim claim = Claim.builder()
                .claimNumber(generateClaimNumber())
                .incidentDate(request.getIncidentDate())
                .incidentDescription(request.getIncidentDescription())
                .incidentLocation(request.getIncidentLocation())
                .claimedAmount(request.getClaimedAmount())
                .claimType(request.getClaimType())
                .status(Claim.ClaimStatus.SUBMITTED)
                .policy(policy)
                .user(user)
                .build();

        // Run fraud detection asynchronously
        claim = claimRepository.save(claim);
        fraudDetectionService.analyzeClaimForFraud(claim, user);

        userRepository.incrementClaimsCount(userId);
        auditService.log(userId, user.getEmail(), "CLAIM_SUBMITTED", "Claim", claim.getId(), claim.getClaimNumber());
        emailService.sendClaimSubmissionConfirmation(user, claim);
        log.info("Claim submitted: {}", claim.getClaimNumber());

        return claim;
    }

    @Transactional
    public Claim reviewClaim(Long claimId, Long adminId, Claim.ClaimStatus newStatus,
                             BigDecimal approvedAmount, String remarks) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        if (claim.getStatus() != Claim.ClaimStatus.SUBMITTED &&
                claim.getStatus() != Claim.ClaimStatus.UNDER_REVIEW) {
            throw new BusinessException("Claim is not in a reviewable state");
        }

        claim.setStatus(newStatus);
        claim.setReviewedBy(adminId);
        claim.setReviewedAt(java.time.LocalDateTime.now());

        if (newStatus == Claim.ClaimStatus.APPROVED) {
            claim.setApprovedAmount(approvedAmount != null ? approvedAmount : claim.getClaimedAmount());
            claim.setSurveyorRemarks(remarks);
            updateUserRiskProfile(claim.getUser());
        } else if (newStatus == Claim.ClaimStatus.REJECTED) {
            claim.setRejectionReason(remarks);
        }

        claimRepository.save(claim);
        auditService.log(adminId, "admin", "CLAIM_REVIEWED", "Claim", claimId,
                newStatus.name() + " - " + remarks);
        emailService.sendClaimStatusUpdate(claim.getUser(), claim);
        return claim;
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> getUserClaims(Long userId, Pageable pageable) {
        return claimRepository.findByUserId(userId, pageable).map(this::mapToClaimResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> getAllClaims(Pageable pageable) {
        return claimRepository.findAll(pageable).map(this::mapToClaimResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> getClaimsByStatus(Claim.ClaimStatus status, Pageable pageable) {
        return claimRepository.findByStatus(status, pageable).map(this::mapToClaimResponse);
    }

    private void updateUserRiskProfile(User user) {
        int claims = user.getClaimsCount();
        User.RiskProfile newProfile;
        if (claims >= 5) newProfile = User.RiskProfile.HIGH;
        else if (claims >= 2) newProfile = User.RiskProfile.MEDIUM;
        else newProfile = User.RiskProfile.LOW;

        userRepository.updateRiskProfile(user.getId(), newProfile);
    }

    private String generateClaimNumber() {
        return "CLM-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    private ClaimResponse mapToClaimResponse(Claim c) {
        return ClaimResponse.builder()
                .id(c.getId())
                .claimNumber(c.getClaimNumber())
                .incidentDate(c.getIncidentDate())
                .incidentDescription(c.getIncidentDescription())
                .incidentLocation(c.getIncidentLocation())
                .claimedAmount(c.getClaimedAmount())
                .approvedAmount(c.getApprovedAmount())
                .settledAmount(c.getSettledAmount())
                .status(c.getStatus())
                .claimType(c.getClaimType())
                .rejectionReason(c.getRejectionReason())
                .surveyorRemarks(c.getSurveyorRemarks())
                .fraudulent(c.isFraudulent())
                .policyId(c.getPolicy().getId())
                .policyNumber(c.getPolicy().getPolicyNumber())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
