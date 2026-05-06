package com.insurance.service;

import com.insurance.dto.request.VehicleRequest;
import com.insurance.dto.response.ApiResponse;
import com.insurance.entity.User;
import com.insurance.entity.Vehicle;
import com.insurance.exception.DuplicateResourceException;
import com.insurance.exception.ResourceNotFoundException;
import com.insurance.exception.UnauthorizedException;
import com.insurance.repository.UserRepository;
import com.insurance.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vehicle management service.
 * Users can register multiple vehicles; each undergoes duplicate checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public Vehicle registerVehicle(VehicleRequest request, Long ownerId) {
        validateUniqueness(request);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerId));

        // Builder pattern for Vehicle construction
        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(request.getRegistrationNumber().toUpperCase())
                .make(request.getMake())
                .model(request.getModel())
                .manufactureYear(request.getManufactureYear())
                .engineNumber(request.getEngineNumber())
                .chassisNumber(request.getChassisNumber())
                .vehicleType(request.getVehicleType())
                .fuelType(request.getFuelType())
                .seatingCapacity(request.getSeatingCapacity())
                .cubicCapacity(request.getCubicCapacity())
                .marketValue(request.getMarketValue())
                .color(request.getColor())
                .owner(owner)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle registered: {} by user {}", vehicle.getRegistrationNumber(), ownerId);
        auditService.log(ownerId, owner.getEmail(), "VEHICLE_REGISTERED",
                "Vehicle", vehicle.getId(), "Reg: " + vehicle.getRegistrationNumber());

        return vehicle;
    }

    @Transactional(readOnly = true)
    public Page<Vehicle> getUserVehicles(Long userId, Pageable pageable) {
        return vehicleRepository.findByOwnerId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long vehicleId, Long requestingUserId, boolean isAdmin) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (!isAdmin && !vehicle.getOwner().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("Access denied to vehicle " + vehicleId);
        }
        return vehicle;
    }

    @Transactional
    public Vehicle updateVehicle(Long vehicleId, VehicleRequest request, Long requestingUserId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (!vehicle.getOwner().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("Access denied");
        }

        vehicle.setColor(request.getColor());
        vehicle.setMarketValue(request.getMarketValue());
        vehicle.setSeatingCapacity(request.getSeatingCapacity());

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deactivateVehicle(Long vehicleId, Long requestingUserId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        if (!vehicle.getOwner().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("Access denied");
        }

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
        log.info("Vehicle deactivated: {}", vehicleId);
    }

    private void validateUniqueness(VehicleRequest request) {
        if (vehicleRepository.existsByRegistrationNumber(
                request.getRegistrationNumber().toUpperCase())) {
            throw new DuplicateResourceException(
                "Registration number already exists: " + request.getRegistrationNumber());
        }
        if (vehicleRepository.existsByEngineNumber(request.getEngineNumber())) {
            throw new DuplicateResourceException("Engine number already registered");
        }
        if (vehicleRepository.existsByChassisNumber(request.getChassisNumber())) {
            throw new DuplicateResourceException("Chassis number already registered");
        }
    }
}
