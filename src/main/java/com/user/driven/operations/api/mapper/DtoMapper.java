package com.user.driven.operations.api.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.user.driven.operations.api.dto.EntityDefinitionDto;
import com.user.driven.operations.api.dto.FieldDefinitionDto;
import com.user.driven.operations.api.dto.OperationConfigDto;
import com.user.driven.operations.api.dto.ProjectDefinitionDto;
import com.user.driven.operations.core.model.EntityDefinition;
import com.user.driven.operations.core.model.FieldDefinition;
import com.user.driven.operations.core.model.OperationConfig;
import com.user.driven.operations.core.model.ProjectDefinition;

/**
 * Mapper class responsible for converting between DTOs and Entity objects.
 * It provides utility methods to convert full object graphs, including nested entities and lists.
 * <p>
 * This class helps in isolating mapping logic from the rest of the service and controller layers,
 * which promotes cleaner architecture and separation of concerns.
 * </p>
 * 
 * @author Jatin Raheja
 */

@Component
public class DtoMapper {

	/**
	 * Converts a ProjectDefinitionDto to a ProjectDefinition entity.
	 *
	 * @param dto the project DTO
	 * @return the corresponding ProjectDefinition entity
	 */
	public ProjectDefinition toEntity(ProjectDefinitionDto dto) {
		ProjectDefinition entity = new ProjectDefinition();
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPackageName(dto.getPackageName());
		entity.setDatabaseType(dto.getDatabaseType());
		entity.setSecurityEnabled(dto.isSecurityEnabled());
		entity.setSecurityType(dto.getSecurityType());
		entity.setCachingEnabled(dto.isCachingEnabled());
		entity.setSwaggerEnabled(dto.isSwaggerEnabled());
		entity.setCustomConfiguration(dto.getCustomConfiguration());

		if (dto.getEntities() != null) {
			entity.setEntities(dto.getEntities().stream().map(this::toEntity).collect(Collectors.toList()));
			entity.getEntities().forEach(e -> e.setProject(entity));
		}

		return entity;
	}

	/**
	 * Converts an EntityDefinitionDto to an EntityDefinition entity.
	 *
	 * @param dto the entity definition DTO
	 * @return the corresponding EntityDefinition entity
	 */
	public EntityDefinition toEntity(EntityDefinitionDto dto) {
		EntityDefinition entity = new EntityDefinition();
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());

		if (dto.getFields() != null) {
			entity.setFields(dto.getFields().stream().map(this::toEntity).collect(Collectors.toList()));
			entity.getFields().forEach(f -> f.setEntity(entity));
		}

		if (dto.getOperations() != null) {
			entity.setOperations(dto.getOperations().stream().map(this::toEntity).collect(Collectors.toList()));
			entity.getOperations().forEach(o -> o.setEntity(entity));
		}

		return entity;
	}

	/**
	 * Converts a FieldDefinitionDto to a FieldDefinition entity.
	 *
	 * @param dto the field definition DTO
	 * @return the corresponding FieldDefinition entity
	 */
	public FieldDefinition toEntity(FieldDefinitionDto dto) {
		FieldDefinition entity = new FieldDefinition();
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDataType(dto.getDataType());
		entity.setFieldType(dto.getFieldType());
		entity.setValidationRules(dto.getValidationRules());
		entity.setRelationshipType(dto.getRelationshipType());
		entity.setRelationshipTarget(dto.getRelationshipTarget());
		entity.setNullable(dto.isNullable());
		entity.setDefaultValue(dto.getDefaultValue());
		return entity;
	}

	/**
	 * Converts an OperationConfigDto to an OperationConfig entity.
	 *
	 * @param dto the operation config DTO
	 * @return the corresponding OperationConfig entity
	 */
	public OperationConfig toEntity(OperationConfigDto dto) {
		OperationConfig entity = new OperationConfig();
		entity.setOperationType(dto.getOperationType());
		entity.setEnabled(dto.isEnabled());
		entity.setCustomLogic(dto.getCustomLogic());
		entity.setParameters(dto.getParameters());
		return entity;
	}

	/**
	 * Updates an existing ProjectDefinition entity with data from a
	 * ProjectDefinitionDto.
	 *
	 * @param dto    the project DTO containing updated data
	 * @param entity the existing ProjectDefinition entity to update
	 */
	public void updateEntityFromDto(ProjectDefinitionDto dto, ProjectDefinition entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPackageName(dto.getPackageName());
		entity.setDatabaseType(dto.getDatabaseType());
		entity.setSecurityEnabled(dto.isSecurityEnabled());
		entity.setSecurityType(dto.getSecurityType());
		entity.setCachingEnabled(dto.isCachingEnabled());
		entity.setSwaggerEnabled(dto.isSwaggerEnabled());
		entity.setCustomConfiguration(dto.getCustomConfiguration());
	}

	/**
	 * Updates an existing EntityDefinition entity with data from an
	 * EntityDefinitionDto.
	 *
	 * @param dto    the entity DTO containing updated data
	 * @param entity the existing EntityDefinition entity to update
	 */
	public void updateEntityFromDto(EntityDefinitionDto dto, EntityDefinition entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
	}
}
