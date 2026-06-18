package com.user.driven.operations.app.config;

import com.user.driven.operations.app.common.util.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates that required environment variables are set when running
 * under the {@code prod} or {@code docker} profile.
 *
 * <p>If any required database variable (DB_URL, DB_USERNAME, DB_PASSWORD)
 * resolves to blank or still contains an unresolved {@code ${...}} placeholder,
 * the application will fail to start with a descriptive error message.</p>
 *
 * @see <a href="../../../../../../.env.example">.env.example</a>
 */
@Component
public class StartupValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupValidator.class);

    private static final List<String> PROFILES_REQUIRING_VALIDATION = List.of("prod", "docker");

    private final Environment environment;

    public StartupValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());

        boolean requiresValidation = activeProfiles.stream()
                .anyMatch(PROFILES_REQUIRING_VALIDATION::contains);

        if (!requiresValidation) {
            log.debug("Skipping environment validation — active profile(s) {} do not require it",
                    activeProfiles);
            return;
        }

        List<String> missingVariables = new ArrayList<>();

        validateVariable("spring.datasource.url", "DB_URL", missingVariables);
        validateVariable("spring.datasource.username", "DB_USERNAME", missingVariables);
        validateVariable("spring.datasource.password", "DB_PASSWORD", missingVariables);

        if (!missingVariables.isEmpty()) {
            String errorMessage = String.format(
                    MessageConstants.ENV_VALIDATION_FAILED,
                    String.join(", ", activeProfiles),
                    String.join(", ", missingVariables)
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        log.info(MessageConstants.ENV_VALIDATION_PASSED, activeProfiles);
    }

    /**
     * Checks whether the resolved property value is present and not an unresolved placeholder.
     *
     * @param propertyKey     the Spring property key (e.g. spring.datasource.url)
     * @param envVariableName the environment variable name shown in errors (e.g. DB_URL)
     * @param missingList     accumulator for missing variable names
     */
    private void validateVariable(String propertyKey, String envVariableName, List<String> missingList) {
        String value = environment.getProperty(propertyKey);

        if (value == null || value.isBlank()) {
            missingList.add(envVariableName);
        } else if (value.contains("${" + envVariableName)) {
            // Property file references the env var but it was never resolved
            missingList.add(envVariableName);
        }
    }
}
