package com.user.driven.operations.generator.module;

import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.FetchType;
import com.user.driven.operations.enums.RelationshipType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates JPA relationship annotations and field declarations for entity classes.
 * Handles all four relationship types (OneToOne, OneToMany, ManyToOne, ManyToMany)
 * with full support for fetch type, cascade, orphan removal, join columns/tables,
 * and bidirectional JSON serialization annotations.
 *
 * @author Jatin Raheja
 */
@Slf4j
@Component
public class RelationshipCodeGenerator {

    private static final String INDENT = "    ";

    /**
     * Generates the complete annotated field declaration for a relationship.
     *
     * @param rel the relationship definition
     * @param entityName the name of the owning entity (used for default mappedBy)
     * @return the generated Java code block for this relationship field
     */
    public String generateRelationshipField(RelationshipDefinition rel, String entityName) {
        log.debug("Generating relationship field: {} -> {} ({})",
                entityName, rel.getTargetEntity(), rel.getRelationshipType());

        StringBuilder sb = new StringBuilder();

        switch (rel.getRelationshipType()) {
            case MANY_TO_ONE -> generateManyToOne(sb, rel);
            case ONE_TO_MANY -> generateOneToMany(sb, rel, entityName);
            case ONE_TO_ONE -> generateOneToOne(sb, rel, entityName);
            case MANY_TO_MANY -> generateManyToMany(sb, rel, entityName);
        }

        return sb.toString();
    }

    /**
     * Generates the set of required imports for all relationships on an entity.
     *
     * @param relationships the list of relationship definitions
     * @return a set of fully-qualified import statements
     */
    public Set<String> generateImports(List<RelationshipDefinition> relationships) {
        Set<String> imports = new LinkedHashSet<>();

        if (relationships == null || relationships.isEmpty()) {
            return imports;
        }

        boolean hasCollection = false;

        for (RelationshipDefinition rel : relationships) {
            // JPA annotation imports
            switch (rel.getRelationshipType()) {
                case MANY_TO_ONE -> imports.add("jakarta.persistence.ManyToOne");
                case ONE_TO_MANY -> {
                    imports.add("jakarta.persistence.OneToMany");
                    hasCollection = true;
                }
                case ONE_TO_ONE -> imports.add("jakarta.persistence.OneToOne");
                case MANY_TO_MANY -> {
                    imports.add("jakarta.persistence.ManyToMany");
                    hasCollection = true;
                }
            }

            // FetchType import (always needed for explicit fetch type)
            if (rel.getFetchType() != null) {
                imports.add("jakarta.persistence.FetchType");
            }

            // CascadeType import
            if (rel.getCascadeTypes() != null && !rel.getCascadeTypes().isBlank()) {
                imports.add("jakarta.persistence.CascadeType");
            }

            // JoinColumn import
            if (needsJoinColumn(rel)) {
                imports.add("jakarta.persistence.JoinColumn");
            }

            // JoinTable import for ManyToMany
            if (rel.getRelationshipType() == RelationshipType.MANY_TO_MANY
                    && rel.getJoinTableName() != null && !rel.getJoinTableName().isBlank()) {
                imports.add("jakarta.persistence.JoinTable");
            }

            // JSON serialization imports
            if (isBidirectional(rel)) {
                if (isOwningSide(rel)) {
                    imports.add("com.fasterxml.jackson.annotation.JsonBackReference");
                } else {
                    imports.add("com.fasterxml.jackson.annotation.JsonManagedReference");
                }
            }
        }

        // Collection imports
        if (hasCollection) {
            imports.add("java.util.List");
            imports.add("java.util.ArrayList");
        }

        return imports;
    }

    // ========== Private generation methods ==========

    private void generateManyToOne(StringBuilder sb, RelationshipDefinition rel) {
        // JSON annotation for bidirectional
        appendJsonAnnotation(sb, rel);

        // @ManyToOne annotation
        sb.append(INDENT).append("@ManyToOne");
        String manyToOneParams = buildManyToOneParams(rel);
        if (!manyToOneParams.isEmpty()) {
            sb.append("(").append(manyToOneParams).append(")");
        }
        sb.append("\n");

        // @JoinColumn
        String joinCol = resolveJoinColumnName(rel);
        sb.append(INDENT).append("@JoinColumn(name = \"").append(joinCol).append("\"");
        if (!rel.isNullable()) {
            sb.append(", nullable = false");
        }
        sb.append(")\n");

        // Field declaration
        sb.append(INDENT).append("private ").append(rel.getTargetEntity())
                .append(" ").append(rel.getFieldName()).append(";\n");
    }

