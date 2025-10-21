package com.Sehaty.Sehaty.repository;

import com.Sehaty.Sehaty.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing User entities.
 *
 * Extends JpaRepository to provide built-in CRUD operations.
 * You can also define custom queries if needed.
 */

@Repository

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email address.
     *
     * @param email The email address of the user.
     * @return Optional<User> containing the found user or empty if not found.
     */


    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email already exists.
     *
     * @param email The email address to check.
     * @return true if a user exists with the given email, false otherwise.
     */

    boolean existsByEmail(String email);
}
