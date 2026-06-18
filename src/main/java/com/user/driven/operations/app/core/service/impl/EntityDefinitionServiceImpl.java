package com.user.driven.operations.app.core.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.driven.operations.app.api.dto.EntityDefinitionDto;
import com.user.driven.operations.app.api.dto.RelationshipDefinitionDto;
import com.user.driven.operations.app.common.exception.DuplicateNameException;
import com.user.driven.operations.app.common.exception.ProjectNotFoundException;
import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.common.util.MessageConstants;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.core.model.RelationshipDefinition;
import com.user.driven.operations.app.api.mapper.DtoMapper;
import com.user.driven.operations.app.core.repository.EntityDefinitionRepository;
import com.user.driven.operations.app.core.repository.FieldDefinitionRepository;
import com.user.driven.operations.app.core.repository.OperationConfigRepository;
import com.user.driven.operations.app.core.repository.ProjectDefinitionRepository;
import com.user.driven.operations.app.core.repository.RelationshipDefinitionRepository;
import com.user.driven.operations.app.core.service.EntityDefinitionService;
import com.user.driven.operations.enums.CascadeType;

/**
 * Service implementation for managing {@link EntityDefinition}.
 * Handles business logic for creating, retrieving, updating, and deleting entities,
 * as well as managing relationships with fields and operations.
 *
 * @author: Jatin Raheja
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EntityDefinitionServiceImpl implements EntityDefinitionService {

	private final OperationConfigRepository operationRepository;

	private final FieldDefinitionRepository fieldRepository;

	private final EntityDefinitionRepository entityRepository;

	private final ProjectDefinitionRepository projectRepository;

	private final RelationshipDefinitionRepository relationshipRepository;

	private final DtoMapper dtoMapper;

	/**
	 * Valid cascade types for relationship definitions.
	 */
	private static final Set<String> VALID_CASCADE_TYPES = Arrays.stream(CascadeType.values())
			.map(CascadeType::name)
			.collect(Collectors.toSet());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityDefinition createEntity(Long projectId, EntityDefinitionDto entityDto) {
		ProjectDefinition project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ProjectNotFoundException("Project", projectId.toString()));

		// Check entity limit per project
		long currentEntityCount = entityRepository.countByProjectId(projectId);
		if (currentEntityCount >= MessageConstants.MAX_ENTITIES_PER_PROJECT) {
			throw new ValidationException(
					String.format(MessageConstants.MAX_ENTITIES_EXCEEDED,
							MessageConstants.MAX_ENTITIES_PER_PROJECT, currentEntityCount));
		}

		// Check field limit per entity
		if (entityDto.getFields() != null && entityDto.getFields().size() > MessageConstants.MAX_FIELDS_PER_ENTITY) {
			throw new ValidationException(
					String.format(MessageConstants.MAX_FIELDS_EXCEEDED,
							MessageConstants.MAX_FIELDS_PER_ENTITY, entityDto.getFields().size()));
		}

		if (existsByNameAndProjectId(entityDto.getName(), projectId)) {
			throw new DuplicateNameException("Entity", entityDto.getName());
		}

		// Validate relationships if provided
		if (entityDto.getRelationships() != null && !entityDto.getRelationships().isEmpty()) {
			validateRelationships(entityDto.getRelationships(), projectId);
		}

		EntityDefinition entity = dtoMapper.toEntity(entityDto);
		entity.setProject(project);
		EntityDefinition savedEntity = entityRepository.save(entity);

		// Save relationships if provided
		if (entityDto.getRelationships() != null && !entityDto.getRelationships().isEmpty()) {
			saveRelationships(entityDto.getRelationships(), savedEntity);
		}

		return savedEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<EntityDefinition> getEntityById(Long id) {
		return entityRepository.findById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<EntityDefinition> getEntityByIdWithFieldsAndOperations(Long id) {
		Optional<EntityDefinition> entityOpt = entityRepository.findById(id);

		if (entityOpt.isPresent()) {
			EntityDefinition entity = entityOpt.get();
			// Manually load collections to avoid MultipleBagFetchException
			entity.setFields(fieldRepository.findByEntityId(id));
			entity.setOperations(operationRepository.findByEntityId(id));
		}

		return entityOpt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<EntityDefinition> getEntitiesByProjectId(Long projectId) {
		return entityRepository.findByProjectId(projectId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<EntityDefinition> getEntitiesByProjectId(Long projectId, Pageable pageable) {
		return entityRepository.findByProjectId(projectId, pageable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityDefinition updateEntity(Long id, EntityDefinitionDto entityDto) {
		EntityDefinition existingEntity = entityRepository.findById(id)
				.orElseThrow(() -> new ProjectNotFoundException("Entity", id.toString()));

		// Check field limit per entity on update
		if (entityDto.getFields() != null && entityDto.getFields().size() > MessageConstants.MAX_FIELDS_PER_ENTITY) {
			throw new ValidationException(
					String.format(MessageConstants.MAX_FIELDS_EXCEEDED,
							MessageConstants.MAX_FIELDS_PER_ENTITY, entityDto.getFields().size()));
		}

		if (!existingEntity.getName().equals(entityDto.getName())
				&& existsByNameAndProjectId(entityDto.getName(), existingEntity.getProject().getId())) {
			throw new DuplicateNameException("Entity", entityDto.getName());
		}

		Long projectId = existingEntity.getProject().getId();

		// Validate relationships if provided
		if (entityDto.getRelationships() != null && !entityDto.getRelationships().isEmpty()) {
			validateRelationships(entityDto.getRelationships(), projectId);
		}

		dtoMapper.updateEntityFromDto(entityDto, existingEntity);
		EntityDefinition savedEntity = entityRepository.save(existingEntity);

		// Update relationships if provided
		if (entityDto.getRelationships() != null) {
			// Remove existing relationships
			relationshipRepository.deleteByEntityId(savedEntity.getId());
			savedEntity.getRelationships().clear();

			// Save new relationships
			if (!entityDto.getRelationships().isEmpty()) {
				saveRelationships(entityDto.getRelationships(), savedEntity);
			}
		}

		return savedEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntity(Long id) {
		if (!entityRepository.existsById(id)) {
			throw new ProjectNotFoundException("Entity", id.toString());
		}
		entityRepository.deleteById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsByNameAndProjectId(String name, Long projectId) {
		return entityRepository.existsByNameAndProjectId(name, projectId);
	}

	/**
	 * Validates a list of relationship definitions for an entity within a project.
	 * Checks relationship count limit, target entity existence, and cascade type validity.
	 *
	 * @param relationships the list of relationship DTOs to validate
	 * @param projectId     the ID of the project containing the entity
	 */
	private void validateRelationships(List<RelationshipDefinitionDto> relationships, Long projectId) {
		// Check relationship count limit
		if (relationships.size() > MessageConstants.MAX_RELATIONSHIPS_PER_ENTITY) {
			throw new ValidationException(
					String.format(MessageConstants.MAX_RELATIONSHIPS_EXCEEDED,
							MessageConstants.MAX_RELATIONSHIPS_PER_ENTITY, relationships.size()));
		}

		for (RelationshipDefinitionDto rel : relationships) {
			// Validate target entity exists within the same project
			if (!entityRepository.existsByNameAndProjectId(rel.getTargetEntity(), projectId)) {
				throw new ValidationException(
						String.format(MessageConstants.INVALID_TARGET_ENTITY,
								rel.getFieldName(), rel.getTargetEntity()));
			}

			// Validate cascade types if provided
			if (rel.getCascadeTypes() != null && !rel.getCascadeTypes().isEmpty()) {
				for (com.user.driven.operations.enums.CascadeType cascadeType : rel.getCascadeTypes()) {
					if (!VALID_CASCADE_TYPES.contains(cascadeType.name())) {
						throw new ValidationException(
								String.format(MessageConstants.INVALID_CASCADE_TYPE,
										cascadeType.name(), rel.getFieldName()));
					}
				}
			}
		}
	}

	/**
	 * Saves a list of relationship definitions for a given entity.
	 * Maps each DTO to a RelationshipDefinition entity, sets the entity reference,
	 * and persists all relationships.
	 *
	 * @param relationships the list of relationship DTOs to save
	 * @param entity        the entity that owns these relationships
	 */
	private void saveRelationships(List<RelationshipDefinitionDto> relationships, EntityDefinition entity) {
		List<RelationshipDefinition> relationshipEntities = relationships.stream()
				.map(dto -> {
					RelationshipDefinition rel = dtoMapper.toEntity(dto);
					rel.setEntity(entity);
					return rel;
				})
				.collect(Collectors.toList());

		relationshipRepository.saveAll(relationshipEntities);
		entity.getRelationships().addAll(relationshipEntities);
	}
}