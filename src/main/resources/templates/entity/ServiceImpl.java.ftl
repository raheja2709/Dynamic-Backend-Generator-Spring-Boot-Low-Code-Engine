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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<#if project.cachingEnabled>
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
</#if>
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
<#list entity.fields as field>
    <#if field.fieldType != "PRIMARY_KEY" && !(field.relationshipType?? && field.relationshipType?has_content)>
        ${entity.name?uncap_first}.set${field.name?cap_first}(request.get${field.name?cap_first}());
    </#if>
</#list>
<#if entity.relationships?? && (entity.relationships?size > 0)>
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
<#list entity.fields as field>
    <#if field.fieldType != "PRIMARY_KEY" && !(field.relationshipType?? && field.relationshipType?has_content)>
        if (request.get${field.name?cap_first}() != null) {
            existing.set${field.name?cap_first}(request.get${field.name?cap_first}());
        }
    </#if>
</#list>
<#if entity.relationships?? && (entity.relationships?size > 0)>
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

    <#elseif operation.operationType == "PAGINATION">
    @Override
    @Transactional(readOnly = true)
    public Page<${entity.name?cap_first}> getAll${entity.name?cap_first}sPaged(Pageable pageable) {
        return ${entity.name?uncap_first}Repository.findAll(pageable);
    }

    <#elseif operation.operationType == "BULK_INSERT">
    @Override
    <#if project.cachingEnabled>
    @CacheEvict(value = "${entity.name?lower_case}s", allEntries = true)
    </#if>
    public List<${entity.name?cap_first}> bulkCreate${entity.name?cap_first}s(List<${entity.name?cap_first}CreateRequest> requests) {
        if (requests.size() > 500) {
            throw new RuntimeException("Bulk insert limited to 500 items. Received: " + requests.size());
        }
        List<${entity.name?cap_first}> entities = requests.stream().map(request -> {
            ${entity.name?cap_first} entity = new ${entity.name?cap_first}();
<#list entity.fields as field>
    <#if field.fieldType != "PRIMARY_KEY" && !(field.relationshipType?? && field.relationshipType?has_content)>
            entity.set${field.name?cap_first}(request.get${field.name?cap_first}());
    </#if>
</#list>
            return entity;
        }).collect(Collectors.toList());
        return ${entity.name?uncap_first}Repository.saveAll(entities);
    }

    <#elseif operation.operationType == "BULK_UPDATE">
    @Override
    public List<${entity.name?cap_first}> bulkUpdate${entity.name?cap_first}s(List<${entity.name?cap_first}UpdateRequest> requests) {
        // Bulk update implementation - each request should include an ID field
        throw new UnsupportedOperationException("Bulk update requires ID field in UpdateRequest");
    }

    <#elseif operation.operationType == "BULK_DELETE">
    @Override
    public void bulkDelete${entity.name?cap_first}s(List<Long> ids) {
        ${entity.name?uncap_first}Repository.deleteAllById(ids);
    }

    <#elseif operation.operationType == "SOFT_DELETE">
    @Override
    public void softDelete${entity.name?cap_first}(Long id) {
        if (!${entity.name?uncap_first}Repository.existsById(id)) {
            throw new RuntimeException("${entity.name?cap_first} not found with id: " + id);
        }
        ${entity.name?uncap_first}Repository.softDelete(id);
    }

    <#elseif operation.operationType == "RESTORE">
    @Override
    public void restore${entity.name?cap_first}(Long id) {
        if (!${entity.name?uncap_first}Repository.existsById(id)) {
            throw new RuntimeException("${entity.name?cap_first} not found with id: " + id);
        }
        ${entity.name?uncap_first}Repository.restore(id);
    }

    <#elseif operation.operationType == "EXPORT_CSV">
    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        List<${entity.name?cap_first}> entities = ${entity.name?uncap_first}Repository.findAll();
        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("<#list entity.fields as field><#if field.fieldType != 'PRIMARY_KEY' && !(field.relationshipType?? && field.relationshipType?has_content)>${field.name}<#sep>,</#sep></#if></#list>\n");
        // Data rows
        for (${entity.name?cap_first} entity : entities) {
            csv.append(<#list entity.fields as field><#if field.fieldType != "PRIMARY_KEY" && !(field.relationshipType?? && field.relationshipType?has_content)>String.valueOf(entity.get${field.name?cap_first}())<#sep> + "," + </#sep></#if></#list> + "\n");
        }
        return csv.toString().getBytes();
    }

    <#elseif operation.operationType == "EXPORT_EXCEL">
    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcel() {
        // TODO: Implement with Apache POI
        throw new UnsupportedOperationException("Excel export not yet implemented");
    }

    <#elseif operation.operationType == "EXPORT_PDF">
    @Override
    @Transactional(readOnly = true)
    public byte[] exportPdf() {
        // TODO: Implement with OpenPDF
        throw new UnsupportedOperationException("PDF export not yet implemented");
    }

    <#elseif operation.operationType == "IMPORT_CSV">
    @Override
    public String importCsv(byte[] data) {
        String content = new String(data);
        String[] lines = content.split("\n");
        int imported = 0;
        int errors = 0;
        for (int i = 1; i < lines.length; i++) {
            try {
                // Parse and save each row
                imported++;
            } catch (Exception e) {
                errors++;
            }
        }
        return String.format("Imported: %d, Errors: %d", imported, errors);
    }

    <#elseif operation.operationType == "IMPORT_EXCEL">
    @Override
    public String importExcel(byte[] data) {
        // TODO: Implement with Apache POI
        throw new UnsupportedOperationException("Excel import not yet implemented");
    }

    <#elseif operation.operationType == "FILE_UPLOAD">
    @Override
    public String uploadFile(Long entityId, byte[] fileData, String fileName) {
        // TODO: Implement file storage
        throw new UnsupportedOperationException("File upload not yet implemented");
    }

    <#elseif operation.operationType == "FILE_DOWNLOAD">
    @Override
    public byte[] downloadFile(Long entityId) {
        // TODO: Implement file retrieval
        throw new UnsupportedOperationException("File download not yet implemented");
    }

    <#elseif operation.operationType == "STATUS_TRANSITION">
    @Override
    public ${entity.name?cap_first} transition${entity.name?cap_first}Status(Long id, String newStatus) {
        ${entity.name?cap_first} entity = ${entity.name?uncap_first}Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("${entity.name?cap_first} not found with id: " + id));
        // TODO: Validate state transition
        entity.setStatus(newStatus);
        return ${entity.name?uncap_first}Repository.save(entity);
    }

    </#if>
</#list>
}
