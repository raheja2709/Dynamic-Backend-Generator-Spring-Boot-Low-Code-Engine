package ${project.packageName}.service;

import ${project.packageName}.model.${entity.name?cap_first};
import java.util.List;
import java.util.Optional;

public interface ${entity.name?cap_first}Service {
    
<#list entity.operations as operation>
    <#if operation.operationType == "CREATE">
    ${entity.name?cap_first} create${entity.name?cap_first}(${entity.name?cap_first} ${entity.name?uncap_first});
    
    <#elseif operation.operationType == "READ">
    Optional<${entity.name?cap_first}> get${entity.name?cap_first}ById(Long id);
    
    List<${entity.name?cap_first}> getAll${entity.name?cap_first}s();
    
    <#elseif operation.operationType == "UPDATE">
    ${entity.name?cap_first} update${entity.name?cap_first}(Long id, ${entity.name?cap_first} ${entity.name?uncap_first});
    
    <#elseif operation.operationType == "DELETE">
    void delete${entity.name?cap_first}(Long id);
    
    <#elseif operation.operationType == "SEARCH">
    List<${entity.name?cap_first}> search${entity.name?cap_first}s(String query);
    
    <#elseif operation.operationType == "BULK_INSERT">
    List<${entity.name?cap_first}> bulkCreate${entity.name?cap_first}s(List<${entity.name?cap_first}> ${entity.name?uncap_first}s);
    
    </#if>
</#list>
}