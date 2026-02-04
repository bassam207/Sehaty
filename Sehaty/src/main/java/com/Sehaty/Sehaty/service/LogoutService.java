package com.Sehaty.Sehaty.service;


import com.Sehaty.Sehaty.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

/**
 * Service for handling user logout.
 * Invalidates the user's JWT token.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;


    /**
     * Performs logout logic.
     * Marks the token as expired and revoked in the database.
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param authentication The authentication object.
     */
    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7);

        tokenRepository.findByToken(jwt).ifPresent(storedToken -> {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.saveAndFlush(storedToken);
            log.info("Logout successful for token ending with {}", jwt.substring(jwt.length() - 6));

        });
    }
}
