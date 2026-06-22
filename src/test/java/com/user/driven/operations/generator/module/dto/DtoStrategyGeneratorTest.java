package com.user.driven.operations.generator.module.dto;

import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.enums.DtoStrategy;
import com.user.driven.operations.enums.RelationshipType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoStrategyGeneratorTest {

    private final IdOnlyStrategyGenerator idOnly = new IdOnlyStrategyGenerator();
    private final SummaryStrategyGenerator summary = new SummaryStrategyGenerator();
    private final NestedStrategyGenerator nested = new NestedStrategyGenerator();
    private final IgnoreStrategyGenerator ignore = new IgnoreStrategyGenerator();
    private final DtoStrategyFactory factory = new DtoStrategyFactory(idOnly, summary, nested, ignore);

    @Test
    void idOnly_manyToOne_generatesLongField() {
        RelationshipDefinition rel = buildRel(RelationshipType.MANY_TO_ONE, "Department", "department");
        String field = idOnly.generateField(rel);
        assertEquals("    private Long departmentId;", field);
    }

    @Test
    void idOnly_oneToMany_generatesListOfLongs() {
        RelationshipDefinition rel = buildRel(RelationshipType.ONE_TO_MANY, "Employee", "employees");
        String field = idOnly.generateField(rel);
        assertEquals("    private List<Long> employeesIds;", field);
    }

    @Test
    void summary_manyToOne_generatesSummaryDtoField() {
        RelationshipDefinition rel = buildRel(RelationshipType.MANY_TO_ONE, "Department", "department");
        String field = summary.generateField(rel);
        assertEquals("    private DepartmentSummaryDto department;", field);
    }

    @Test
    void nested_oneToMany_generatesListOfDetailDto() {
        RelationshipDefinition rel = buildRel(RelationshipType.ONE_TO_MANY, "OrderItem", "items");
        String field = nested.generateField(rel);
        assertEquals("    private List<OrderItemDetailDto> items;", field);
    }

    @Test
    void ignore_returnsEmptyString() {
        RelationshipDefinition rel = buildRel(RelationshipType.MANY_TO_ONE, "Category", "category");
        String field = ignore.generateField(rel);
        assertEquals("", field);
    }

    @Test
    void factory_resolvesCorrectGenerator() {
        assertInstanceOf(IdOnlyStrategyGenerator.class, factory.getGenerator(DtoStrategy.ID_ONLY));
        assertInstanceOf(SummaryStrategyGenerator.class, factory.getGenerator(DtoStrategy.SUMMARY));
        assertInstanceOf(NestedStrategyGenerator.class, factory.getGenerator(DtoStrategy.NESTED));
        assertInstanceOf(IgnoreStrategyGenerator.class, factory.getGenerator(DtoStrategy.IGNORE));
    }

    @Test
    void factory_nullDefaultsToIdOnly() {
        assertInstanceOf(IdOnlyStrategyGenerator.class, factory.getGenerator(null));
    }

    @Test
    void idOnly_imports_manyToOne_noList() {
        RelationshipDefinition rel = buildRel(RelationshipType.MANY_TO_ONE, "Dept", "dept");
        assertEquals("", idOnly.generateImports(rel));
    }

    @Test
    void idOnly_imports_oneToMany_returnsList() {
        RelationshipDefinition rel = buildRel(RelationshipType.ONE_TO_MANY, "Item", "items");
        assertEquals("java.util.List", idOnly.generateImports(rel));
    }

    private RelationshipDefinition buildRel(RelationshipType type, String target, String fieldName) {
        RelationshipDefinition rel = new RelationshipDefinition();
        rel.setRelationshipType(type);
        rel.setTargetEntity(target);
        rel.setFieldName(fieldName);
        rel.setDtoStrategy(DtoStrategy.ID_ONLY);
        return rel;
    }
}
