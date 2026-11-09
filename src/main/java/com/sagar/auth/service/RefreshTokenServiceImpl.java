package com.sagar.auth.service;

import com.sagar.auth.model.RefreshToken;
import com.sagar.auth.repository.RefreshTokenRepository;
import com.sagar.exception.ResourceNotFoundException;
import com.sagar.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Creates a new secure refresh token for the specified user.
     *
     * @param user user for whom the refresh token is created
     * @return persisted refresh token
     */
    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        String token = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(
                        LocalDateTime.now()
                                .plusSeconds(refreshTokenExpiration / 1000)
                )
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verifies that a refresh token exists, has not been revoked,
     * and has not expired.
     *
     * @param token refresh token value
     * @return valid refresh token entity
     * @throws ResourceNotFoundException if the token is invalid,
     *                                   revoked, or expired
     */
    @Override
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invalid refresh token"
                        )
                );

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new ResourceNotFoundException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            refreshToken.setRevoked(true);

            throw new ResourceNotFoundException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    /**
     * Revokes a single refresh token.
     *
     * @param token refresh token to revoke
     */
    @Override
    @Transactional
    public void revokeToken(String token) {

        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    /**
     * Revokes all refresh tokens belonging to a user.
     *
     * @param userId user identifier
     */
    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {

        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Generates a cryptographically secure opaque token.
     *
     * @return Base64 URL-safe random token
     */
    private String generateSecureToken() {

        byte[] randomBytes = new byte[64];

        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {

        refreshTokenRepository.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );
    }

}