package com.Sehaty.Sehaty.security;


import com.Sehaty.Sehaty.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for rate limiting requests.
 * Limits requests based on user ID (if authenticated) or IP address.
 */
@Component
@RequiredArgsConstructor

public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Get user identifier (userId from JWT or IP address)
        String identifier = getUserIdentifier(request);

        boolean allowed = rateLimiterService.allowRequest(identifier);

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Gets the user identifier for rate limiting.
     * Prefers user ID from JWT, falls back to IP address.
     *
     * @param request The HTTP request.
     * @return The user identifier string.
     */
    private String getUserIdentifier(HttpServletRequest request) {
        // Try to get userId from JWT token
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String jwt = authHeader.substring(7);
                String userId = jwtUtil.extractUserId(jwt);
                if (userId != null) {
                    return "user:" + userId; // Prefix with "user:" for authenticated users
                }
            } catch (Exception e) {
                // Invalid token - fall through to IP-based rate limiting
            }
        }

        // Fall back to IP address for unauthenticated requests
        return "ip:" + getClientIP(request);
    }

    /**
     * Gets the client's IP address.
     * Considers X-Forwarded-For header for proxies.
     *
     * @param request The HTTP request.
     * @return The client's IP address.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
