package com.insurance.repository;

import com.insurance.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Page<Claim> findByUserId(Long userId, Pageable pageable);

    Page<Claim> findByPolicyId(Long policyId, Pageable pageable);

    Page<Claim> findByStatus(Claim.ClaimStatus status, Pageable pageable);

    List<Claim> findByPolicyId(Long policyId);

    /** Fraud detection: users with >= threshold claims in the past N days */
    @Query("""
            SELECT c.user.id, COUNT(c) as claimCount
            FROM Claim c
            WHERE c.incidentDate >= :since
              AND c.status <> 'WITHDRAWN'
            GROUP BY c.user.id
            HAVING COUNT(c) >= :threshold
            """)
    List<Object[]> findUsersWithHighClaimFrequency(@Param("since") LocalDate since,
                                                    @Param("threshold") long threshold);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.policy.id = :policyId AND c.status NOT IN ('WITHDRAWN','REJECTED')")
    long countActiveClaims(@Param("policyId") Long policyId);

    @Query("SELECT SUM(c.claimedAmount) FROM Claim c WHERE c.user.id = :userId AND c.status = 'APPROVED'")
    java.math.BigDecimal totalApprovedAmountByUser(@Param("userId") Long userId);

    boolean existsByClaimNumber(String claimNumber);
}
