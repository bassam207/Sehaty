package com.Sehaty.Sehaty.security;


import com.Sehaty.Sehaty.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Utility class for handling JWT operations.
 * Includes token generation, validation, and claim extraction.
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;
    private final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours

    /**
     * Extracts the user ID (UUID) from the token.
     *
     * @param token The JWT token.
     * @return The user ID as a string.
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract any claim from the token.
     *
     * @param token The JWT token.
     * @param claimsResolver A function to resolve the desired claim.
     * @return The extracted claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT.
     *
     * @param token The JWT token.
     * @return The claims object.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the token is expired.
     *
     * @param token The JWT token.
     * @return true if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token The JWT token.
     * @return The expiration date.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Validates the token against user details.
     *
     * @param token The JWT token.
     * @param userDetails The user details.
     * @return true if the token is valid, false otherwise.
     */
    public Boolean isTokenValid(String token, UserDetails userDetails) {
        // Extract UUID from token subject
        String tokenUserId = extractUserId(token);
        // Check if token is expired
        return tokenUserId.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts the email from the token claims.
     *
     * @param token The JWT token.
     * @return The email address.
     */
    public String extractEmail(String token) {
        return extractClaim(token, c -> c.get("email", String.class));
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param user The user for whom to generate the token.
     * @return The generated JWT token.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail()); // optional claim

        return createToken(claims, user.getId().toString()); // subject = UUID
    }

    /**
     * Creates the token with claims and subject (user ID).
     *
     * @param claims The claims to include in the token.
     * @param userId The user ID to set as the subject.
     * @return The created JWT token.
     */
    private String createToken(Map<String, Object> claims, String userId) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId) // store UUID as subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
