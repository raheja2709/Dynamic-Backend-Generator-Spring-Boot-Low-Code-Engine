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
<#-- ListDto always uses ID_ONLY for relationships, check if collections exist -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
    <#if rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
        <#assign needsList = true>
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
 * List DTO for ${entity.name?cap_first} - uses ID_ONLY strategy for all relationships.
 * Suitable for collection endpoints where minimal data is needed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ${entity.name?cap_first}ListDto {

<#-- Regular fields (exclude relationship fields from legacy model) -->
<#list entity.fields as field>
    <#if !(field.relationshipType?? && field.relationshipType?has_content)>
    private ${getJavaType(field.dataType)} ${field.name?uncap_first};
    </#if>
</#list>

<#-- Relationship fields - always ID_ONLY for ListDto -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
    // Relationship references (ID_ONLY)
<#list entity.relationships as rel>
    <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
    private Long ${rel.fieldName}Id;
    <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
    private List<Long> ${rel.fieldName}Ids;
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
