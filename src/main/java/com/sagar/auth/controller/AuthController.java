package com.sagar.auth.controller;

import com.sagar.auth.payload.LoginRequest;
import com.sagar.auth.payload.RefreshTokenRequest;
import com.sagar.auth.payload.RegisterRequest;
import com.sagar.auth.payload.TokenResponse;
import com.sagar.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sagar.auth.payload.LogoutRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

/**
 * REST controller responsible for user authentication.
 *
 * <p>
 * Provides endpoints for local registration, login, and access-token
 * renewal using a refresh token.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new local user account.
     *
     * @param request registration details
     * @return access and refresh tokens
     */
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        TokenResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param request login credentials
     * @return access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    /**
     * Logs out the current user by revoking the provided refresh token.
     *
     * @param request refresh token to revoke
     * @return HTTP 204 when logout is successful
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {

        authService.logout(request);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * Generates a new access-token and refresh-token pair
     * using a valid refresh token.
     *
     * @param request refresh token request
     * @return new access and refresh tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                authService.refreshToken(request)
        );
    }
}