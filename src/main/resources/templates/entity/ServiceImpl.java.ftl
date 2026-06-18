package ${project.packageName}.service.impl;

import ${project.packageName}.model.${entity.name?cap_first};
import ${project.packageName}.dto.${entity.name?cap_first}CreateRequest;
import ${project.packageName}.dto.${entity.name?cap_first}UpdateRequest;
import ${project.packageName}.repository.${entity.name?cap_first}Repository;
import ${project.packageName}.service.${entity.name?cap_first}Service;
<#-- Import related entity repositories for FK resolution -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
import ${project.packageName}.model.${rel.targetEntity};
import ${project.packageName}.repository.${rel.targetEntity}Repository;
</#list>
</#if>
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<#if project.cachingEnabled>
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
</#if>
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ${entity.name?cap_first}ServiceImpl implements ${entity.name?cap_first}Service {

    private final ${entity.name?cap_first}Repository ${entity.name?uncap_first}Repository;
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
    private final ${rel.targetEntity}Repository ${rel.targetEntity?uncap_first}Repository;
</#list>
</#if>

    public ${entity.name?cap_first}ServiceImpl(${entity.name?cap_first}Repository ${entity.name?uncap_first}Repository<#if entity.relationships?? && (entity.relationships?size > 0)><#list entity.relationships as rel>,
            ${rel.targetEntity}Repository ${rel.targetEntity?uncap_first}Repository</#list></#if>) {
        this.${entity.name?uncap_first}Repository = ${entity.name?uncap_first}Repository;
<#if entity.relationships?? && (entity.relationships?size > 0)>
<#list entity.relationships as rel>
        this.${rel.targetEntity?uncap_first}Repository = ${rel.targetEntity?uncap_first}Repository;
</#list>
</#if>
    }

<#list entity.operations as operation>
    <#if operation.operationType == "CREATE">
    @Override
    <#if project.cachingEnabled>
    @CacheEvict(value = "${entity.name?lower_case}s", allEntries = true)
    </#if>
    public ${entity.name?cap_first} create${entity.name?cap_first}(${entity.name?cap_first}CreateRequest request) {
        ${entity.name?cap_first} ${entity.name?uncap_first} = new ${entity.name?cap_first}();

        // Set regular fields
<#list entity.fields as field>
    <#if field.fieldType != "PRIMARY_KEY" && !(field.relationshipType?? && field.relationshipType?has_content)>
        ${entity.name?uncap_first}.set${field.name?cap_first}(request.get${field.name?cap_first}());
    </#if>
</#list>

<#-- Resolve FK relationships -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
        // Resolve relationship references
<#list entity.relationships as rel>
    <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
        if (request.get${rel.fieldName?cap_first}Id() != null) {
            ${rel.targetEntity} ${rel.fieldName} = ${rel.targetEntity?uncap_first}Repository.findById(request.get${rel.fieldName?cap_first}Id())
                    .orElseThrow(() -> new RuntimeException("${rel.targetEntity} not found with id: " + request.get${rel.fieldName?cap_first}Id()));
            ${entity.name?uncap_first}.set${rel.fieldName?cap_first}(${rel.fieldName});
        }
    <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
        if (request.get${rel.fieldName?cap_first}Ids() != null && !request.get${rel.fieldName?cap_first}Ids().isEmpty()) {
            List<${rel.targetEntity}> ${rel.fieldName} = ${rel.targetEntity?uncap_first}Repository.findAllById(request.get${rel.fieldName?cap_first}Ids());
            if (${rel.fieldName}.size() != request.get${rel.fieldName?cap_first}Ids().size()) {
                throw new RuntimeException("One or more ${rel.targetEntity} entities not found");
            }
            ${entity.name?uncap_first}.set${rel.fieldName?cap_first}(${rel.fieldName});
        }
    </#if>
</#list>
</#if>

        return ${entity.name?uncap_first}Repository.save(${entity.name?uncap_first});
    }

    <#elseif operation.operationType == "READ">
    @Override
    @Transactional(readOnly = true)
    <#if project.cachingEnabled>
    @Cacheable(value = "${entity.name?lower_case}", key = "#id")
    </#if>
    public Optional<${entity.name?cap_first}> get${entity.name?cap_first}ById(Long id) {
        return ${entity.name?uncap_first}Repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    <#if project.cachingEnabled>
    @Cacheable(value = "${entity.name?lower_case}s")
    </#if>
    public List<${entity.name?cap_first}> getAll${entity.name?cap_first}s() {
        return ${entity.name?uncap_first}Repository.findAll();
    }

    <#elseif operation.operationType == "UPDATE">
    @Override
    <#if project.cachingEnabled>
    @CacheEvict(value = {"${entity.name?lower_case}", "${entity.name?lower_case}s"}, allEntries = true)
    </#if>
    public ${entity.name?cap_first} update${entity.name?cap_first}(Long id, ${entity.name?cap_first}UpdateRequest request) {
        ${entity.name?cap_first} existing = ${entity.name?uncap_first}Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("${entity.name?cap_first} not found with id: " + id));

        // Update regular fields (only if provided)
<#list entity.fields as field>
    <#if field.fieldType != "PRIMARY_KEY" && !(field.relationshipType?? && field.relationshipType?has_content)>
        if (request.get${field.name?cap_first}() != null) {
            existing.set${field.name?cap_first}(request.get${field.name?cap_first}());
        }
    </#if>
</#list>

<#-- Resolve FK relationships for update -->
<#if entity.relationships?? && (entity.relationships?size > 0)>
        // Resolve relationship references
<#list entity.relationships as rel>
    <#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
        if (request.get${rel.fieldName?cap_first}Id() != null) {
            ${rel.targetEntity} ${rel.fieldName} = ${rel.targetEntity?uncap_first}Repository.findById(request.get${rel.fieldName?cap_first}Id())
                    .orElseThrow(() -> new RuntimeException("${rel.targetEntity} not found with id: " + request.get${rel.fieldName?cap_first}Id()));
            existing.set${rel.fieldName?cap_first}(${rel.fieldName});
        }
    <#elseif rel.relationshipType.name() == "ONE_TO_MANY" || rel.relationshipType.name() == "MANY_TO_MANY">
        if (request.get${rel.fieldName?cap_first}Ids() != null) {
            List<${rel.targetEntity}> ${rel.fieldName} = ${rel.targetEntity?uncap_first}Repository.findAllById(request.get${rel.fieldName?cap_first}Ids());
            if (${rel.fieldName}.size() != request.get${rel.fieldName?cap_first}Ids().size()) {
                throw new RuntimeException("One or more ${rel.targetEntity} entities not found");
            }
            existing.set${rel.fieldName?cap_first}(${rel.fieldName});
        }
    </#if>
</#list>
</#if>

        return ${entity.name?uncap_first}Repository.save(existing);
    }

    <#elseif operation.operationType == "DELETE">
    @Override
    <#if project.cachingEnabled>
    @CacheEvict(value = {"${entity.name?lower_case}", "${entity.name?lower_case}s"}, allEntries = true)
    </#if>
    public void delete${entity.name?cap_first}(Long id) {
        if (!${entity.name?uncap_first}Repository.existsById(id)) {
            throw new RuntimeException("${entity.name?cap_first} not found with id: " + id);
        }
        ${entity.name?uncap_first}Repository.deleteById(id);
    }

    <#elseif operation.operationType == "SEARCH">
    @Override
    @Transactional(readOnly = true)
    public List<${entity.name?cap_first}> search${entity.name?cap_first}s(String query) {
        return ${entity.name?uncap_first}Repository.findByNameContainingIgnoreCase(query);
    }

    <#elseif operation.operationType == "BULK_INSERT">
    @Override
    <#if project.cachingEnabled>
    @CacheEvict(value = "${entity.name?lower_case}s", allEntries = true)
    </#if>
    public List<${entity.name?cap_first}> bulkCreate${entity.name?cap_first}s(List<${entity.name?cap_first}> ${entity.name?uncap_first}s) {
        return ${entity.name?uncap_first}Repository.saveAll(${entity.name?uncap_first}s);
    }

    </#if>
</#list>
}
