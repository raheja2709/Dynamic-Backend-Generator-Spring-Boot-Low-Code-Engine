package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.FieldType;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validates a ProjectDefinition before code generation.
 * Checks entity structure, primary keys, and package name validity.
 *
 * @author Jatin Raheja
 */
@Component
public class ProjectValidator {

    /**
     * Java reserved keywords that cannot be used as package name segments.
     */
    private static final Set<String> JAVA_RESERVED_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            // Literals that are also reserved
            "true", "false", "null",
            // Module-related (Java 9+)
            "module", "requires", "exports", "opens", "to", "uses", "provides", "with",
            "transitive", "open"
    );

    public void validate(ProjectDefinition project) {

        // Validate package name
        validatePackageName(project.getPackageName());

        // Validate entities exist
        if (project.getEntities() == null || project.getEntities().isEmpty()) {
            throw new ValidationException("At least one entity is required");
        }

        // Validate each entity
        project.getEntities().forEach(entity -> {

            if (entity.getFields() == null || entity.getFields().isEmpty()) {
                throw new ValidationException("Entity must have at least one field: " + entity.getName());
            }

            boolean hasPK = entity.getFields().stream()
                    .anyMatch(f -> f.getFieldType() == FieldType.PRIMARY_KEY);

            if (!hasPK) {
                throw new ValidationException("Primary key is missing in entity: " + entity.getName());
            }

            // Validate entity name is not a Java reserved keyword
            if (JAVA_RESERVED_KEYWORDS.contains(entity.getName().toLowerCase())) {
                throw new ValidationException(
                        "Entity name '" + entity.getName() + "' is a Java reserved keyword");
            }
        });
    }

    /**
     * Validates that the package name doesn't contain Java reserved keywords
     * and follows Java naming conventions.
     */
    private void validatePackageName(String packageName) {
        if (packageName == null || packageName.isBlank()) {
            throw new ValidationException("Package name is required");
        }

        // Check basic format
        if (!packageName.matches("^[a-zA-Z][a-zA-Z0-9]*(\\.[a-zA-Z][a-zA-Z0-9]*)*$")) {
            throw new ValidationException(
                    "Invalid package name '" + packageName + "'. Must follow Java package naming conventions (e.g., com.example.myapp)");
        }

        // Check each segment for reserved keywords
        String[] segments = packageName.split("\\.");
        for (String segment : segments) {
            if (JAVA_RESERVED_KEYWORDS.contains(segment.toLowerCase())) {
                throw new ValidationException(
                        "Package name contains Java reserved keyword '" + segment + "'. "
                                + "Please choose a different package name (e.g., replace '" + segment + "' with '" + segment + "app')");
            }
        }
    }
}