    private void generateOneToMany(StringBuilder sb, RelationshipDefinition rel, String entityName) {
        // JSON annotation for bidirectional
        appendJsonAnnotation(sb, rel);

        // @OneToMany annotation
        sb.append(INDENT).append("@OneToMany(");
        List<String> params = new ArrayList<>();

        // mappedBy
        String mappedBy = rel.getMappedBy() != null && !rel.getMappedBy().isBlank()
                ? rel.getMappedBy()
                : uncapitalize(entityName);
        params.add("mappedBy = \"" + mappedBy + "\"");

        // cascade
        String cascade = buildCascadeParam(rel);
        if (cascade != null) {
            params.add(cascade);
        }

        // orphanRemoval
        if (rel.isOrphanRemoval()) {
            params.add("orphanRemoval = true");
        }

        // fetch
        if (rel.getFetchType() != null) {
            params.add("fetch = FetchType." + rel.getFetchType().name());
        }

        sb.append(String.join(", ", params));
        sb.append(")\n");

        // Field declaration with initialization
        sb.append(INDENT).append("private List<").append(rel.getTargetEntity())
                .append("> ").append(rel.getFieldName()).append(" = new ArrayList<>();\n");
    }

    private void generateOneToOne(StringBuilder sb, RelationshipDefinition rel, String entityName) {
        // JSON annotation for bidirectional
        appendJsonAnnotation(sb, rel);

        // @OneToOne annotation
        sb.append(INDENT).append("@OneToOne");
        List<String> params = new ArrayList<>();

        if (isOwningSide(rel)) {
            // Owning side: has @JoinColumn
            String fetchParam = buildFetchParam(rel);
            if (fetchParam != null) params.add(fetchParam);
            String cascade = buildCascadeParam(rel);
            if (cascade != null) params.add(cascade);
            if (rel.isOrphanRemoval()) params.add("orphanRemoval = true");

            if (!params.isEmpty()) {
                sb.append("(").append(String.join(", ", params)).append(")");
            }
            sb.append("\n");

            // @JoinColumn
            String joinCol = resolveJoinColumnName(rel);
            sb.append(INDENT).append("@JoinColumn(name = \"").append(joinCol).append("\"");
            if (!rel.isNullable()) {
                sb.append(", nullable = false");
            }
            sb.append(")\n");
        } else {
            // Inverse side: has mappedBy
            String mappedBy = rel.getMappedBy() != null && !rel.getMappedBy().isBlank()
                    ? rel.getMappedBy()
                    : uncapitalize(entityName);
            params.add("mappedBy = \"" + mappedBy + "\"");

            String fetchParam = buildFetchParam(rel);
            if (fetchParam != null) params.add(fetchParam);
            String cascade = buildCascadeParam(rel);
            if (cascade != null) params.add(cascade);
            if (rel.isOrphanRemoval()) params.add("orphanRemoval = true");

            sb.append("(").append(String.join(", ", params)).append(")\n");
        }

        // Field declaration
        sb.append(INDENT).append("private ").append(rel.getTargetEntity())
                .append(" ").append(rel.getFieldName()).append(";\n");
    }

    private void generateManyToMany(StringBuilder sb, RelationshipDefinition rel, String entityName) {
        // JSON annotation for bidirectional
        appendJsonAnnotation(sb, rel);

        // @ManyToMany annotation
        sb.append(INDENT).append("@ManyToMany");
        List<String> params = new ArrayList<>();

        if (!isOwningSide(rel) && rel.getMappedBy() != null && !rel.getMappedBy().isBlank()) {
            // Inverse side
            params.add("mappedBy = \"" + rel.getMappedBy() + "\"");
        }

        String fetchParam = buildFetchParam(rel);
        if (fetchParam != null) params.add(fetchParam);
        String cascade = buildCascadeParam(rel);
        if (cascade != null) params.add(cascade);

        if (!params.isEmpty()) {
            sb.append("(").append(String.join(", ", params)).append(")");
        }
        sb.append("\n");

        // @JoinTable for owning side
        if (isOwningSide(rel) && rel.getJoinTableName() != null && !rel.getJoinTableName().isBlank()) {
            sb.append(INDENT).append("@JoinTable(\n");
            sb.append(INDENT).append(INDENT).append("name = \"").append(rel.getJoinTableName()).append("\",\n");

            String joinCol = resolveJoinColumnName(rel);
            sb.append(INDENT).append(INDENT).append("joinColumns = @JoinColumn(name = \"")
                    .append(joinCol).append("\"),\n");

            String inverseJoinCol = rel.getInverseJoinColumn() != null && !rel.getInverseJoinColumn().isBlank()
                    ? rel.getInverseJoinColumn()
                    : uncapitalize(rel.getTargetEntity()) + "_id";
            sb.append(INDENT).append(INDENT).append("inverseJoinColumns = @JoinColumn(name = \"")
                    .append(inverseJoinCol).append("\")\n");

            sb.append(INDENT).append(")\n");
        }

        // Field declaration with initialization
        sb.append(INDENT).append("private List<").append(rel.getTargetEntity())
                .append("> ").append(rel.getFieldName()).append(" = new ArrayList<>();\n");
    }

