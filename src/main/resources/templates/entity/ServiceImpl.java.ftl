package ${project.packageName}.service.impl;

import ${project.packageName}.model.${entity.name?cap_first};
import ${project.packageName}.repository.${entity.name?cap_first}Repository;
import ${project.packageName}.service.${entity.name?cap_first}Service;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private ${entity.name?cap_first}Repository ${entity.name?uncap_first}Repository;
    
<#list entity.operations as operation>
    <#if operation.operationType == "CREATE">
    @Override
    <#if project.cachingEnabled>
    @CacheEvict(value = "${entity.name?lower_case}s", allEntries = true)
    </#if>
    public ${entity.name?cap_first} create${entity.name?cap_first}(${entity.name?cap_first} ${entity.name?uncap_first}) {
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
    public ${entity.name?cap_first} update${entity.name?cap_first}(Long id, ${entity.name?cap_first} ${entity.name?uncap_first}) {
        ${entity.name?cap_first} existing = ${entity.name?uncap_first}Repository.findById(id)
            .orElseThrow(() -> new RuntimeException("${entity.name?cap_first} not found with id: " + id));
        
        // Update fields here
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