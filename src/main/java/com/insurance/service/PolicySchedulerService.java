package com.insurance.service;

import com.insurance.entity.Policy;
import com.insurance.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler for automated policy lifecycle management.
 * Runs on a configurable cron schedule.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicySchedulerService {

    private final PolicyRepository policyRepository;
    private final EmailService emailService;

    /** Daily at midnight: expire overdue policies */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOverduePolicies() {
        List<Policy> expired = policyRepository.findPoliciesPassedExpiry(LocalDate.now());
        for (Policy policy : expired) {
            policy.setStatus(Policy.PolicyStatus.EXPIRED);
            policyRepository.save(policy);
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} policies", expired.size());
        }
    }

    /** Daily at 8 AM: send 30-day expiry reminders */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpiryReminders() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(30);

        List<Policy> expiring = policyRepository.findExpiringPolicies(today, reminderDate);
        for (Policy policy : expiring) {
            emailService.sendPolicyExpiryReminder(policy.getVehicle().getOwner(), policy);
        }
        if (!expiring.isEmpty()) {
            log.info("Sent {} expiry reminder emails", expiring.size());
        }
    }
}
