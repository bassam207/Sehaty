package com.Sehaty.Sehaty.repository;

import com.Sehaty.Sehaty.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Token entities.
 * Used for storing and retrieving JWT tokens for authentication and logout.
 */
public interface TokenRepository extends JpaRepository<Token, UUID> {

    /**
     * Finds a token by its string representation.
     *
     * @param token The JWT token string.
     * @return Optional<Token> containing the found token or empty if not found.
     */
    Optional<Token> findByToken(String token);
}
