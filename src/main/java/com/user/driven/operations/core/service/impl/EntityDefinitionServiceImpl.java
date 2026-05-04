package com.user.driven.operations.core.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.driven.operations.api.dto.EntityDefinitionDto;
import com.user.driven.operations.core.model.EntityDefinition;
import com.user.driven.operations.core.model.ProjectDefinition;
import com.user.driven.operations.api.mapper.DtoMapper;
import com.user.driven.operations.core.repository.EntityDefinitionRepository;
import com.user.driven.operations.core.repository.FieldDefinitionRepository;
import com.user.driven.operations.core.repository.OperationConfigRepository;
import com.user.driven.operations.core.repository.ProjectDefinitionRepository;
import com.user.driven.operations.core.service.EntityDefinitionService;

/**
 * Service implementation for managing {@link EntityDefinition}.
 * Handles business logic for creating, retrieving, updating, and deleting entities,
 * as well as managing relationships with fields and operations.
 *
 * @author: Jatin Raheja
 */
@Service
@Transactional
public class EntityDefinitionServiceImpl implements EntityDefinitionService {

	@Autowired
	private OperationConfigRepository operationRepository;

	@Autowired
	private FieldDefinitionRepository fieldRepository;

	@Autowired
	private EntityDefinitionRepository entityRepository;

	@Autowired
	private ProjectDefinitionRepository projectRepository;

	@Autowired
	private DtoMapper dtoMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityDefinition createEntity(Long projectId, EntityDefinitionDto entityDto) {
		ProjectDefinition project = projectRepository.findById(projectId)
				.orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

		if (existsByNameAndProjectId(entityDto.getName(), projectId)) {
			throw new RuntimeException("Entity with name '" + entityDto.getName() + "' already exists in this project");
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
	public EntityDefinition updateEntity(Long id, EntityDefinitionDto entityDto) {
		EntityDefinition existingEntity = entityRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));

		if (!existingEntity.getName().equals(entityDto.getName())
				&& existsByNameAndProjectId(entityDto.getName(), existingEntity.getProject().getId())) {
			throw new RuntimeException("Entity with name '" + entityDto.getName() + "' already exists in this project");
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
			throw new RuntimeException("Entity not found with id: " + id);
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