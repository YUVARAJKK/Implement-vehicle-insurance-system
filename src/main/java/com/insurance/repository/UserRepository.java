package com.insurance.repository;

import com.insurance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLicenseNumber(String licenseNumber);

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    @Modifying
    @Query("UPDATE User u SET u.claimsCount = u.claimsCount + 1 WHERE u.id = :userId")
    void incrementClaimsCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.riskProfile = :profile WHERE u.id = :userId")
    void updateRiskProfile(@Param("userId") Long userId,
                           @Param("profile") User.RiskProfile profile);

    @Query("SELECT u FROM User u WHERE u.active = true AND u.role = :role")
    java.util.List<User> findAllActiveByRole(@Param("role") User.Role role);
}
