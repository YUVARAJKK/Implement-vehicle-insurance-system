package com.insurance.dto.response;

import lombok.Builder;
import lombok.Data;

/** Returned after successful login/register. Contains JWT tokens. */
@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;   // milliseconds
    private UserResponse user;
}
