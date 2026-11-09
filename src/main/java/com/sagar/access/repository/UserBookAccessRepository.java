package com.sagar.access.repository;

import com.sagar.access.model.UserBookAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserBookAccessRepository
        extends JpaRepository<UserBookAccess, Long> {

    Optional<UserBookAccess> findByUserIdAndBookId(
            Long userId,
            Long bookId
    );

    List<UserBookAccess>
    findByUserIdAndRevokedFalseAndAccessExpiresAtAfter(
            Long userId,
            LocalDateTime accessExpiresAt
    );
}