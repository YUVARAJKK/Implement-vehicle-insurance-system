package com.insurance.controller;

import com.insurance.dto.request.VehicleRequest;
import com.insurance.dto.response.ApiResponse;
import com.insurance.entity.Vehicle;
import com.insurance.security.CustomUserDetails;
import com.insurance.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle registration and management")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Register a new vehicle")
    public ResponseEntity<ApiResponse<Vehicle>> registerVehicle(
            @Valid @RequestBody VehicleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Vehicle vehicle = vehicleService.registerVehicle(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vehicle, "Vehicle registered successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles for the authenticated user")
    public ResponseEntity<ApiResponse<Page<Vehicle>>> getMyVehicles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<Vehicle> vehicles = vehicleService.getUserVehicles(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(vehicles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a vehicle by ID")
    public ResponseEntity<ApiResponse<Vehicle>> getVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getRole() == com.insurance.entity.User.Role.ADMIN;
        Vehicle vehicle = vehicleService.getVehicleById(id, userDetails.getUserId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(vehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle details")
    public ResponseEntity<ApiResponse<Vehicle>> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Vehicle vehicle = vehicleService.updateVehicle(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(vehicle, "Vehicle updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a vehicle")
    public ResponseEntity<ApiResponse<Void>> deactivateVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        vehicleService.deactivateVehicle(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Vehicle deactivated"));
    }
}
