package com.sagar.auth.service;

import com.sagar.auth.model.RefreshToken;
import com.sagar.user.model.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeToken(String token);

    void revokeAllUserTokens(Long userId);

    void deleteExpiredTokens();
}