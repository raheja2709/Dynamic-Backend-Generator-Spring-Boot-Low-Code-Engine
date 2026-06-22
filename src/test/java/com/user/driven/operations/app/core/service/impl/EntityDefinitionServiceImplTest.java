package com.user.driven.operations.app.core.service.impl;

import com.user.driven.operations.app.api.dto.EntityDefinitionDto;
import com.user.driven.operations.app.api.dto.RelationshipDefinitionDto;
import com.user.driven.operations.app.api.mapper.DtoMapper;
import com.user.driven.operations.app.common.exception.DuplicateNameException;
import com.user.driven.operations.app.common.exception.ProjectNotFoundException;
import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.repository.*;
import com.user.driven.operations.enums.RelationshipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityDefinitionServiceImplTest {

    @Mock
    private EntityDefinitionRepository entityRepository;
    @Mock
    private ProjectDefinitionRepository projectRepository;
    @Mock
    private FieldDefinitionRepository fieldRepository;
    @Mock
    private OperationConfigRepository operationRepository;
    @Mock
    private RelationshipDefinitionRepository relationshipRepository;
    @Mock
    private DtoMapper dtoMapper;

    @InjectMocks
    private EntityDefinitionServiceImpl service;

    private ProjectDefinition project;

    @BeforeEach
    void setUp() {
        project = new ProjectDefinition();
        project.setId(1L);
        project.setName("TestProject");
    }

    @Test
    void createEntity_success() {
        EntityDefinitionDto dto = new EntityDefinitionDto();
        dto.setName("Product");
        dto.setFields(new ArrayList<>());

        EntityDefinition entity = new EntityDefinition();
        entity.setId(1L);
        entity.setName("Product");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(entityRepository.countByProjectId(1L)).thenReturn(0L);
        when(entityRepository.existsByNameAndProjectId("Product", 1L)).thenReturn(false);
        when(dtoMapper.toEntity(dto)).thenReturn(entity);
        when(entityRepository.save(any())).thenReturn(entity);

        EntityDefinition result = service.createEntity(1L, dto);

        assertNotNull(result);
        assertEquals("Product", result.getName());
        verify(entityRepository).save(any());
    }

    @Test
    void createEntity_projectNotFound_throwsException() {
        EntityDefinitionDto dto = new EntityDefinitionDto();
        dto.setName("Product");

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> service.createEntity(99L, dto));
    }

    @Test
    void createEntity_duplicateName_throwsException() {
        EntityDefinitionDto dto = new EntityDefinitionDto();
        dto.setName("Product");
        dto.setFields(new ArrayList<>());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(entityRepository.countByProjectId(1L)).thenReturn(0L);
        when(entityRepository.existsByNameAndProjectId("Product", 1L)).thenReturn(true);

        assertThrows(DuplicateNameException.class, () -> service.createEntity(1L, dto));
    }

    @Test
    void createEntity_exceedsEntityLimit_throwsValidation() {
        EntityDefinitionDto dto = new EntityDefinitionDto();
        dto.setName("Product");
        dto.setFields(new ArrayList<>());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(entityRepository.countByProjectId(1L)).thenReturn(50L);

        assertThrows(ValidationException.class, () -> service.createEntity(1L, dto));
    }

    @Test
    void deleteEntity_success() {
        when(entityRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> service.deleteEntity(1L));
        verify(entityRepository).deleteById(1L);
    }

    @Test
    void deleteEntity_notFound_throwsException() {
        when(entityRepository.existsById(99L)).thenReturn(false);

        assertThrows(ProjectNotFoundException.class, () -> service.deleteEntity(99L));
    }

    @Test
    void createEntity_invalidRelationshipTarget_throwsValidation() {
        RelationshipDefinitionDto relDto = new RelationshipDefinitionDto();
        relDto.setFieldName("department");
        relDto.setTargetEntity("NonExistentEntity");
        relDto.setRelationshipType(RelationshipType.MANY_TO_ONE);

        EntityDefinitionDto dto = new EntityDefinitionDto();
        dto.setName("Employee");
        dto.setFields(new ArrayList<>());
        dto.setRelationships(List.of(relDto));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(entityRepository.countByProjectId(1L)).thenReturn(0L);
        when(entityRepository.existsByNameAndProjectId("Employee", 1L)).thenReturn(false);
        when(entityRepository.existsByNameAndProjectId("NonExistentEntity", 1L)).thenReturn(false);

        assertThrows(ValidationException.class, () -> service.createEntity(1L, dto));
    }
}
