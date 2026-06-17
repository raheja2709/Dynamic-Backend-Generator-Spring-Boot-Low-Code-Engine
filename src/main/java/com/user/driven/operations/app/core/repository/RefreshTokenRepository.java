package com.user.driven.operations.app.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.driven.operations.app.core.model.RefreshToken;

/**
 * Repository interface for {@link RefreshToken} entity. Provides methods
 * for performing CRUD operations and custom queries related to refresh tokens.
 *
 * @author Jatin Raheja
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its token string value.
     *
     * @param token the token string to search for
     * @return an {@link Optional} containing the refresh token if found, or empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens for a given user.
     *
     * @param userId the ID of the user whose tokens should be deleted
     */
    void deleteByUserId(Long userId);

}
