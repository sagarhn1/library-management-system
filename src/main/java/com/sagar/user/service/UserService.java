package com.sagar.user.service;

import com.sagar.auth.payload.RegisterRequest;
import com.sagar.user.model.Role;
import com.sagar.user.payload.CreateUserRequest;
import com.sagar.user.payload.UpdateUserRequest;
import com.sagar.user.payload.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse createLocalUser(RegisterRequest request);

    UserResponse getUserById(Long id);

    /**
     * Retrieves users with optional filtering, pagination, and sorting.
     */
    Page<UserResponse> getUsers(
            Role role,
            Boolean active,
            String firstName,
            String lastName,
            String email,
            int page,
            int size,
            String sortBy,
            String direction
    );

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}