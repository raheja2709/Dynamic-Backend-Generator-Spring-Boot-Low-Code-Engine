package com.user.driven.operations.app.api.controller;

import com.user.driven.operations.app.api.dto.GenerateProjectRequest;
import com.user.driven.operations.app.api.mapper.ProjectMapper;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.service.ProjectGenerationService;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/generator")
public class GeneratorController {

    private final ProjectGenerationService service;

    public GeneratorController(ProjectGenerationService service) {
        this.service = service;
    }

    @PostMapping("/generate")
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
