package com.insurance.service;

import com.insurance.entity.Claim;
import com.insurance.entity.Policy;
import com.insurance.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email notification service.
 * All methods are @Async — email sending never blocks the main request thread.
 * Falls back to logging if mail sending fails (non-critical path).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendPolicyPurchaseConfirmation(User user, Policy policy) {
        String subject = "Policy Purchase Confirmation - " + policy.getPolicyNumber();
        String body = String.format("""
                Dear %s,
                
                Your insurance policy has been submitted for approval.
                
                Policy Number : %s
                Policy Type   : %s
                Vehicle       : %s
                Premium       : ₹%.2f
                Coverage      : ₹%.2f
                Start Date    : %s
                End Date      : %s
                
                Our team will review and approve within 24-48 hours.
                
                Regards,
                Vehicle Insurance Team
                """,
                user.getFullName(),
                policy.getPolicyNumber(),
                policy.getPolicyType(),
                policy.getVehicle().getRegistrationNumber(),
                policy.getPremiumAmount(),
                policy.getCoverageAmount(),
                policy.getStartDate(),
                policy.getEndDate()
        );
        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendPolicyApprovalNotification(User user, Policy policy) {
        String subject = "Policy Approved - " + policy.getPolicyNumber();
        String body = String.format("""
                Dear %s,
                
                Your policy %s has been APPROVED and is now ACTIVE.
                
                Coverage Period: %s to %s
                
                Regards,
                Vehicle Insurance Team
                """,
                user.getFullName(), policy.getPolicyNumber(),
                policy.getStartDate(), policy.getEndDate());
        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendPolicyCancellationNotice(User user, Policy policy) {
        String subject = "Policy Cancelled - " + policy.getPolicyNumber();
        String body = String.format("Dear %s, your policy %s has been cancelled. Reason: %s",
                user.getFullName(), policy.getPolicyNumber(), policy.getCancellationReason());
        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendPolicyExpiryReminder(User user, Policy policy) {
        String subject = "Policy Expiry Reminder - " + policy.getPolicyNumber();
        String body = String.format("""
                Dear %s,
                
                Your policy %s is expiring on %s.
                Please renew it to avoid a coverage gap.
                
                Regards,
                Vehicle Insurance Team
                """,
                user.getFullName(), policy.getPolicyNumber(), policy.getEndDate());
        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendClaimSubmissionConfirmation(User user, Claim claim) {
        String subject = "Claim Submitted - " + claim.getClaimNumber();
        String body = String.format("Dear %s, your claim %s for ₹%.2f has been submitted for review.",
                user.getFullName(), claim.getClaimNumber(), claim.getClaimedAmount());
        sendEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendClaimStatusUpdate(User user, Claim claim) {
        String subject = "Claim Status Update - " + claim.getClaimNumber();
        String body = String.format("Dear %s, your claim %s status has been updated to: %s",
                user.getFullName(), claim.getClaimNumber(), claim.getStatus());
        sendEmail(user.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
            // Non-critical — do not rethrow
        }
    }
}
