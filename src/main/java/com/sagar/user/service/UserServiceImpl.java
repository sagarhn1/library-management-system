    package com.sagar.user.service;

    import com.sagar.auth.payload.RegisterRequest;
    import com.sagar.exception.DuplicateResourceException;
    import com.sagar.exception.ResourceNotFoundException;
    import com.sagar.user.mapper.UserMapper;
    import com.sagar.user.model.AuthProvider;
    import com.sagar.user.model.Role;
    import com.sagar.user.model.User;
    import com.sagar.user.payload.CreateUserRequest;
    import com.sagar.user.payload.UpdateUserRequest;
    import com.sagar.user.payload.UserResponse;
    import com.sagar.user.repository.UserRepository;
    import com.sagar.user.specification.UserSpecification;
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.data.jpa.domain.Specification;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;

        /**
         * Creates a new user account.
         *
         * <p>
         * Email and phone number must be unique. The user's password is
         * BCrypt-encoded before persistence to ensure plain-text passwords
         * are never stored in the database.
         * </p>
         *
         * @param request user creation details
         * @return created user response
         * @throws DuplicateResourceException if email or phone already exists
         */
        @Override
        @Transactional
        public UserResponse createUser(CreateUserRequest request) {

            // Email must be unique across all user accounts.
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(
                        "User with email '" + request.getEmail() + "' already exists"
                );
            }

            // Phone number must be unique when provided.
            if (request.getPhone() != null
                    && userRepository.existsByPhone(request.getPhone())) {

                throw new DuplicateResourceException(
                        "User with phone '" + request.getPhone() + "' already exists"
                );
            }

            // Mapper assigns MEMBER role and LOCAL authentication provider.
            User user = userMapper.toEntity(request);

            user.setPassword(
                    passwordEncoder.encode(user.getPassword())
            );

            User savedUser = userRepository.save(user);

            // Never expose the User entity directly.
            return userMapper.toResponse(savedUser);
        }


        /**
         * Creates a new local authentication user.
         *
         * <p>
         * This method is used specifically by the authentication module during
         * local email/password registration. The password must already be encoded
         * before the user is persisted.
         * </p>
         *
         * @param request local registration details
         * @return created user response
         * @throws DuplicateResourceException if email or phone already exists
         */
        @Override
        @Transactional
        public UserResponse createLocalUser(RegisterRequest request) {

            // Email must be unique across all authentication providers.
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException(
                        "User with email '" + request.getEmail() + "' already exists"
                );
            }

            // Phone is required for local registration and must be unique.
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new DuplicateResourceException(
                        "User with phone '" + request.getPhone() + "' already exists"
                );
            }

            /*
             * Create the user as a LOCAL MEMBER account.
             *
             * Password encoding is handled by AuthServiceImpl before this
             * method is called, so this service never stores a plain-text password.
             */
            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .password(request.getPassword())
                    .role(Role.MEMBER)
                    .authProvider(AuthProvider.LOCAL)
                    .active(true)
                    .build();

            User savedUser = userRepository.save(user);

            return userMapper.toResponse(savedUser);
        }


        /**
         * Retrieves a user by its unique identifier.
         *
         * @param id user identifier
         * @return user response
         * @throws ResourceNotFoundException if the user does not exist
         */
        @Override
        public UserResponse getUserById(Long id) {

            User user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: " + id
                            )
                    );

            return userMapper.toResponse(user);
        }

        /**
         * Retrieves users using dynamic filtering, pagination and sorting.
         *
         * @param role role filter
         * @param active active status filter
         * @param firstName first-name search filter
         * @param lastName last-name search filter
         * @param email email search filter
         * @param page zero-based page number
         * @param size number of records per page
         * @param sortBy field used for sorting
         * @param direction sorting direction
         * @return paginated user responses
         */
        @Override
        public Page<UserResponse> getUsers(
                Role role,
                Boolean active,
                String firstName,
                String lastName,
                String email,
                int page,
                int size,
                String sortBy,
                String direction
        ) {

            // Determine requested sorting direction.
            Sort.Direction sortDirection =
                    direction.equalsIgnoreCase("desc")
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            // Build pagination and sorting configuration.
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by(sortDirection, sortBy)
            );

            /*
             * Combine all optional filters into a single dynamic specification.
             * Null specifications are ignored by Spring Data JPA.
             */
            Specification<User> specification =
                    Specification.allOf(
                            UserSpecification.hasRole(role),
                            UserSpecification.isActive(active),
                            UserSpecification.hasFirstName(firstName),
                            UserSpecification.hasLastName(lastName),
                            UserSpecification.hasEmail(email)
                    );

            /*
             * Execute the filtered and paginated query.
             * Page.map() converts entities to response DTOs while preserving
             * pagination metadata.
             */
            return userRepository
                    .findAll(specification, pageable)
                    .map(userMapper::toResponse);
        }

        /**
         * Updates an existing user's profile and account information.
         *
         * <p>
         * Authentication-specific fields such as password, providerId and
         * authProvider are intentionally not modified here.
         * </p>
         *
         * @param id user identifier
         * @param request updated user details
         * @return updated user response
         * @throws ResourceNotFoundException if the user does not exist
         * @throws DuplicateResourceException if email or phone is already used
         */
        @Override
        @Transactional
        public UserResponse updateUser(
                Long id,
                UpdateUserRequest request
        ) {

            User user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: " + id
                            )
                    );

            // Validate email uniqueness only when the email has changed.
            if (!user.getEmail().equals(request.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {

                throw new DuplicateResourceException(
                        "User with email '" + request.getEmail() + "' already exists"
                );
            }

            /*
             * Validate phone uniqueness only when a phone number is supplied
             *,and it differs from the user's existing phone number.
             */
            if (request.getPhone() != null
                    && !request.getPhone().equals(user.getPhone())
                    && userRepository.existsByPhone(request.getPhone())) {

                throw new DuplicateResourceException(
                        "User with phone '" + request.getPhone() + "' already exists"
                );
            }

            // Apply permitted profile/account changes.
            userMapper.updateEntity(user, request);

            /*
             * The entity is managed by the current transaction, so Hibernate
             * automatically persists the detected changes during commit.
             */
            return userMapper.toResponse(user);
        }

        /**
         * Deletes a user by its identifier.
         *
         * @param id user identifier
         * @throws ResourceNotFoundException if the user does not exist
         */
        @Override
        @Transactional
        public void deleteUser(Long id) {

            User user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: " + id
                            )
                    );

            userRepository.delete(user);
        }
    }