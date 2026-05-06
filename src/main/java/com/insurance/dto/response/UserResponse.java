package com.insurance.dto.response;

import com.insurance.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String licenseNumber;
    private String address;
    private User.Role role;
    private User.RiskProfile riskProfile;
    private boolean active;
    private int claimsCount;
    private LocalDateTime createdAt;
}
