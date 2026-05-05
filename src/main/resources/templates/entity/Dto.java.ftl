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

public class ${entity.name?cap_first}Dto {
    
<#list entity.fields as field>
    <#if !field.nullable>
    @NotNull
    </#if>
    <#if field.validationRules?has_content>
    // Validation: ${field.validationRules}
    </#if>
    private ${getJavaType(field.dataType)} ${field.name?uncap_first};
    
</#list>
    
    // Constructors
    public ${entity.name?cap_first}Dto() {}
    
    // Getters and Setters
<#list entity.fields as field>
    public ${getJavaType(field.dataType)} get${field.name?cap_first}() { 
        return ${field.name?uncap_first}; 
    }
    
    public void set${field.name?cap_first}(${getJavaType(field.dataType)} ${field.name?uncap_first}) { 
        this.${field.name?uncap_first} = ${field.name?uncap_first}; 
    }
    
</#list>
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