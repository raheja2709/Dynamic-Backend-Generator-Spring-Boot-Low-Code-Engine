package ${project.packageName}.controller;

import ${project.packageName}.model.${entity.name?cap_first};
import ${project.packageName}.dto.${entity.name?cap_first}CreateRequest;
import ${project.packageName}.dto.${entity.name?cap_first}UpdateRequest;
import ${project.packageName}.service.${entity.name?cap_first}Service;
<#if project.swaggerEnabled>
import io.swagger.v3.oas.annotations.Operation;
</#if>
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/${entity.name?lower_case?replace(" ", "-")}s")
<#if project.swaggerEnabled>
@io.swagger.v3.oas.annotations.tags.Tag(name = "${entity.name?cap_first} Management", description = "APIs for managing ${entity.name?lower_case}s")
</#if>
public class ${entity.name?cap_first}Controller {

    private final ${entity.name?cap_first}Service ${entity.name?uncap_first}Service;

    public ${entity.name?cap_first}Controller(${entity.name?cap_first}Service ${entity.name?uncap_first}Service) {
        this.${entity.name?uncap_first}Service = ${entity.name?uncap_first}Service;
    }

<#list entity.operations as operation>
    <#if operation.operationType == "CREATE">
    @PostMapping
    <#if project.swaggerEnabled>
    @Operation(summary = "Create a new ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<${entity.name?cap_first}> create${entity.name?cap_first}(@Valid @RequestBody ${entity.name?cap_first}CreateRequest request) {
        ${entity.name?cap_first} created = ${entity.name?uncap_first}Service.create${entity.name?cap_first}(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    <#elseif operation.operationType == "READ">
    @GetMapping
    <#if project.swaggerEnabled>
    @Operation(summary = "Get all ${entity.name?lower_case}s")
    </#if>
    public ResponseEntity<List<${entity.name?cap_first}>> getAll${entity.name?cap_first}s() {
        List<${entity.name?cap_first}> ${entity.name?uncap_first}s = ${entity.name?uncap_first}Service.getAll${entity.name?cap_first}s();
        return ResponseEntity.ok(${entity.name?uncap_first}s);
    }

    @GetMapping("/{id}")
    <#if project.swaggerEnabled>
    @Operation(summary = "Get ${entity.name?lower_case} by ID")
    </#if>
    public ResponseEntity<${entity.name?cap_first}> get${entity.name?cap_first}ById(@PathVariable Long id) {
        return ${entity.name?uncap_first}Service.get${entity.name?cap_first}ById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    <#elseif operation.operationType == "UPDATE">
    @PutMapping("/{id}")
    <#if project.swaggerEnabled>
    @Operation(summary = "Update ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<${entity.name?cap_first}> update${entity.name?cap_first}(@PathVariable Long id, @Valid @RequestBody ${entity.name?cap_first}UpdateRequest request) {
        try {
            ${entity.name?cap_first} updated = ${entity.name?uncap_first}Service.update${entity.name?cap_first}(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    <#elseif operation.operationType == "DELETE">
    @DeleteMapping("/{id}")
    <#if project.swaggerEnabled>
    @Operation(summary = "Delete ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<Void> delete${entity.name?cap_first}(@PathVariable Long id) {
        try {
            ${entity.name?uncap_first}Service.delete${entity.name?cap_first}(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    <#elseif operation.operationType == "SEARCH">
    @GetMapping("/search")
    <#if project.swaggerEnabled>
    @Operation(summary = "Search ${entity.name?lower_case}s")
    </#if>
    public ResponseEntity<List<${entity.name?cap_first}>> search${entity.name?cap_first}s(@RequestParam String query) {
        List<${entity.name?cap_first}> results = ${entity.name?uncap_first}Service.search${entity.name?cap_first}s(query);
        return ResponseEntity.ok(results);
    }

    <#elseif operation.operationType == "PAGINATION">
    @GetMapping("/paged")
    <#if project.swaggerEnabled>
    @Operation(summary = "Get ${entity.name?lower_case}s with pagination")
    </#if>
    public ResponseEntity<Page<${entity.name?cap_first}>> getAll${entity.name?cap_first}sPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        if (size > 100) size = 100;
        if (size < 1) size = 1;
        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(sortParams.length > 1 ? sortParams[1] : "asc"), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<${entity.name?cap_first}> result = ${entity.name?uncap_first}Service.getAll${entity.name?cap_first}sPaged(pageable);
        return ResponseEntity.ok(result);
    }

    <#elseif operation.operationType == "BULK_INSERT">
    @PostMapping("/bulk")
    <#if project.swaggerEnabled>
    @Operation(summary = "Bulk create ${entity.name?lower_case}s (max 500)")
    </#if>
    public ResponseEntity<List<${entity.name?cap_first}>> bulkCreate${entity.name?cap_first}s(@Valid @RequestBody List<${entity.name?cap_first}CreateRequest> requests) {
        List<${entity.name?cap_first}> created = ${entity.name?uncap_first}Service.bulkCreate${entity.name?cap_first}s(requests);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    <#elseif operation.operationType == "BULK_UPDATE">
    @PutMapping("/bulk")
    <#if project.swaggerEnabled>
    @Operation(summary = "Bulk update ${entity.name?lower_case}s")
    </#if>
    public ResponseEntity<List<${entity.name?cap_first}>> bulkUpdate${entity.name?cap_first}s(@Valid @RequestBody List<${entity.name?cap_first}UpdateRequest> requests) {
        List<${entity.name?cap_first}> updated = ${entity.name?uncap_first}Service.bulkUpdate${entity.name?cap_first}s(requests);
        return ResponseEntity.ok(updated);
    }

    <#elseif operation.operationType == "BULK_DELETE">
    @DeleteMapping("/bulk")
    <#if project.swaggerEnabled>
    @Operation(summary = "Bulk delete ${entity.name?lower_case}s")
    </#if>
    public ResponseEntity<Void> bulkDelete${entity.name?cap_first}s(@RequestBody List<Long> ids) {
        ${entity.name?uncap_first}Service.bulkDelete${entity.name?cap_first}s(ids);
        return ResponseEntity.noContent().build();
    }

    <#elseif operation.operationType == "SOFT_DELETE">
    @DeleteMapping("/{id}/soft")
    <#if project.swaggerEnabled>
    @Operation(summary = "Soft delete ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<Void> softDelete${entity.name?cap_first}(@PathVariable Long id) {
        ${entity.name?uncap_first}Service.softDelete${entity.name?cap_first}(id);
        return ResponseEntity.noContent().build();
    }

    <#elseif operation.operationType == "RESTORE">
    @PatchMapping("/{id}/restore")
    <#if project.swaggerEnabled>
    @Operation(summary = "Restore soft-deleted ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<Void> restore${entity.name?cap_first}(@PathVariable Long id) {
        ${entity.name?uncap_first}Service.restore${entity.name?cap_first}(id);
        return ResponseEntity.ok().build();
    }

    <#elseif operation.operationType == "EXPORT_CSV">
    @GetMapping("/export/csv")
    <#if project.swaggerEnabled>
    @Operation(summary = "Export ${entity.name?lower_case}s as CSV")
    </#if>
    public ResponseEntity<byte[]> exportCsv() {
        byte[] data = ${entity.name?uncap_first}Service.exportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${entity.name?lower_case}s.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    <#elseif operation.operationType == "EXPORT_EXCEL">
    @GetMapping("/export/excel")
    <#if project.swaggerEnabled>
    @Operation(summary = "Export ${entity.name?lower_case}s as Excel")
    </#if>
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = ${entity.name?uncap_first}Service.exportExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${entity.name?lower_case}s.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    <#elseif operation.operationType == "EXPORT_PDF">
    @GetMapping("/export/pdf")
    <#if project.swaggerEnabled>
    @Operation(summary = "Export ${entity.name?lower_case}s as PDF")
    </#if>
    public ResponseEntity<byte[]> exportPdf() {
        byte[] data = ${entity.name?uncap_first}Service.exportPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${entity.name?lower_case}s.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    <#elseif operation.operationType == "IMPORT_CSV">
    @PostMapping("/import/csv")
    <#if project.swaggerEnabled>
    @Operation(summary = "Import ${entity.name?lower_case}s from CSV")
    </#if>
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) throws Exception {
        String result = ${entity.name?uncap_first}Service.importCsv(file.getBytes());
        return ResponseEntity.ok(result);
    }

    <#elseif operation.operationType == "IMPORT_EXCEL">
    @PostMapping("/import/excel")
    <#if project.swaggerEnabled>
    @Operation(summary = "Import ${entity.name?lower_case}s from Excel")
    </#if>
    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        String result = ${entity.name?uncap_first}Service.importExcel(file.getBytes());
        return ResponseEntity.ok(result);
    }

    <#elseif operation.operationType == "FILE_UPLOAD">
    @PostMapping("/{id}/upload")
    <#if project.swaggerEnabled>
    @Operation(summary = "Upload file for ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<String> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
        String result = ${entity.name?uncap_first}Service.uploadFile(id, file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.ok(result);
    }

    <#elseif operation.operationType == "FILE_DOWNLOAD">
    @GetMapping("/{id}/download")
    <#if project.swaggerEnabled>
    @Operation(summary = "Download file for ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        byte[] data = ${entity.name?uncap_first}Service.downloadFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${entity.name?lower_case}_" + id)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    <#elseif operation.operationType == "STATUS_TRANSITION">
    @PatchMapping("/{id}/status")
    <#if project.swaggerEnabled>
    @Operation(summary = "Transition ${entity.name?lower_case} status")
    </#if>
    public ResponseEntity<${entity.name?cap_first}> transitionStatus(@PathVariable Long id, @RequestParam String status) {
        ${entity.name?cap_first} updated = ${entity.name?uncap_first}Service.transition${entity.name?cap_first}Status(id, status);
        return ResponseEntity.ok(updated);
    }

    </#if>
</#list>
}
