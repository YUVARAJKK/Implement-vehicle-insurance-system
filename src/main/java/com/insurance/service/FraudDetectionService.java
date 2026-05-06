package com.insurance.service;

import com.insurance.entity.Claim;
import com.insurance.entity.User;
import com.insurance.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fraud Detection Service — analyzes patterns asynchronously.
 *
 * Fraud signals:
 *  1. High claim frequency (>= 3 claims in 90 days)
 *  2. Claimed amount exceeds vehicle market value
 *  3. Consecutive claims on same policy within 30 days
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final ClaimRepository claimRepository;

    @Async
    @Transactional
    public void analyzeClaimForFraud(Claim claim, User user) {
        boolean isFraudulent = false;
        StringBuilder reasons = new StringBuilder();

        // Signal 1: High claim frequency
        LocalDate since = LocalDate.now().minusDays(90);
        List<Object[]> highFrequencyUsers = claimRepository.findUsersWithHighClaimFrequency(since, 3);
        boolean highFreq = highFrequencyUsers.stream()
                .anyMatch(row -> row[0].equals(user.getId()));

        if (highFreq) {
            isFraudulent = true;
            reasons.append("HIGH_CLAIM_FREQUENCY;");
        }

        // Signal 2: Claimed amount >= 90% of vehicle market value
        BigDecimal vehicleValue = claim.getPolicy().getVehicle().getMarketValue();
        if (vehicleValue != null && claim.getClaimedAmount()
                .compareTo(vehicleValue.multiply(BigDecimal.valueOf(0.9))) >= 0) {
            isFraudulent = true;
            reasons.append("EXCESSIVE_CLAIM_AMOUNT;");
        }

        // Signal 3: More than 2 active claims on same policy
        long activeClaims = claimRepository.countActiveClaims(claim.getPolicy().getId());
        if (activeClaims > 2) {
            isFraudulent = true;
            reasons.append("MULTIPLE_CONCURRENT_CLAIMS;");
        }

        if (isFraudulent) {
            claim.setFraudulent(true);
            claim.setFraudReason(reasons.toString());
            claimRepository.save(claim);
            log.warn("Fraud alert on claim {}: {}", claim.getClaimNumber(), reasons);
        }
    }
}
