package com.Sehaty.Sehaty.security;

import com.Sehaty.Sehaty.model.Token;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.TokenRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import  com.Sehaty.Sehaty.security.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter for authenticating requests using JWT.
 * Extracts the token, validates it, and sets the authentication context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userId;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userId = jwtUtil.extractUserId(jwt);

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                User user = userRepository.findById(UUID.fromString(userId))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                Optional<Token> storedToken = tokenRepository.findByToken(jwt);

                boolean isTokenValid = storedToken.isPresent() &&
                        !storedToken.get().isExpired() &&
                        !storedToken.get().isRevoked();

                if (isTokenValid) {
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getId().toString())
                            .password(user.getPassword())
                            .authorities("ROLE_USER")
                            .build();

                    if (jwtUtil.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                log.error("Authentication error: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
