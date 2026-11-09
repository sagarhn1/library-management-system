package com.sagar.auth.service;

import com.sagar.auth.payload.*;

public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}