package ${project.packageName}.model;

import jakarta.persistence.*;
<#list entity.fields as field>
<#if field.relationshipType??>
import ${project.packageName}.model.${field.relationshipTarget};
</#if>
</#list>
<#assign hasRelationship = false>

<#list entity.fields as field>
    <#if field.relationshipType?? && field.relationshipType?has_content>
        <#assign hasRelationship = true>
    </#if>
</#list>

<#if hasRelationship>
import java.util.List;
</#if>
import jakarta.validation.constraints.*;
import lombok.*;

<#-- Determine required imports -->
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

    <#-- Apply validation only for normal fields, not relationships -->
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

<#if field.relationshipType??>

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
