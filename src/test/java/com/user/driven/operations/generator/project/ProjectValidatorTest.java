package com.user.driven.operations.generator.project;

import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.FieldDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.enums.DataType;
import com.user.driven.operations.enums.FieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectValidatorTest {

    private ProjectValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProjectValidator();
    }

    @Test
    void validate_validProject_succeeds() {
        ProjectDefinition project = buildValidProject("com.example.myapp");
        assertDoesNotThrow(() -> validator.validate(project));
    }

    @ParameterizedTest
    @ValueSource(strings = {"com.final.app", "com.class.test", "com.import.service", "com.public.api",
            "com.static.util", "com.void.handler", "com.new.factory", "com.return.value"})
    void validate_reservedKeywordInPackage_throwsValidation(String packageName) {
        ProjectDefinition project = buildValidProject(packageName);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(project));
        assertTrue(ex.getMessage().contains("reserved keyword"));
    }

    @Test
    void validate_nullPackageName_throwsValidation() {
        ProjectDefinition project = buildValidProject(null);
        assertThrows(ValidationException.class, () -> validator.validate(project));
    }

    @Test
    void validate_emptyPackageName_throwsValidation() {
        ProjectDefinition project = buildValidProject("");
        assertThrows(ValidationException.class, () -> validator.validate(project));
    }

    @Test
    void validate_invalidPackageFormat_throwsValidation() {
        ProjectDefinition project = buildValidProject("123.invalid");
        assertThrows(ValidationException.class, () -> validator.validate(project));
    }

    @Test
    void validate_noEntities_throwsValidation() {
        ProjectDefinition project = new ProjectDefinition();
        project.setPackageName("com.example.app");
        project.setEntities(new ArrayList<>());
        assertThrows(ValidationException.class, () -> validator.validate(project));
    }

    @Test
    void validate_entityWithoutPK_throwsValidation() {
        ProjectDefinition project = new ProjectDefinition();
        project.setPackageName("com.example.app");
        EntityDefinition entity = new EntityDefinition();
        entity.setName("Product");
        FieldDefinition field = new FieldDefinition();
        field.setName("name");
        field.setDataType(DataType.STRING);
        field.setFieldType(FieldType.NORMAL);
        entity.setFields(List.of(field));
        project.setEntities(List.of(entity));

        assertThrows(ValidationException.class, () -> validator.validate(project));
    }

    @Test
    void validate_entityNameIsReservedKeyword_throwsValidation() {
        ProjectDefinition project = new ProjectDefinition();
        project.setPackageName("com.example.app");
        EntityDefinition entity = new EntityDefinition();
        entity.setName("Class");
        FieldDefinition pk = new FieldDefinition();
        pk.setName("id");
        pk.setDataType(DataType.LONG);
        pk.setFieldType(FieldType.PRIMARY_KEY);
        entity.setFields(List.of(pk));
        project.setEntities(List.of(entity));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(project));
        assertTrue(ex.getMessage().contains("reserved keyword"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"com.example.app", "org.company.service", "io.myproject.backend", "com.test.relapp"})
    void validate_validPackageNames_succeed(String packageName) {
        ProjectDefinition project = buildValidProject(packageName);
        assertDoesNotThrow(() -> validator.validate(project));
    }

    private ProjectDefinition buildValidProject(String packageName) {
        ProjectDefinition project = new ProjectDefinition();
        project.setPackageName(packageName);
        project.setName("TestProject");

        EntityDefinition entity = new EntityDefinition();
        entity.setName("Product");

        FieldDefinition pk = new FieldDefinition();
        pk.setName("id");
        pk.setDataType(DataType.LONG);
        pk.setFieldType(FieldType.PRIMARY_KEY);

        FieldDefinition nameField = new FieldDefinition();
        nameField.setName("name");
        nameField.setDataType(DataType.STRING);
        nameField.setFieldType(FieldType.NORMAL);

        entity.setFields(List.of(pk, nameField));
        project.setEntities(List.of(entity));

        return project;
    }
}