    // ========== JSON annotation handling ==========

    private void appendJsonAnnotation(StringBuilder sb, RelationshipDefinition rel) {
        if (!isBidirectional(rel)) {
            return;
        }

        String referenceName = buildReferenceName(rel);

        if (isOwningSide(rel)) {
            // Owning side gets @JsonBackReference
            sb.append(INDENT).append("@JsonBackReference(\"").append(referenceName).append("\")\n");
        } else {
            // Non-owning (mappedBy) side gets @JsonManagedReference
            sb.append(INDENT).append("@JsonManagedReference(\"").append(referenceName).append("\")\n");
        }
    }

    /**
     * Determines if a relationship is bidirectional.
     * A relationship is bidirectional if it has mappedBy set,
     * or if it has jsonHandling set to MANAGED_REFERENCE or BACK_REFERENCE.
     */
    private boolean isBidirectional(RelationshipDefinition rel) {
        // OneToMany always implies a bidirectional with ManyToOne
        if (rel.getRelationshipType() == RelationshipType.ONE_TO_MANY) {
            return true;
        }

        // If mappedBy is set, it's the inverse side of a bidirectional
        if (rel.getMappedBy() != null && !rel.getMappedBy().isBlank()) {
            return true;
        }

        // Explicit JSON handling configuration
        String jsonHandling = rel.getJsonHandling();
        return jsonHandling != null
                && (jsonHandling.equals("BACK_REFERENCE") || jsonHandling.equals("MANAGED_REFERENCE"));
    }

    /**
     * Determines if this relationship definition represents the owning side.
     * The owning side is the one with the @JoinColumn (ManyToOne, or OneToOne/ManyToMany without mappedBy).
     */
    private boolean isOwningSide(RelationshipDefinition rel) {
        // ManyToOne is always the owning side
        if (rel.getRelationshipType() == RelationshipType.MANY_TO_ONE) {
            return true;
        }

        // If mappedBy is NOT set, this is the owning side
        return rel.getMappedBy() == null || rel.getMappedBy().isBlank();
    }

    private String buildReferenceName(RelationshipDefinition rel) {
        return rel.getEntity() != null
                ? uncapitalize(rel.getEntity().getName()) + "-" + rel.getFieldName()
                : rel.getFieldName();
    }

    // ========== Parameter building helpers ==========

    private String buildManyToOneParams(RelationshipDefinition rel) {
        List<String> params = new ArrayList<>();

        String fetchParam = buildFetchParam(rel);
        if (fetchParam != null) params.add(fetchParam);

        String cascade = buildCascadeParam(rel);
        if (cascade != null) params.add(cascade);

        if (!rel.isNullable()) {
            params.add("optional = false");
        }

        return String.join(", ", params);
    }

    private String buildFetchParam(RelationshipDefinition rel) {
        if (rel.getFetchType() == null) {
            return null;
        }
        return "fetch = FetchType." + rel.getFetchType().name();
    }

    /**
     * Builds the cascade parameter string. Uses single value format when
     * there is only one cascade type, and array format for multiple types.
     */
    private String buildCascadeParam(RelationshipDefinition rel) {
        if (rel.getCascadeTypes() == null || rel.getCascadeTypes().isBlank()) {
            return null;
        }

        List<String> types = Arrays.stream(rel.getCascadeTypes().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .map(t -> "CascadeType." + t)
                .collect(Collectors.toList());

        if (types.isEmpty()) {
            return null;
        }

        if (types.size() == 1) {
            return "cascade = " + types.get(0);
        }

        return "cascade = {" + String.join(", ", types) + "}";
    }

    // ========== Join column resolution ==========

    private boolean needsJoinColumn(RelationshipDefinition rel) {
        return rel.getRelationshipType() == RelationshipType.MANY_TO_ONE
                || (rel.getRelationshipType() == RelationshipType.ONE_TO_ONE && isOwningSide(rel));
    }

    private String resolveJoinColumnName(RelationshipDefinition rel) {
        if (rel.getJoinColumn() != null && !rel.getJoinColumn().isBlank()) {
            return rel.getJoinColumn();
        }
        // Default: fieldName + "_id"
        return toSnakeCase(rel.getFieldName()) + "_id";
    }

    // ========== Utility methods ==========

    private String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private String toSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) return camelCase;
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
