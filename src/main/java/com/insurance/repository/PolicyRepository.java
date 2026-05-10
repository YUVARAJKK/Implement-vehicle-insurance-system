package com.insurance.repository;

import com.insurance.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long>,
        JpaSpecificationExecutor<Policy> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    Page<Policy> findByVehicleOwnerId(Long userId, Pageable pageable);

    Page<Policy> findByStatus(Policy.PolicyStatus status, Pageable pageable);

    List<Policy> findByVehicleId(Long vehicleId);

    /** Policies expiring within the next N days — used by scheduler for reminders */
    @Query("""
            SELECT p FROM Policy p
            WHERE p.status = 'ACTIVE'
              AND p.endDate BETWEEN :today AND :reminderDate
            """)
    List<Policy> findExpiringPolicies(@Param("today") LocalDate today,
                                      @Param("reminderDate") LocalDate reminderDate);

    /** Policies already expired but not yet marked as EXPIRED */
    @Query("SELECT p FROM Policy p WHERE p.status = 'ACTIVE' AND p.endDate < :today")
    List<Policy> findPoliciesPassedExpiry(@Param("today") LocalDate today);

    @Query("SELECT COUNT(p) FROM Policy p WHERE p.status = :status")
    long countByStatus(@Param("status") Policy.PolicyStatus status);

    @Query("SELECT p FROM Policy p WHERE p.vehicle.owner.id = :userId AND p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesByUser(@Param("userId") Long userId);

    /** Analytics: revenue per month */
    @Query("""
            SELECT EXTRACT(MONTH FROM p.createdAt) as month,
                   SUM(p.premiumAmount) as revenue
            FROM Policy p
            WHERE p.status IN ('ACTIVE','EXPIRED')
              AND EXTRACT(YEAR FROM p.createdAt) = :year
            GROUP BY EXTRACT(MONTH FROM p.createdAt)
            ORDER BY EXTRACT(MONTH FROM p.createdAt)
            """)
    List<Object[]> getMonthlyRevenue(@Param("year") int year);
}
