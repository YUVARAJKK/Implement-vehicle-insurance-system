package com.insurance.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Admin analytics dashboard summary */
@Data
@Builder
public class DashboardResponse {
    private long totalUsers;
    private long totalVehicles;
    private long totalPolicies;
    private long activePolicies;
    private long pendingApprovals;
    private long totalClaims;
    private long pendingClaims;
    private long fraudAlerts;

    private BigDecimal totalPremiumRevenue;
    private BigDecimal totalClaimsSettled;

    private List<MonthlyRevenue> monthlyRevenue;
    private Map<String, Long> policyStatusBreakdown;
    private Map<String, Long> claimTypeBreakdown;
    private Map<String, Long> vehicleTypeBreakdown;

    @Data
    @Builder
    public static class MonthlyRevenue {
        private int month;
        private String monthName;
        private BigDecimal revenue;
    }
}
