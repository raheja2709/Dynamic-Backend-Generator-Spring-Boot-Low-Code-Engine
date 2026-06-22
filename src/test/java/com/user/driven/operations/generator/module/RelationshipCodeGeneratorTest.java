package com.user.driven.operations.generator.module;

import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.DtoStrategy;
import com.user.driven.operations.enums.FetchType;
import com.user.driven.operations.enums.RelationshipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RelationshipCodeGeneratorTest {

    private RelationshipCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RelationshipCodeGenerator();
    }

    @Test
    void generateManyToOne_withDefaults() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.MANY_TO_ONE,
                "Department", "department", null, FetchType.LAZY, null);

        String result = generator.generateRelationshipField(rel, "Employee");

        assertTrue(result.contains("@ManyToOne"));
        assertTrue(result.contains("@JoinColumn(name = \"department_id\")"));
        assertTrue(result.contains("private Department department;"));
    }

    @Test
    void generateOneToMany_withCascadeAndOrphanRemoval() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.ONE_TO_MANY,
                "Employee", "employees", "department", FetchType.LAZY, "ALL");
        rel.setOrphanRemoval(true);

        String result = generator.generateRelationshipField(rel, "Department");

        assertTrue(result.contains("@OneToMany("));
        assertTrue(result.contains("mappedBy = \"department\""));
        assertTrue(result.contains("cascade = CascadeType.ALL"));
        assertTrue(result.contains("orphanRemoval = true"));
        assertTrue(result.contains("private List<Employee> employees"));
    }

    @Test
    void generateManyToMany_withJoinTable() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.MANY_TO_MANY,
                "Tag", "tags", null, FetchType.LAZY, "PERSIST,MERGE");
        rel.setJoinTableName("project_tags");
        rel.setJoinColumn("project_id");
        rel.setInverseJoinColumn("tag_id");

        String result = generator.generateRelationshipField(rel, "Project");

        assertTrue(result.contains("@ManyToMany"));
        assertTrue(result.contains("@JoinTable("));
        assertTrue(result.contains("name = \"project_tags\""));
        assertTrue(result.contains("joinColumns = @JoinColumn(name = \"project_id\")"));
        assertTrue(result.contains("inverseJoinColumns = @JoinColumn(name = \"tag_id\")"));
        assertTrue(result.contains("private List<Tag> tags"));
    }

    @Test
    void generateCascade_singleValue() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.MANY_TO_ONE,
                "Category", "category", null, FetchType.EAGER, "ALL");

        String result = generator.generateRelationshipField(rel, "Product");

        assertTrue(result.contains("cascade = CascadeType.ALL"));
        assertFalse(result.contains("{"));
    }

    @Test
    void generateCascade_multipleValues() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.ONE_TO_MANY,
                "Item", "items", "order", FetchType.LAZY, "PERSIST,MERGE,REMOVE");

        String result = generator.generateRelationshipField(rel, "Order");

        assertTrue(result.contains("cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}"));
    }

    @Test
    void generateBidirectional_jsonAnnotations() {
        // ManyToOne (owning side) -> @JsonBackReference
        RelationshipDefinition manyToOne = buildRelationship(RelationshipType.MANY_TO_ONE,
                "Department", "department", null, FetchType.LAZY, null);
        manyToOne.setJsonHandling("BACK_REFERENCE");

        String result = generator.generateRelationshipField(manyToOne, "Employee");
        assertTrue(result.contains("@JsonBackReference"));

        // OneToMany (inverse side) -> @JsonManagedReference
        RelationshipDefinition oneToMany = buildRelationship(RelationshipType.ONE_TO_MANY,
                "Employee", "employees", "department", FetchType.LAZY, null);

        String result2 = generator.generateRelationshipField(oneToMany, "Department");
        assertTrue(result2.contains("@JsonManagedReference"));
    }

    @Test
    void generateUnidirectional_noJsonAnnotations() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.MANY_TO_ONE,
                "Address", "address", null, FetchType.LAZY, null);
        rel.setJsonHandling(null);

        String result = generator.generateRelationshipField(rel, "Customer");

        assertFalse(result.contains("@JsonBackReference"));
        assertFalse(result.contains("@JsonManagedReference"));
    }

    @Test
    void generateImports_manyToOneOnly() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.MANY_TO_ONE,
                "Category", "category", null, FetchType.LAZY, null);

        Set<String> imports = generator.generateImports(List.of(rel));

        assertTrue(imports.contains("jakarta.persistence.ManyToOne"));
        assertTrue(imports.contains("jakarta.persistence.FetchType"));
        assertTrue(imports.contains("jakarta.persistence.JoinColumn"));
        assertFalse(imports.contains("java.util.List"));
    }

    @Test
    void generateImports_oneToManyIncludesList() {
        RelationshipDefinition rel = buildRelationship(RelationshipType.ONE_TO_MANY,
                "Item", "items", "order", FetchType.LAZY, "ALL");

        Set<String> imports = generator.generateImports(List.of(rel));

        assertTrue(imports.contains("jakarta.persistence.OneToMany"));
        assertTrue(imports.contains("jakarta.persistence.CascadeType"));
        assertTrue(imports.contains("java.util.List"));
        assertTrue(imports.contains("java.util.ArrayList"));
    }

    @Test
    void generateImports_emptyList() {
        Set<String> imports = generator.generateImports(List.of());
        assertTrue(imports.isEmpty());
    }

    private RelationshipDefinition buildRelationship(RelationshipType type, String target,
                                                      String fieldName, String mappedBy,
                                                      FetchType fetchType, String cascadeTypes) {
        RelationshipDefinition rel = new RelationshipDefinition();
        rel.setRelationshipType(type);
        rel.setTargetEntity(target);
        rel.setFieldName(fieldName);
        rel.setMappedBy(mappedBy);
        rel.setFetchType(fetchType);
        rel.setCascadeTypes(cascadeTypes);
        rel.setNullable(true);
        rel.setDtoStrategy(DtoStrategy.ID_ONLY);

        // Set entity for reference name generation
        EntityDefinition entity = new EntityDefinition();
        entity.setName("TestEntity");
        rel.setEntity(entity);

        return rel;
    }
}
