package com.user.driven.operations.generator.core;

import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FreemarkerTemplateEngineTest {

    private FreemarkerTemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new FreemarkerTemplateEngine();
    }

    @Test
    void entityTemplate_generatesClassWithFields() {
        ProjectDefinition project = buildProject("com.test.app");
        EntityDefinition entity = buildEntity("Product",
                List.of(
                        buildField("id", DataType.LONG, FieldType.PRIMARY_KEY, false),
                        buildField("name", DataType.STRING, FieldType.NORMAL, false),
                        buildField("price", DataType.DECIMAL, FieldType.NORMAL, true)
                ));

        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);
        model.put("relationshipFields", Collections.emptyList());
        model.put("relationshipImports", Collections.emptySet());

        String result = engine.process("entity/Entity.java.ftl", model);

        assertTrue(result.contains("package com.test.app.model;"));
        assertTrue(result.contains("public class Product"));
        assertTrue(result.contains("@Entity"));
        assertTrue(result.contains("@Id"));
        assertTrue(result.contains("private Long id;"));
        assertTrue(result.contains("private String name;"));
        assertTrue(result.contains("private BigDecimal price;"));
        assertTrue(result.contains("import java.math.BigDecimal;"));
    }

    @Test
    void entityTemplate_generatesDateImports() {
        ProjectDefinition project = buildProject("com.test.app");
        EntityDefinition entity = buildEntity("Event",
                List.of(
                        buildField("id", DataType.LONG, FieldType.PRIMARY_KEY, false),
                        buildField("eventDate", DataType.DATE, FieldType.NORMAL, true),
                        buildField("createdAt", DataType.DATETIME, FieldType.NORMAL, true)
                ));

        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);
        model.put("relationshipFields", Collections.emptyList());
        model.put("relationshipImports", Collections.emptySet());

        String result = engine.process("entity/Entity.java.ftl", model);

        assertTrue(result.contains("import java.time.LocalDate;"));
        assertTrue(result.contains("import java.time.LocalDateTime;"));
        assertTrue(result.contains("private LocalDate eventDate;"));
        assertTrue(result.contains("private LocalDateTime createdAt;"));
    }

    @Test
    void entityTemplate_generatesValidationAnnotations() {
        ProjectDefinition project = buildProject("com.test.app");
        EntityDefinition entity = buildEntity("User",
                List.of(
                        buildField("id", DataType.LONG, FieldType.PRIMARY_KEY, false),
                        buildField("email", DataType.STRING, FieldType.NORMAL, false)
                ));

        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);
        model.put("relationshipFields", Collections.emptyList());
        model.put("relationshipImports", Collections.emptySet());

        String result = engine.process("entity/Entity.java.ftl", model);

        assertTrue(result.contains("@NotNull"));
    }

    @Test
    void dtoTemplate_generatesFieldsWithValidation() {
        ProjectDefinition project = buildProject("com.test.app");
        EntityDefinition entity = buildEntity("Product",
                List.of(
                        buildField("id", DataType.LONG, FieldType.PRIMARY_KEY, false),
                        buildField("name", DataType.STRING, FieldType.NORMAL, false),
                        buildField("active", DataType.BOOLEAN, FieldType.NORMAL, true)
                ));

        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);

        String result = engine.process("entity/Dto.java.ftl", model);

        assertTrue(result.contains("public class ProductDto"));
        assertTrue(result.contains("private String name;"));
        assertTrue(result.contains("private Boolean active;"));
    }

    @Test
    void createRequestTemplate_excludesPrimaryKey() {
        ProjectDefinition project = buildProject("com.test.app");
        EntityDefinition entity = buildEntity("Item",
                List.of(
                        buildField("id", DataType.LONG, FieldType.PRIMARY_KEY, false),
                        buildField("title", DataType.STRING, FieldType.NORMAL, false)
                ));

        Map<String, Object> model = new HashMap<>();
        model.put("project", project);
        model.put("entity", entity);

        String result = engine.process("entity/CreateRequest.java.ftl", model);

        assertTrue(result.contains("public class ItemCreateRequest"));
        assertTrue(result.contains("private String title;"));
        assertFalse(result.contains("private Long id;"));
    }

    private ProjectDefinition buildProject(String packageName) {
        ProjectDefinition project = new ProjectDefinition();
        project.setName("TestProject");
        project.setPackageName(packageName);
        project.setSwaggerEnabled(false);
        project.setCachingEnabled(false);
        return project;
    }

    private EntityDefinition buildEntity(String name, List<FieldDefinition> fields) {
        EntityDefinition entity = new EntityDefinition();
        entity.setName(name);
        entity.setFields(fields);
        entity.setOperations(new ArrayList<>());
        entity.setRelationships(new ArrayList<>());
        return entity;
    }

    private FieldDefinition buildField(String name, DataType dataType, FieldType fieldType, boolean nullable) {
        FieldDefinition field = new FieldDefinition();
        field.setName(name);
        field.setDataType(dataType);
        field.setFieldType(fieldType);
        field.setNullable(nullable);
        return field;
    }
}
