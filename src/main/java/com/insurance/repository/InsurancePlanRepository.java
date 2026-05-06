package com.insurance.repository;

import com.insurance.entity.InsurancePlan;
import com.insurance.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {

    List<InsurancePlan> findByActiveTrue();

    List<InsurancePlan> findByPlanTypeAndActiveTrue(Policy.PolicyType planType);

    Optional<InsurancePlan> findByPlanCode(String planCode);

    boolean existsByPlanCode(String planCode);
}
