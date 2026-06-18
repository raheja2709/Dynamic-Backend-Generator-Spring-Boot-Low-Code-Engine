package ${project.packageName}.model;

import jakarta.persistence.*;
<#-- Import target entity types from legacy field-based relationships -->
<#list entity.fields as field>
<#if field.relationshipType?? && field.relationshipType?has_content>
import ${project.packageName}.model.${field.relationshipTarget};
</#if>
</#list>
<#-- Import target entity types from new relationship definitions -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
import ${project.packageName}.model.${rel.targetEntity};
</#list>
</#if>
<#-- Dynamic relationship imports -->
<#if relationshipImports??>
<#list relationshipImports as imp>
import ${imp};
</#list>
</#if>
<#-- Determine if collections are needed (legacy or new) -->
<#assign hasCollection = false>
<#list entity.fields as field>
    <#if field.relationshipType?? && field.relationshipType?has_content>
        <#if field.relationshipType == "ONE_TO_MANY" || field.relationshipType == "MANY_TO_MANY">
            <#assign hasCollection = true>
        </#if>
    </#if>
</#list>
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
    <#if rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
        <#assign hasCollection = true>
    </#if>
</#list>
</#if>
<#if hasCollection && !(relationshipImports?? && relationshipImports?seq_contains("java.util.List"))>
import java.util.List;
import java.util.ArrayList;
</#if>
import jakarta.validation.constraints.*;
import lombok.*;

<#-- Determine required imports for field types -->
<#assign hasDateOrDateTime = false>
<#assign hasDecimal = false>
<#list entity.fields as field>
    <#if field.dataType == "DATE" || field.dataType == "DATETIME">
        <#assign hasDateOrDateTime = true>
    </#if>
    <#if field.dataType == "DECIMAL">
        <#assign hasDecimal = true>
    </#if>
</#list>
<#if hasDateOrDateTime>
import java.time.LocalDate;
import java.time.LocalDateTime;
</#if>
<#if hasDecimal>
import java.math.BigDecimal;
</#if>

@Entity
@Table(name = "${entity.name?lower_case?replace(" ", "_")}s")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ${entity.name?cap_first} {

<#-- Generate regular fields -->
<#list entity.fields as field>
    <#assign isPrimaryKey = field.fieldType == "PRIMARY_KEY">
    <#assign minVal = "" >
    <#assign maxVal = "" >
    <#assign hasEmail = false >
    <#assign needsSize = field.dataType == "STRING" || field.dataType == "TEXT">
    <#assign needsMinMax = field.dataType?matches("INTEGER|LONG|DECIMAL|FLOAT|DOUBLE")>

    <#if field.validationRules?has_content>
        <#list field.validationRules?split(",") as rule>
            <#assign rule = rule?trim>
            <#if rule == "email">
                <#assign hasEmail = true >
            <#elseif rule?starts_with("min=")>
                <#assign minVal = rule?substring(4)>
            <#elseif rule?starts_with("max=")>
                <#assign maxVal = rule?substring(4)>
            </#if>
        </#list>
    </#if>

    <#-- Annotations -->
    <#if isPrimaryKey>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    <#elseif field.fieldType == "UNIQUE_FIELD">
    @Column(unique = true)
    </#if>

    <#-- Apply validation only for normal fields, not legacy relationships -->
    <#if !(field.relationshipType?? && field.relationshipType?has_content)>

        <#-- @NotNull if not nullable and not a primary key -->
        <#if !field.nullable && !isPrimaryKey>
    @NotNull
        </#if>

        <#-- @Email -->
        <#if hasEmail>
    @Email
        </#if>

        <#-- @Size -->
        <#if needsSize && (minVal != "" || maxVal != "")>
    @Size(
            <#if minVal != "">min = ${minVal}</#if>
            <#if minVal != "" && maxVal != "">, </#if>
            <#if maxVal != "">max = ${maxVal}</#if>
    )
        </#if>

        <#-- Numeric validations -->
        <#if needsMinMax>
            <#if minVal != "">
    @Min(${minVal})
            </#if>

            <#if maxVal != "">
    @Max(${maxVal})
            </#if>
        </#if>

    </#if>

<#-- Legacy field-based relationships (backward compatibility) -->
<#if field.relationshipType?? && field.relationshipType?has_content>

    <#switch field.relationshipType?string>

        <#case "MANY_TO_ONE">
    @ManyToOne
    @JoinColumn(name="${field.name?lower_case}_id")
    private ${field.relationshipTarget} ${field.name?uncap_first};
            <#break>

        <#case "ONE_TO_ONE">
    @OneToOne
    @JoinColumn(name="${field.name?lower_case}_id")
    private ${field.relationshipTarget} ${field.name?uncap_first};
            <#break>

        <#case "ONE_TO_MANY">
    @OneToMany(mappedBy="${entity.name?uncap_first}")
    private List<${field.relationshipTarget}> ${field.name?uncap_first};
            <#break>

        <#case "MANY_TO_MANY">
    @ManyToMany
    private List<${field.relationshipTarget}> ${field.name?uncap_first};
            <#break>

    </#switch>

<#else>

    private ${getJavaType(field.dataType)} ${field.name?uncap_first};

</#if>
</#list>

<#-- Generate relationship fields from RelationshipDefinition (new model) -->
<#if relationshipFields?? && (relationshipFields?size > 0)>
    // ========== Relationships ==========

<#list relationshipFields as relField>
${relField}
</#list>
</#if>
}

<#-- Java type mapper -->
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
