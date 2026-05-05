package ${project.packageName}.repository;

import ${project.packageName}.model.${entity.name?cap_first};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ${entity.name?cap_first}Repository extends JpaRepository<${entity.name?cap_first}, Long> {
    
<#list entity.operations as operation>
    <#if operation.operationType == "SEARCH">
    // Search operations
    List<${entity.name?cap_first}> findByNameContainingIgnoreCase(String name);
    
    <#elseif operation.operationType == "SOFT_DELETE">
    // Soft delete operations
    @Query("SELECT e FROM ${entity.name?cap_first} e WHERE e.deleted = false")
    List<${entity.name?cap_first}> findAllActive();
    
    </#if>
</#list>
}