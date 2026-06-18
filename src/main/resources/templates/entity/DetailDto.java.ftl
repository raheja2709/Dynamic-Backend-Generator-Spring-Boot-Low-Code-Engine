package ${project.packageName}.dto;

<#assign hasDateOrDateTime = false>
<#assign hasDecimal = false>
<#assign needsList = false>
<#list entity.fields as field>
    <#if !(field.relationshipType?? && field.relationshipType?has_content)>
        <#if field.dataType == "DATE" || field.dataType == "DATETIME">
            <#assign hasDateOrDateTime = true>
        </#if>
        <#if field.dataType == "DECIMAL">
            <#assign hasDecimal = true>
        </#if>
    </#if>
</#list>
<#-- Check for list imports from relationship strategies -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
    <#assign strategy = rel.dtoStrategy.name()>
    <#if strategy != "IGNORE">
        <#if rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
            <#assign needsList = true>
        </#if>
    </#if>
</#list>
</#if>
<#if hasDateOrDateTime>
import java.time.LocalDate;
import java.time.LocalDateTime;
</#if>
<#if hasDecimal>
import java.math.BigDecimal;
</#if>
<#if needsList>
import java.util.List;
</#if>
import lombok.*;

/**
 * Detail DTO for ${entity.name?cap_first} - uses configured DTO strategy per relationship.
 * Suitable for single-entity detail endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ${entity.name?cap_first}DetailDto {

<#-- Regular fields (exclude relationship fields from legacy model) -->
<#list entity.fields as field>
    <#if !(field.relationshipType?? && field.relationshipType?has_content)>
    private ${getJavaType(field.dataType)} ${field.name?uncap_first};
    </#if>
</#list>

<#-- Relationship fields with configured DTO strategy -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
    // Relationship references (per configured strategy)
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
    private ${rel.targetEntity}SummaryDto ${rel.fieldName};
        <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
    private List<${rel.targetEntity}SummaryDto> ${rel.fieldName};
        </#if>
    <#elseif strategy == "NESTED">
        <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
    private ${rel.targetEntity}DetailDto ${rel.fieldName};
        <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
    private List<${rel.targetEntity}DetailDto> ${rel.fieldName};
        </#if>
    <#elseif strategy == "IGNORE">
    <#-- Omit field entirely -->
    </#if>
</#list>
</#if>
}

<#function getJavaType dataType>
    <#switch dataType>
        <#case "STRING"><#return "String">
        <#case "INTEGER"><#return "Integer">
        <#case "LONG"><#return "Long">
        <#case "DOUBLE"><#return "Double">
        <#case "FLOAT"><#return "Float">
        <#case "BOOLEAN"><#return "Boolean">
        <#case "DATE"><#return "LocalDate">
        <#case "DATETIME"><#return "LocalDateTime">
        <#case "TEXT"><#return "String">
        <#case "DECIMAL"><#return "BigDecimal">
        <#case "UUID"><#return "String">
        <#default><#return "String">
    </#switch>
</#function>
