package com.sagar.security.jwt;

import com.sagar.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * JWT authentication filter that processes Bearer tokens from incoming
 * HTTP requests and establishes the authenticated user's security context.
 *
 * <p>
 * The filter executes once per request and authenticates the request only
 * when a valid JWT access token is present.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Extracts and validates the JWT from the Authorization header.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain security filter chain
     * @throws ServletException when servlet processing fails
     * @throws IOException when request processing fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        /*
         * If there is no Authorization header or it does not contain
         * a Bearer token, continue without attempting authentication.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT from "Bearer <token>".
        final String jwt = authHeader.substring(7);

        try {

            // Extract the user's email from the JWT subject.
            final String email = jwtService.extractEmail(jwt);

            /*
             * Authenticate only when no authentication has already been
             * established for the current request.
             */
            if (email != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                /*
                 * Load the latest user information from the database.
                 * This also provides the user's current authorities and
                 * account status.
                 */
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                /*
                 * Validate the JWT signature, subject and expiration
                 * against the loaded user.
                 */
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    /*
                     * Store the authentication in the SecurityContext.
                     * The remaining application layers can now access
                     * the authenticated user through Spring Security.
                     */
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception ignored) {

            /*
             * Invalid or expired tokens must never establish authentication.
             * The request continues through the filter chain so that
             * Spring Security can handle authorization appropriately.
             */
        }

        filterChain.doFilter(request, response);
    }
}