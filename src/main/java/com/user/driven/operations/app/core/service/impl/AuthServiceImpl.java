package com.user.driven.operations.app.core.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.driven.operations.app.api.dto.AuthResponse;
import com.user.driven.operations.app.api.dto.LoginRequest;
import com.user.driven.operations.app.api.dto.RegisterRequest;
import com.user.driven.operations.app.api.dto.UserResponse;
import com.user.driven.operations.app.common.exception.AuthenticationFailedException;
import com.user.driven.operations.app.common.exception.DuplicateNameException;
import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.common.util.MessageConstants;
import com.user.driven.operations.app.core.model.AppUser;
import com.user.driven.operations.app.core.model.RefreshToken;
import com.user.driven.operations.app.core.repository.AppUserRepository;
import com.user.driven.operations.app.core.repository.RefreshTokenRepository;
import com.user.driven.operations.app.core.service.AuthService;
import com.user.driven.operations.app.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of {@link AuthService} handling user registration and login.
 * Validates password strength, hashes passwords with BCrypt, and issues JWT tokens.
 *
 * @author Jatin Raheja
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.access-token-expiration:900000}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.debug("Processing registration request for email: {}", request.email());

        validatePassword(request.password());

        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateNameException("User", request.email());
        }

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole("USER");
        user.setEnabled(true);
        user.setLocked(false);

        AppUser savedUser = appUserRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Processing login request for email: {}", request.email());

        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login attempt with non-existent email: {}", request.email());
                    return new AuthenticationFailedException(MessageConstants.INVALID_CREDENTIALS);
                });

        if (!user.isEnabled()) {
            log.warn("Login attempt for disabled account: {}", request.email());
            throw new AuthenticationFailedException(MessageConstants.ACCOUNT_DISABLED);
        }

        if (user.isLocked()) {
            log.warn("Login attempt for locked account: {}", request.email());
            throw new AuthenticationFailedException(MessageConstants.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login attempt with invalid password for: {}", request.email());
            throw new AuthenticationFailedException(MessageConstants.INVALID_CREDENTIALS);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Store refresh token
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);

        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        appUserRepository.save(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getEmail(),
                user.getRole(),
                accessTokenExpirationMs / 1000
        );
    }

    /**
     * Validates password against strength rules:
     * - 8-128 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * - At least 1 special character
     *
     * @param password the password to validate
     * @throws ValidationException if any rules are violated
     */
    private void validatePassword(String password) {
        List<String> violations = new ArrayList<>();

        if (password.length() < 8) {
            violations.add(MessageConstants.PASSWORD_TOO_SHORT);
        }
        if (password.length() > 128) {
            violations.add(MessageConstants.PASSWORD_TOO_LONG);
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add(MessageConstants.PASSWORD_MISSING_UPPERCASE);
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add(MessageConstants.PASSWORD_MISSING_LOWERCASE);
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add(MessageConstants.PASSWORD_MISSING_DIGIT);
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add(MessageConstants.PASSWORD_MISSING_SPECIAL);
        }

        if (!violations.isEmpty()) {
            throw new ValidationException(String.join("; ", violations));
        }
    }
}
