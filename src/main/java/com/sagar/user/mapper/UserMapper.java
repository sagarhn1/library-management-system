package com.sagar.user.mapper;

import com.sagar.user.model.AuthProvider;
import com.sagar.user.model.Role;
import com.sagar.user.model.User;
import com.sagar.user.payload.CreateUserRequest;
import com.sagar.user.payload.UpdateUserRequest;
import com.sagar.user.payload.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Handles conversion between User entities and API payloads.
 *
 * <p>
 * The mapper is responsible only for data transformation.
 * Business rules and authentication logic belong in the service/security layers.
 * </p>
 */
@Component
public class UserMapper {

    /**
     * Converts a create-user request into a User entity.
     *
     * <p>
     * Newly registered users are created as LOCAL users with the
     * MEMBER role by default. Privileged roles and authentication
     * providers must never be controlled by the client during registration.
     * </p>
     *
     * @param request user creation request
     * @return User entity
     */
    public User toEntity(CreateUserRequest request) {

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .role(Role.MEMBER)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    /**
     * Updates the editable profile and account fields of an existing user.
     *
     * <p>
     * Authentication-related fields such as password, authProvider,
     * and providerId are intentionally excluded from this operation.
     * </p>
     *
     * @param user existing User entity
     * @param request updated user details
     */
    public void updateEntity(
            User user,
            UpdateUserRequest request
    ) {

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setActive(request.getActive());
    }

    /**
     * Converts a User entity into a safe API response.
     *
     * <p>
     * Sensitive authentication information such as password and
     * providerId is intentionally excluded from the response.
     * </p>
     *
     * @param user User entity
     * @return User response payload
     */
    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}