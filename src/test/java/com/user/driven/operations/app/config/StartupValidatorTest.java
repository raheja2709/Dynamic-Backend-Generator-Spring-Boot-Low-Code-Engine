package com.user.driven.operations.app.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StartupValidatorTest {

    private final ApplicationArguments args = mock(ApplicationArguments.class);

    private Environment mockEnvironment(String[] profiles, String dbUrl, String dbUsername, String dbPassword) {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(profiles);
        when(env.getProperty("spring.datasource.url")).thenReturn(dbUrl);
        when(env.getProperty("spring.datasource.username")).thenReturn(dbUsername);
        when(env.getProperty("spring.datasource.password")).thenReturn(dbPassword);
        return env;
    }

    @Test
    @DisplayName("Should pass validation when all variables are set in prod profile")
    void shouldPassWhenAllVariablesSetInProd() {
        Environment env = mockEnvironment(
                new String[]{"prod"},
                "jdbc:postgresql://localhost:5432/mydb",
                "postgres",
                "secret"
        );
        StartupValidator validator = new StartupValidator(env);

        assertDoesNotThrow(() -> validator.run(args));
    }

    @Test
    @DisplayName("Should pass validation when all variables are set in docker profile")
    void shouldPassWhenAllVariablesSetInDocker() {
        Environment env = mockEnvironment(
                new String[]{"docker"},
                "jdbc:postgresql://db:5432/mydb",
                "postgres",
                "secret"
        );
        StartupValidator validator = new StartupValidator(env);

        assertDoesNotThrow(() -> validator.run(args));
    }

    @Test
    @DisplayName("Should skip validation for dev profile")
    void shouldSkipValidationForDevProfile() {
        Environment env = mockEnvironment(
                new String[]{"dev"},
                null, null, null
        );
        StartupValidator validator = new StartupValidator(env);

        assertDoesNotThrow(() -> validator.run(args));
    }

    @Test
    @DisplayName("Should skip validation when no profile is active")
    void shouldSkipValidationWhenNoProfileActive() {
        Environment env = mockEnvironment(
                new String[]{},
                null, null, null
        );
        StartupValidator validator = new StartupValidator(env);

        assertDoesNotThrow(() -> validator.run(args));
    }

    @Test
    @DisplayName("Should fail when DB_URL is missing in prod profile")
    void shouldFailWhenDbUrlMissingInProd() {
        Environment env = mockEnvironment(
                new String[]{"prod"},
                null,
                "postgres",
                "secret"
        );
        StartupValidator validator = new StartupValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(args));
        assertTrue(ex.getMessage().contains("DB_URL"));
        assertTrue(ex.getMessage().contains("prod"));
    }

    @Test
    @DisplayName("Should fail when DB_USERNAME is blank in docker profile")
    void shouldFailWhenDbUsernameBlankInDocker() {
        Environment env = mockEnvironment(
                new String[]{"docker"},
                "jdbc:postgresql://db:5432/mydb",
                "   ",
                "secret"
        );
        StartupValidator validator = new StartupValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(args));
        assertTrue(ex.getMessage().contains("DB_USERNAME"));
        assertTrue(ex.getMessage().contains("docker"));
    }

    @Test
    @DisplayName("Should fail when DB_PASSWORD is missing in prod profile")
    void shouldFailWhenDbPasswordMissingInProd() {
        Environment env = mockEnvironment(
                new String[]{"prod"},
                "jdbc:postgresql://localhost:5432/mydb",
                "postgres",
                null
        );
        StartupValidator validator = new StartupValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(args));
        assertTrue(ex.getMessage().contains("DB_PASSWORD"));
    }

    @Test
    @DisplayName("Should fail when value contains unresolved placeholder")
    void shouldFailWhenValueContainsUnresolvedPlaceholder() {
        Environment env = mockEnvironment(
                new String[]{"prod"},
                "${DB_URL}",
                "postgres",
                "secret"
        );
        StartupValidator validator = new StartupValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(args));
        assertTrue(ex.getMessage().contains("DB_URL"));
    }

    @Test
    @DisplayName("Should report all missing variables in a single error message")
    void shouldReportAllMissingVariables() {
        Environment env = mockEnvironment(
                new String[]{"prod"},
                null, null, null
        );
        StartupValidator validator = new StartupValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(args));
        assertTrue(ex.getMessage().contains("DB_URL"));
        assertTrue(ex.getMessage().contains("DB_USERNAME"));
        assertTrue(ex.getMessage().contains("DB_PASSWORD"));
    }

    @Test
    @DisplayName("Error message should reference .env.example")
    void errorMessageShouldReferenceEnvExample() {
        Environment env = mockEnvironment(
                new String[]{"prod"},
                null, "postgres", "secret"
        );
        StartupValidator validator = new StartupValidator(env);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> validator.run(args));
        assertTrue(ex.getMessage().contains(".env.example"));
    }
}
