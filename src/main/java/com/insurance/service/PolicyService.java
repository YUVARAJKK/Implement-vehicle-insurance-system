package com.insurance.service;

import com.insurance.dto.request.PolicyRequest;
import com.insurance.dto.response.PolicyResponse;
import com.insurance.entity.*;
import com.insurance.exception.BusinessException;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.exception.UnauthorizedException;
import com.insurance.factory.PremiumStrategyFactory;
import com.insurance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final InsurancePlanRepository planRepository;
    private final UserRepository userRepository;
    private final PremiumStrategyFactory strategyFactory;
    private final EmailService emailService;
    private final AuditService auditService;

    @Transactional
    public Policy purchasePolicy(PolicyRequest request, Long userId) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.getVehicleId()));

        if (!vehicle.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Vehicle does not belong to the requesting user");
        }

        InsurancePlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found: " + request.getPlanId()));

        if (!plan.isActive()) {
            throw new BusinessException("Selected insurance plan is inactive");
        }

        boolean hasActive = policyRepository.findByVehicleId(vehicle.getId()).stream()
                .anyMatch(p -> p.getStatus() == Policy.PolicyStatus.ACTIVE
                            && p.getPolicyType() == request.getPolicyType());
        if (hasActive) {
            throw new BusinessException("Vehicle already has an active " + request.getPolicyType() + " policy");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal premium = strategyFactory.getStrategy(request.getPolicyType())
                .calculatePremium(vehicle, plan, user);

        int months = request.getDurationMonths() != null
                ? request.getDurationMonths() : plan.getValidityMonths();

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .policyType(request.getPolicyType())
                .status(Policy.PolicyStatus.PENDING_APPROVAL)
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusMonths(months))
                .premiumAmount(premium)
                .coverageAmount(plan.getMaxCoverageAmount())
                .deductibleAmount(vehicle.getMarketValue().multiply(plan.getDeductiblePercentage()))
                .vehicle(vehicle)
                .insurancePlan(plan)
                .build();

        policy = policyRepository.save(policy);
        log.info("Policy purchased: {}", policy.getPolicyNumber());
        auditService.log(userId, user.getEmail(), "POLICY_PURCHASED", "Policy", policy.getId(), policy.getPolicyNumber());
        emailService.sendPolicyPurchaseConfirmation(user, policy);
        return policy;
    }

    @Cacheable(value = "policies", key = "#policyId")
    @Transactional(readOnly = true)
    public PolicyResponse getPolicyById(Long policyId, Long userId, boolean isAdmin) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + policyId));

        if (!isAdmin && !policy.getVehicle().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied to policy " + policyId);
        }
        return mapToPolicyResponse(policy);
    }

    @Transactional(readOnly = true)
    public Page<PolicyResponse> getUserPolicies(Long userId, Pageable pageable) {
        return policyRepository.findByVehicleOwnerId(userId, pageable).map(this::mapToPolicyResponse);
    }

    @Transactional
    @CacheEvict(value = "policies", key = "#policyId")
    public Policy renewPolicy(Long policyId, Long userId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if (!policy.getVehicle().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }
        if (policy.getStatus() == Policy.PolicyStatus.CANCELLED) {
            throw new BusinessException("Cancelled policies cannot be renewed");
        }

        LocalDate newStart = policy.getEndDate().isBefore(LocalDate.now()) ? LocalDate.now() : policy.getEndDate();
        policy.setStartDate(newStart);
        policy.setEndDate(newStart.plusMonths(policy.getInsurancePlan().getValidityMonths()));
        policy.setStatus(Policy.PolicyStatus.PENDING_APPROVAL);
        policy.setRenewalCount(policy.getRenewalCount() + 1);

        User user = policy.getVehicle().getOwner();
        auditService.log(userId, user.getEmail(), "POLICY_RENEWED", "Policy", policyId, "Renewal #" + policy.getRenewalCount());
        return policyRepository.save(policy);
    }

    @Transactional
    @CacheEvict(value = "policies", key = "#policyId")
    public void cancelPolicy(Long policyId, Long userId, String reason) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if (!policy.getVehicle().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }
        if (policy.getStatus() != Policy.PolicyStatus.ACTIVE &&
                policy.getStatus() != Policy.PolicyStatus.PENDING_APPROVAL) {
            throw new BusinessException("Policy cannot be cancelled in its current state");
        }

        policy.setStatus(Policy.PolicyStatus.CANCELLED);
        policy.setCancellationReason(reason);
        policyRepository.save(policy);
        User user = policy.getVehicle().getOwner();
        auditService.log(userId, user.getEmail(), "POLICY_CANCELLED", "Policy", policyId, reason);
        emailService.sendPolicyCancellationNotice(user, policy);
    }

    @Transactional
    @CacheEvict(value = "policies", key = "#policyId")
    public Policy approvePolicy(Long policyId, Long adminId, String remarks) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if (policy.getStatus() != Policy.PolicyStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only PENDING_APPROVAL policies can be approved");
        }

        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        policy.setAdminRemarks(remarks);
        policy.setApprovedBy(adminId);
        policy.setApprovedAt(java.time.LocalDateTime.now());
        policyRepository.save(policy);
        emailService.sendPolicyApprovalNotification(policy.getVehicle().getOwner(), policy);
        auditService.log(adminId, "admin", "POLICY_APPROVED", "Policy", policyId, remarks);
        return policy;
    }

    @Transactional
    @CacheEvict(value = "policies", key = "#policyId")
    public Policy rejectPolicy(Long policyId, Long adminId, String remarks) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if (policy.getStatus() != Policy.PolicyStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only PENDING_APPROVAL policies can be rejected");
        }

        policy.setStatus(Policy.PolicyStatus.REJECTED);
        policy.setAdminRemarks(remarks);
        policyRepository.save(policy);
        auditService.log(adminId, "admin", "POLICY_REJECTED", "Policy", policyId, remarks);
        return policy;
    }

    private String generatePolicyNumber() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        String uid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "POL-" + timestamp + "-" + uid;
    }

    private PolicyResponse mapToPolicyResponse(Policy p) {
        return PolicyResponse.builder()
                .id(p.getId())
                .policyNumber(p.getPolicyNumber())
                .policyType(p.getPolicyType())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .premiumAmount(p.getPremiumAmount())
                .coverageAmount(p.getCoverageAmount())
                .deductibleAmount(p.getDeductibleAmount())
                .renewalCount(p.getRenewalCount())
                .adminRemarks(p.getAdminRemarks())
                .expiringSoon(p.isExpiringSoon())
                .vehicleId(p.getVehicle().getId())
                .vehicleRegistrationNumber(p.getVehicle().getRegistrationNumber())
                .vehicleMakeModel(p.getVehicle().getMake() + " " + p.getVehicle().getModel())
                .vehicleType(p.getVehicle().getVehicleType())
                .planId(p.getInsurancePlan() != null ? p.getInsurancePlan().getId() : null)
                .planName(p.getInsurancePlan() != null ? p.getInsurancePlan().getPlanName() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
