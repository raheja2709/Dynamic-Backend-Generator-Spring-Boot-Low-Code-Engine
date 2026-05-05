package ${project.packageName}.controller;

import ${project.packageName}.model.${entity.name?cap_first};
import ${project.packageName}.service.${entity.name?cap_first}Service;
<#if project.swaggerEnabled>
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
</#if>
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/${entity.name?lower_case?replace(" ", "-")}s")
<#if project.swaggerEnabled>
@Tag(name = "${entity.name?cap_first} Management", description = "APIs for managing ${entity.name?lower_case}s")
</#if>
public class ${entity.name?cap_first}Controller {
    
    @Autowired
    private ${entity.name?cap_first}Service ${entity.name?uncap_first}Service;
    
<#list entity.operations as operation>
    <#if operation.operationType == "CREATE">
    @PostMapping
    <#if project.swaggerEnabled>
    @Operation(summary = "Create a new ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<${entity.name?cap_first}> create${entity.name?cap_first}(@Valid @RequestBody ${entity.name?cap_first} ${entity.name?uncap_first}) {
        ${entity.name?cap_first} created = ${entity.name?uncap_first}Service.create${entity.name?cap_first}(${entity.name?uncap_first});
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
            .map(${entity.name?uncap_first} -> ResponseEntity.ok(${entity.name?uncap_first}))
            .orElse(ResponseEntity.notFound().build());
    }
    
    <#elseif operation.operationType == "UPDATE">
    @PutMapping("/{id}")
    <#if project.swaggerEnabled>
    @Operation(summary = "Update ${entity.name?lower_case}")
    </#if>
    public ResponseEntity<${entity.name?cap_first}> update${entity.name?cap_first}(@PathVariable Long id, @Valid @RequestBody ${entity.name?cap_first} ${entity.name?uncap_first}) {
        try {
            ${entity.name?cap_first} updated = ${entity.name?uncap_first}Service.update${entity.name?cap_first}(id, ${entity.name?uncap_first});
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
    
    <#elseif operation.operationType == "BULK_INSERT">
    @PostMapping("/bulk")
    <#if project.swaggerEnabled>
    @Operation(summary = "Bulk create ${entity.name?lower_case}s")
    </#if>
    public ResponseEntity<List<${entity.name?cap_first}>> bulkCreate${entity.name?cap_first}s(@Valid @RequestBody List<${entity.name?cap_first}> ${entity.name?uncap_first}s) {
        List<${entity.name?cap_first}> created = ${entity.name?uncap_first}Service.bulkCreate${entity.name?cap_first}s(${entity.name?uncap_first}s);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    </#if>
</#list>
}