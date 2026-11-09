package com.sagar.auth.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutRequest {

    /**
     * Refresh token that must be revoked during logout.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}