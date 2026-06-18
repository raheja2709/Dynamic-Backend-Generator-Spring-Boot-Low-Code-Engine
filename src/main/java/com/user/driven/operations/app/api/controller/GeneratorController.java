package com.user.driven.operations.app.api.controller;

import com.user.driven.operations.app.api.dto.GenerateProjectRequest;
import com.user.driven.operations.app.api.mapper.ProjectMapper;
import com.user.driven.operations.app.common.util.AppConstants;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.service.ProjectGenerationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

/**
 * Controller for project generation operations.
 * Binary file responses (ZIP download) do not use ApiEnvelope wrapping.
 */
@RestController
@RequestMapping(AppConstants.GENERATOR_V1)
@Tag(name = "Project Generation", description = "APIs for generating Spring Boot projects")
public class GeneratorController {

    private final ProjectGenerationService service;

    public GeneratorController(ProjectGenerationService service) {
        this.service = service;
    }

    /**
     * Generates a Spring Boot project from the provided definition and returns
     * the result as a downloadable ZIP file.
     *
     * @param request the project generation request
     * @return the generated project as a ZIP file download
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate a Spring Boot project", description = "Generates a complete Spring Boot project from the provided definition and returns it as a downloadable ZIP file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid project definition or validation failed"),
            @ApiResponse(responseCode = "500", description = "Generation or build verification failed")
    })
    public ResponseEntity<FileSystemResource> generate(@Valid @RequestBody GenerateProjectRequest request) {

        ProjectDefinition project = ProjectMapper.map(request);

        Path zipPath = service.generate(project);

        FileSystemResource resource = new FileSystemResource(zipPath.toFile());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + zipPath.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
