package com.user.driven.operations.app.core.service.impl;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.driven.operations.app.api.dto.EntityDefinitionDto;
import com.user.driven.operations.app.common.exception.DuplicateNameException;
import com.user.driven.operations.app.common.exception.ProjectNotFoundException;
import com.user.driven.operations.app.common.exception.ValidationException;
import com.user.driven.operations.app.common.util.MessageConstants;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.api.mapper.DtoMapper;
import com.user.driven.operations.app.core.repository.EntityDefinitionRepository;
import com.user.driven.operations.app.core.repository.FieldDefinitionRepository;
import com.user.driven.operations.app.core.repository.OperationConfigRepository;
import com.user.driven.operations.app.core.repository.ProjectDefinitionRepository;
import com.user.driven.operations.app.core.service.EntityDefinitionService;

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

	private final DtoMapper dtoMapper;

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

		EntityDefinition entity = dtoMapper.toEntity(entityDto);
		entity.setProject(project);
		return entityRepository.save(entity);
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

		dtoMapper.updateEntityFromDto(entityDto, existingEntity);
		return entityRepository.save(existingEntity);
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
}