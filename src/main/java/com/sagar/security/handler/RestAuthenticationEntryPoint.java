package com.sagar.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles unauthenticated requests to protected REST endpoints.
 *
 * <p>
 * Returns HTTP 401 Unauthorized instead of redirecting the client
 * or returning a generic HTML response.
 * </p>
 */
@Component
public class RestAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    /**
     * Sends a 401 Unauthorized response when authentication is required.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param authException authentication exception
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication is required to access this resource"
        );
    }
}