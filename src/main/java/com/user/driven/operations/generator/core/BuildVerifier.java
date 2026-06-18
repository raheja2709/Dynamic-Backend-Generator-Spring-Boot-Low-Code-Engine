package com.user.driven.operations.generator.core;

import java.io.File;
import java.io.IOException;

import com.user.driven.operations.app.common.exception.BuildVerificationException;
import com.user.driven.operations.app.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BuildVerifier {

    private static final Logger log = LoggerFactory.getLogger(BuildVerifier.class);

    private final AppProperties appProperties;

    public BuildVerifier(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void verify(String projectPath) {

        File projectDir = new File(projectPath);

        try {
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase()
                    .contains("win");

            File mvnwFile = new File(projectDir,
                    isWindows ? "mvnw.cmd" : "mvnw");

            String command;

            // Prefer Maven Wrapper if present in the generated project
            if (mvnwFile.exists()) {
                command = isWindows ? "mvnw.cmd" : "./mvnw";
                log.info("Using Maven Wrapper for build verification: {}", command);
            } else {
                // Fallback to configured Maven executable from AppProperties
                command = appProperties.getMavenExecutable();
                if (isWindows && !command.endsWith(".cmd") && !command.endsWith(".bat") && !command.contains("\\")) {
                    command = command + ".cmd";
                }
                log.info("No Maven Wrapper found, using configured Maven executable: {}", command);
            }

            ProcessBuilder pb;
            if (isWindows) {
                pb = new ProcessBuilder("cmd", "/c", command, "clean", "compile", "-q");
            } else {
                pb = new ProcessBuilder(command, "clean", "compile", "-q");
            }

            pb.directory(projectDir);
            pb.inheritIO();

            Process process = pb.start();
            int exit = process.waitFor();

            if (exit != 0) {
                throw new BuildVerificationException(
                        projectDir.getName(), "COMPILATION",
                        new RuntimeException("Maven compile exited with code: " + exit));
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Build verification failed for project at path: {}", projectPath, e);
            throw new BuildVerificationException(projectDir.getName(), "BUILD_VERIFICATION", e);
        }
    }
}
