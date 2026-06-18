package ${project.packageName}.service;

import ${project.packageName}.model.${entity.name?cap_first};
import ${project.packageName}.dto.${entity.name?cap_first}CreateRequest;
import ${project.packageName}.dto.${entity.name?cap_first}UpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ${entity.name?cap_first}Service {

<#list entity.operations as operation>
    <#if operation.operationType == "CREATE">
    ${entity.name?cap_first} create${entity.name?cap_first}(${entity.name?cap_first}CreateRequest request);

    <#elseif operation.operationType == "READ">
    Optional<${entity.name?cap_first}> get${entity.name?cap_first}ById(Long id);

    List<${entity.name?cap_first}> getAll${entity.name?cap_first}s();

    <#elseif operation.operationType == "UPDATE">
    ${entity.name?cap_first} update${entity.name?cap_first}(Long id, ${entity.name?cap_first}UpdateRequest request);

    <#elseif operation.operationType == "DELETE">
    void delete${entity.name?cap_first}(Long id);

    <#elseif operation.operationType == "SEARCH">
    List<${entity.name?cap_first}> search${entity.name?cap_first}s(String query);

    <#elseif operation.operationType == "PAGINATION">
    Page<${entity.name?cap_first}> getAll${entity.name?cap_first}sPaged(Pageable pageable);

    <#elseif operation.operationType == "BULK_INSERT">
    List<${entity.name?cap_first}> bulkCreate${entity.name?cap_first}s(List<${entity.name?cap_first}CreateRequest> requests);

    <#elseif operation.operationType == "BULK_UPDATE">
    List<${entity.name?cap_first}> bulkUpdate${entity.name?cap_first}s(List<${entity.name?cap_first}UpdateRequest> requests);

    <#elseif operation.operationType == "BULK_DELETE">
    void bulkDelete${entity.name?cap_first}s(List<Long> ids);

    <#elseif operation.operationType == "SOFT_DELETE">
    void softDelete${entity.name?cap_first}(Long id);

    <#elseif operation.operationType == "RESTORE">
    void restore${entity.name?cap_first}(Long id);

    <#elseif operation.operationType == "EXPORT_CSV">
    byte[] exportCsv();

    <#elseif operation.operationType == "EXPORT_EXCEL">
    byte[] exportExcel();

    <#elseif operation.operationType == "EXPORT_PDF">
    byte[] exportPdf();

    <#elseif operation.operationType == "IMPORT_CSV">
    String importCsv(byte[] data);

    <#elseif operation.operationType == "IMPORT_EXCEL">
    String importExcel(byte[] data);

    <#elseif operation.operationType == "FILE_UPLOAD">
    String uploadFile(Long entityId, byte[] fileData, String fileName);

    <#elseif operation.operationType == "FILE_DOWNLOAD">
    byte[] downloadFile(Long entityId);

    <#elseif operation.operationType == "AUDIT_LOG">
    // Audit log is handled via JPA event listeners

    <#elseif operation.operationType == "VERSIONING">
    // Versioning is handled via @Version and entity listeners

    <#elseif operation.operationType == "STATUS_TRANSITION">
    ${entity.name?cap_first} transition${entity.name?cap_first}Status(Long id, String newStatus);

    <#elseif operation.operationType == "WEBHOOK_INTEGRATION">
    // Webhook integration is handled via event publishing

    </#if>
</#list>
}
