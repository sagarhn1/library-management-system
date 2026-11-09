package com.sagar.security.config;

import com.sagar.security.handler.RestAuthenticationEntryPoint;
import com.sagar.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Configures the application's HTTP security rules.
     *
     * <p>
     * JWT is used for stateless API authentication while OAuth2 login
     * is handled through Spring Security's OAuth2 client support.
     * </p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // REST APIs do not require CSRF protection.
                .csrf(csrf -> csrf.disable())

                // Use stateless authentication for REST APIs.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Configure authentication failure handling.
                 *
                 * Unauthenticated requests receive HTTP 401 instead
                 * of the default Spring Security response.
                 */
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                )

                // Configure endpoint authorization.
                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints are publicly accessible.
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/test-payment.html"

                        ).permitAll()

                        // OAuth2 authorization and callback endpoints.
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // All other endpoints require authentication.
                        .anyRequest().authenticated()
                )

                /*
                 * Process JWT authentication before Spring Security's
                 * UsernamePasswordAuthenticationFilter.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Provides BCrypt password hashing.
     *
     * @return password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes Spring Security's AuthenticationManager.
     *
     * @param configuration authentication configuration
     * @return authentication manager
     * @throws Exception if the manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }
}