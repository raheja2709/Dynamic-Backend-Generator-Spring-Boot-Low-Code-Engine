package ${project.packageName}.dto;

import jakarta.validation.constraints.*;
<#assign hasDateOrDateTime = false>
<#list entity.fields as field>
    <#if field.dataType == "DATE" || field.dataType == "DATETIME">
        <#assign hasDateOrDateTime = true>
    </#if>
</#list>
<#if hasDateOrDateTime>
import java.time.LocalDateTime;
import java.time.LocalDate;
</#if>
<#assign hasDecimal = false>
<#list entity.fields as field>
    <#if field.dataType == "DECIMAL">
        <#assign hasDecimal = true>
    </#if>
</#list>
<#if hasDecimal>
import java.math.BigDecimal;
</#if>
<#-- Check if we need List import for relationship collections -->
<#assign needsList = false>
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
    <#if rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
        <#assign needsList = true>
    </#if>
</#list>
</#if>
<#if needsList>
import java.util.List;
</#if>
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ${entity.name?cap_first}Dto {

<#-- Regular fields -->
<#list entity.fields as field>
    <#if !(field.relationshipType?? && field.relationshipType?has_content)>
    <#if !field.nullable>
    @NotNull
    </#if>
    <#if field.validationRules?has_content>
    // Validation: ${field.validationRules}
    </#if>
    private ${getJavaType(field.dataType)} ${field.name?uncap_first};

    </#if>
</#list>
<#-- Relationship fields using ID_ONLY strategy by default for DTOs -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
    // ========== Relationship References ==========

<#list entity.relationships as rel>
    <#assign strategy = rel.dtoStrategy.name()>
    <#if strategy == "ID_ONLY">
        <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
    private Long ${rel.fieldName}Id;

        <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
    private List<Long> ${rel.fieldName}Ids;

        </#if>
    <#elseif strategy == "SUMMARY">
        <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
    private Long ${rel.fieldName}Id;
    private String ${rel.fieldName}Label;

        <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
    private List<Long> ${rel.fieldName}Ids;

        </#if>
    <#elseif strategy == "NESTED">
        <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
    private ${rel.targetEntity}Dto ${rel.fieldName};

        <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
    private List<${rel.targetEntity}Dto> ${rel.fieldName};

        </#if>
    <#elseif strategy == "IGNORE">
    <#-- Omit field entirely -->
    </#if>
</#list>
</#if>
}

<#function getJavaType dataType>
    <#switch dataType>
        <#case "STRING">
            <#return "String">
        <#case "INTEGER">
            <#return "Integer">
        <#case "LONG">
            <#return "Long">
        <#case "DOUBLE">
            <#return "Double">
        <#case "FLOAT">
            <#return "Float">
        <#case "BOOLEAN">
            <#return "Boolean">
        <#case "DATE">
            <#return "LocalDate">
        <#case "DATETIME">
            <#return "LocalDateTime">
        <#case "TEXT">
            <#return "String">
        <#case "DECIMAL">
            <#return "BigDecimal">
        <#case "UUID">
            <#return "String">
        <#default>
            <#return "String">
    </#switch>
</#function>
