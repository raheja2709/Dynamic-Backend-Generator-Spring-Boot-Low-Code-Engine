package com.user.driven.operations.app.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.AppUser;

/**
 * Repository interface for {@link AppUser} entity. Provides methods
 * for performing CRUD operations and custom queries related to application users.
 *
 * @author Jatin Raheja
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Finds a user by their API key.
     *
     * @param apiKey the API key to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<AppUser> findByApiKey(String apiKey);

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

}
