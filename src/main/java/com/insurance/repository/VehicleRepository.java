package com.insurance.repository;

import com.insurance.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findByOwnerId(Long ownerId, Pageable pageable);

    List<Vehicle> findByOwnerId(Long ownerId);

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByEngineNumber(String engineNumber);

    boolean existsByChassisNumber(String chassisNumber);

    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :ownerId AND v.active = true")
    List<Vehicle> findActiveVehiclesByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :type AND v.active = true")
    Page<Vehicle> findByVehicleType(@Param("type") Vehicle.VehicleType type, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") Long ownerId);
}
