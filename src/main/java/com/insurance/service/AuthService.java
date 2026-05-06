package com.insurance.service;

import com.insurance.dto.request.LoginRequest;
import com.insurance.dto.request.RegisterRequest;
import com.insurance.dto.response.AuthResponse;
import com.insurance.dto.response.UserResponse;
import com.insurance.entity.User;
import com.insurance.exception.DuplicateResourceException;
import com.insurance.repository.UserRepository;
import com.insurance.security.CustomUserDetails;
import com.insurance.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration, login, and token refresh.
 * Publishes audit events for all auth operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (request.getLicenseNumber() != null &&
                userRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("License number already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .licenseNumber(request.getLicenseNumber())
                .address(request.getAddress())
                .role(User.Role.USER)
                .riskProfile(User.RiskProfile.LOW)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        auditService.log(user.getId(), user.getEmail(), "USER_REGISTERED",
                "User", user.getId(), "New user registration");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return buildAuthResponse(userDetails, user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        log.info("User logged in: {}", userDetails.getUsername());

        auditService.log(userDetails.getUserId(), userDetails.getUsername(),
                "USER_LOGIN", "User", userDetails.getUserId(), "Successful login");

        return buildAuthResponse(userDetails, userDetails.getUser());
    }

    private AuthResponse buildAuthResponse(CustomUserDetails userDetails, User user) {
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getJwtExpiration())
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .licenseNumber(user.getLicenseNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .riskProfile(user.getRiskProfile())
                .active(user.isActive())
                .claimsCount(user.getClaimsCount())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
