package com.sagar.user.repository;

import com.sagar.user.model.AuthProvider;
import com.sagar.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderId(
            AuthProvider authProvider,
            String providerId
    );

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}