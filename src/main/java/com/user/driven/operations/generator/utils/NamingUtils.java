package com.user.driven.operations.generator.utils;

public final class NamingUtils {

    private NamingUtils() {
    }

    /**
     * Converts raw input into valid Java class name.
     *
     * Examples:
     * demo-app -> DemoApp
     * user_service -> UserService
     * employee management -> EmployeeManagement
     */
    public static String toClassName(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Input cannot be null or blank"
            );
        }

        String[] parts = input.split("[^a-zA-Z0-9]");

        StringBuilder sb = new StringBuilder();

        for (String part : parts) {

            if (part == null || part.isBlank()) {
                continue;
            }

            sb.append(
                    Character.toUpperCase(part.charAt(0))
            );

            if (part.length() > 1) {
                sb.append(
                        part.substring(1).toLowerCase()
                );
            }
        }

        return sb.toString();
    }

    /**
     * Converts raw input into valid Java package name.
     *
     * Examples:
     * demo-app -> demoapp
     * user_service -> userservice
     */
    public static String toPackageName(String input) {

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(
                    "Input cannot be null or blank"
            );
        }

        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    /**
     * Converts raw input into valid Java variable name.
     *
     * Examples:
     * user-service -> userService
     * employee management -> employeeManagement
     */
    public static String toVariableName(String input) {

        String className = toClassName(input);

        return Character.toLowerCase(className.charAt(0))
                + className.substring(1);
    }

    /**
     * Generates Spring Boot application class name.
     *
     * Example:
     * demo-app -> DemoAppApplication
     */
    public static String toApplicationClassName(String projectName) {

        return toClassName(projectName)
                + "Application";
    }
}
