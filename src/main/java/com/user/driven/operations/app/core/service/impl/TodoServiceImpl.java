package com.user.driven.operations.app.core.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.user.driven.operations.app.api.dto.ApiResponseDTO;
import com.user.driven.operations.app.api.dto.TodoItemDTO;
import com.user.driven.operations.app.core.model.TodoItem;
import com.user.driven.operations.app.core.repository.TodoItemRepository;
import com.user.driven.operations.app.core.service.TodoService;

@Service
public class TodoServiceImpl implements TodoService {

	@Autowired
	TodoItemRepository todoItemRepository;

	@Override
	public ApiResponseDTO createTodoItem(TodoItemDTO todoItemDTO) {
		TodoItem todoItem = new TodoItem();
		BeanUtils.copyProperties(todoItemDTO, todoItem);
		todoItem = todoItemRepository.save(todoItem);
		BeanUtils.copyProperties(todoItem, todoItemDTO);

		ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
		apiResponseDTO.setStatus(HttpStatus.CREATED);
		apiResponseDTO.setResponseBody(todoItemDTO);
		apiResponseDTO.setError(false);
		return apiResponseDTO;
	}
}
