package com.insurance.dto.request;

import com.insurance.entity.Policy;
import com.insurance.entity.Vehicle;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleRequest {

    @NotBlank(message = "Registration number is required")
    @Size(max = 20)
    private String registrationNumber;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Manufacture year is required")
    @Min(value = 1990, message = "Year must be >= 1990")
    @Max(value = 2025, message = "Year must be <= 2025")
    private Integer manufactureYear;

    @NotBlank(message = "Engine number is required")
    private String engineNumber;

    @NotBlank(message = "Chassis number is required")
    private String chassisNumber;

    @NotNull(message = "Vehicle type is required")
    private Vehicle.VehicleType vehicleType;

    private Vehicle.FuelType fuelType;

    private Integer seatingCapacity;

    private Integer cubicCapacity;

    @DecimalMin(value = "10000.00", message = "Market value too low")
    private BigDecimal marketValue;

    private String color;
}
