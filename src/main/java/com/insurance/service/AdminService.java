package com.insurance.service;

import com.insurance.dto.response.DashboardResponse;
import com.insurance.entity.Claim;
import com.insurance.entity.Policy;
import com.insurance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalVehicles = vehicleRepository.count();
        long totalPolicies = policyRepository.count();
        long activePolicies = policyRepository.countByStatus(Policy.PolicyStatus.ACTIVE);
        long pendingApprovals = policyRepository.countByStatus(Policy.PolicyStatus.PENDING_APPROVAL);
        long totalClaims = claimRepository.count();
        long pendingClaims = claimRepository.findByStatus(
                Claim.ClaimStatus.SUBMITTED, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();

        // Fraud alerts count
        long fraudAlerts = claimRepository.findAll().stream()
                .filter(Claim::isFraudulent).count();

        // Revenue from payments
        List<Object[]> monthlyData = policyRepository.getMonthlyRevenue(LocalDate.now().getYear());
        List<DashboardResponse.MonthlyRevenue> monthlyRevenue = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Object[] row : monthlyData) {
            int month = ((Number) row[0]).intValue();
            BigDecimal rev = (BigDecimal) row[1];
            totalRevenue = totalRevenue.add(rev);
            monthlyRevenue.add(DashboardResponse.MonthlyRevenue.builder()
                    .month(month)
                    .monthName(Month.of(month).name())
                    .revenue(rev)
                    .build());
        }

        // Policy status breakdown
        Map<String, Long> policyBreakdown = new EnumMap<>(Policy.PolicyStatus.class) {{
            for (Policy.PolicyStatus s : Policy.PolicyStatus.values()) {
                put(s.name(), policyRepository.countByStatus(s));
            }
        }};

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalVehicles(totalVehicles)
                .totalPolicies(totalPolicies)
                .activePolicies(activePolicies)
                .pendingApprovals(pendingApprovals)
                .totalClaims(totalClaims)
                .pendingClaims(pendingClaims)
                .fraudAlerts(fraudAlerts)
                .totalPremiumRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .policyStatusBreakdown(policyBreakdown)
                .build();
    }
}
