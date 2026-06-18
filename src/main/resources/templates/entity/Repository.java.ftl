package ${project.packageName}.repository;

import ${project.packageName}.model.${entity.name?cap_first};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ${entity.name?cap_first}Repository extends JpaRepository<${entity.name?cap_first}, Long>, JpaSpecificationExecutor<${entity.name?cap_first}> {

<#list entity.operations as operation>
    <#if operation.operationType == "SEARCH">
    // Search operations
    List<${entity.name?cap_first}> findByNameContainingIgnoreCase(String name);

    <#elseif operation.operationType == "SOFT_DELETE">
    // Soft delete operations
    @Query("SELECT e FROM ${entity.name?cap_first} e WHERE e.deleted = false")
    List<${entity.name?cap_first}> findAllActive();

    @Modifying
    @Query("UPDATE ${entity.name?cap_first} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void softDelete(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ${entity.name?cap_first} e SET e.deleted = false, e.deletedAt = null WHERE e.id = :id")
    void restore(@Param("id") Long id);

    </#if>
</#list>
}
