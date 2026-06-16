package com.user.driven.operations.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized application configuration properties bound from the
 * {@code app.*} namespace in application properties/environment variables.
 *
 * <p>Provides injectable access to externalized settings such as the
 * generated-projects output directory and the Maven executable path.</p>
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    /**
     * Directory where generated projects are stored.
     * Bound from {@code app.generated-projects.directory}.
     * Environment variable: APP_GENERATED_PROJECTS_DIR
     */
    private String generatedProjectsDirectory = "./generated-projects";

    /**
     * Path to Maven executable for build verification.
     * Bound from {@code app.maven.executable}.
     * Environment variable: APP_MAVEN_PATH
     */
    private String mavenExecutable = "mvn";
}
