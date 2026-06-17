package com.user.driven.operations.app.security;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.user.driven.operations.app.core.model.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Service responsible for generating and validating JWT access and refresh tokens.
 * Access tokens expire after 15 minutes; refresh tokens expire after 7 days.
 *
 * @author Jatin Raheja
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret:myDefaultSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration; // 15 minutes in milliseconds

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration; // 7 days in milliseconds

    /**
     * Generates a JWT access token for the given user with role and userId claims.
     *
     * @param user the authenticated user
     * @return a signed JWT access token string
     */
    public String generateAccessToken(AppUser user) {
        return buildToken(user, accessTokenExpiration, Map.of(
                "role", user.getRole(),
                "userId", user.getId(),
                "type", "access"
        ));
    }

    /**
     * Generates a JWT refresh token for the given user.
     *
     * @param user the authenticated user
     * @return a signed JWT refresh token string
     */
    public String generateRefreshToken(AppUser user) {
        return buildToken(user, refreshTokenExpiration, Map.of(
                "userId", user.getId(),
                "type", "refresh"
        ));
    }

    /**
     * Extracts the email (subject) from the given token.
     *
     * @param token the JWT token
     * @return the email stored as the token subject
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates whether the token is valid for the given email.
     * A token is valid if the subject matches and it has not expired.
     *
     * @param token the JWT token
     * @param email the expected email
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks whether the given token has expired.
     *
     * @param token the JWT token
     * @return true if the token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the given token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the token using the provided resolver function.
     *
     * @param token    the JWT token
     * @param resolver a function that extracts a claim from the Claims object
     * @param <T>      the type of the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Parses and validates the token, returning all claims.
     *
     * @param token the JWT token
     * @return the Claims object containing all token claims
     * @throws JwtException if the token is invalid, expired, or malformed
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds a JWT token with the given user, expiration, and extra claims.
     *
     * @param user       the user for whom the token is generated
     * @param expiration the token expiration time in milliseconds
     * @param extraClaims additional claims to include in the token
     * @return the signed JWT token string
     */
    private String buildToken(AppUser user, long expiration, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getEmail())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Derives the HMAC-SHA signing key from the configured secret.
     *
     * @return the SecretKey used for signing and verifying tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
