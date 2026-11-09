package com.sagar.auth.repository;

import com.sagar.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    boolean existsByToken(String token);

    void deleteByUserId(Long userId);

    @Transactional
    @Modifying
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}