package com.sagar.auth.service;

import com.sagar.auth.payload.*;
import com.sagar.auth.model.RefreshToken;
import com.sagar.security.jwt.JwtService;
import com.sagar.user.model.User;
import com.sagar.user.repository.UserRepository;
import com.sagar.user.payload.UserResponse;
import com.sagar.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registers a new local user account.
     *
     * <p>
     * The password is encoded using BCrypt before the user is persisted.
     * New self-registered accounts are always created with the MEMBER role
     * and LOCAL authentication provider.
     * </p>
     *
     * @param request registration details
     * @return access and refresh tokens
     */
    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request) {

        /*
         * Encode the password before passing it to the User module.
         * Plain-text passwords must never be persisted.
         */
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        /*
         * Create a copy of the registration request containing the
         * encoded password.
         */
        RegisterRequest encodedRequest = RegisterRequest.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(encodedPassword)
                .build();

        /*
         * UserService creates the user with:
         * - Role.MEMBER
         * - AuthProvider.LOCAL
         */
        UserResponse userResponse =
                userService.createLocalUser(encodedRequest);

        /*
         * Retrieve the persisted entity so JWT and refresh-token
         * generation can use the actual database user.
         */
        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User could not be retrieved after registration"
                        )
                );

        return generateTokens(user);
    }

    /**
     * Authenticates a local user using email and password.
     *
     * @param request login credentials
     * @return access and refresh tokens
     */
    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {

        /*
         * AuthenticationManager delegates credential validation to
         * Spring Security's configured authentication provider.
         */
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        /*
         * Authentication succeeded. Retrieve the application user
         * using the authenticated email.
         */
        User user = userRepository.findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user could not be found"
                        )
                );

        return generateTokens(user);
    }

    /**
     * Generates a new access token using a valid refresh token.
     *
     * @param request refresh token request
     * @return new access and refresh tokens
     */
    @Override
    @Transactional
    public TokenResponse refreshToken(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        User user = refreshToken.getUser();

        /*
         * Token rotation:
         * revoke the old refresh token and issue a new pair.
         */
        refreshTokenService.revokeToken(
                refreshToken.getToken()
        );

        return generateTokens(user);
    }

    /**
     * Generates a new access-token and refresh-token pair.
     *
     * @param user authenticated application user
     * @return token response
     */
    private TokenResponse generateTokens(User user) {

        String accessToken =
                jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    /** * Revokes the specified refresh token. *
     * <p> Once revoked, the token can no longer be used to generate a new access token.</p>
     * *@param token refresh token value
     */
    @Override
    @Transactional
    public void logout(LogoutRequest request) {

        refreshTokenService.revokeToken(
                request.getRefreshToken()
        );
    }


}