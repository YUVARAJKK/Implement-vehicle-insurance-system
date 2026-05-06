package com.insurance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vehicle registered by a user.
 * The vehicle's type, age, and condition affect premium calculations.
 *
 * Uses Builder pattern for construction.
 */
@Entity
@Table(
    name = "vehicles",
    indexes = {
        @Index(name = "idx_vehicle_plate", columnList = "registration_number", unique = true),
        @Index(name = "idx_vehicle_owner", columnList = "owner_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Column(nullable = false, length = 50)
    private String make;           // e.g., Toyota

    @Column(nullable = false, length = 50)
    private String model;          // e.g., Corolla

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "engine_number", unique = true, length = 30)
    private String engineNumber;

    @Column(name = "chassis_number", unique = true, length = 30)
    private String chassisNumber;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(name = "cubic_capacity")
    private Integer cubicCapacity;   // engine cc

    @Column(name = "market_value", precision = 12, scale = 2)
    private BigDecimal marketValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Policy> policies = new ArrayList<>();

    public enum VehicleType {
        TWO_WHEELER, FOUR_WHEELER, COMMERCIAL_VEHICLE, HEAVY_VEHICLE, ELECTRIC_VEHICLE
    }

    public enum FuelType {
        PETROL, DIESEL, CNG, ELECTRIC, HYBRID
    }

    /** Calculated field: vehicle age in years */
    public int getVehicleAge() {
        return java.time.LocalDate.now().getYear() - manufactureYear;
    }
}
