package com.user.driven.operations.app.core.service.impl;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.driven.operations.app.api.dto.ProjectDefinitionDto;
import com.user.driven.operations.app.common.exception.DuplicateNameException;
import com.user.driven.operations.app.common.exception.ProjectNotFoundException;
import com.user.driven.operations.app.core.model.EntityDefinition;
import com.user.driven.operations.app.core.model.ProjectDefinition;
import com.user.driven.operations.app.api.mapper.DtoMapper;
import com.user.driven.operations.app.core.repository.FieldDefinitionRepository;
import com.user.driven.operations.app.core.repository.OperationConfigRepository;
import com.user.driven.operations.app.core.repository.ProjectDefinitionRepository;
import com.user.driven.operations.app.core.service.ProjectDefinitionService;

/**
 * Implementation of {@link ProjectDefinitionService} for managing project definitions.
 * This service handles creation, retrieval, update, and deletion of projects,
 * as well as eager loading of associated entities, fields, and operations.
 * 
 * @author Jatin Raheja
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectDefinitionServiceImpl implements ProjectDefinitionService {

	private final FieldDefinitionRepository fieldRepository;

	private final OperationConfigRepository operationRepository;

	private final ProjectDefinitionRepository projectRepository;

	private final DtoMapper dtoMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProjectDefinition createProject(ProjectDefinitionDto projectDto) {
		if (existsByName(projectDto.getName())) {
			throw new DuplicateNameException("Project", projectDto.getName());
		}

		ProjectDefinition project = dtoMapper.toEntity(projectDto);
		return projectRepository.save(project);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ProjectDefinition> getProjectById(Long id) {
		return projectRepository.findById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<ProjectDefinition> getProjectByIdWithEntities(Long id) {
		Optional<ProjectDefinition> projectOpt = projectRepository.findByIdWithEntities(id);

		if (projectOpt.isPresent()) {
			ProjectDefinition project = projectOpt.get();

			// Manually load fields and operations for each entity to avoid
			// MultipleBagFetchException
			for (EntityDefinition entity : project.getEntities()) {
				// Load fields
				entity.setFields(fieldRepository.findByEntityId(entity.getId()));
				// Load operations
				entity.setOperations(operationRepository.findByEntityId(entity.getId()));
			}
		}

		return projectOpt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ProjectDefinition> getAllProjects() {
		return projectRepository.findAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProjectDefinition updateProject(Long id, ProjectDefinitionDto projectDto) {
		ProjectDefinition existingProject = projectRepository.findById(id)
				.orElseThrow(() -> new ProjectNotFoundException("Project", id.toString()));

		if (!existingProject.getName().equals(projectDto.getName()) && existsByName(projectDto.getName())) {
			throw new DuplicateNameException("Project", projectDto.getName());
		}

		dtoMapper.updateEntityFromDto(projectDto, existingProject);
		return projectRepository.save(existingProject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteProject(Long id) {
		if (!projectRepository.existsById(id)) {
			throw new ProjectNotFoundException("Project", id.toString());
		}
		projectRepository.deleteById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return projectRepository.existsByName(name);
	}
}