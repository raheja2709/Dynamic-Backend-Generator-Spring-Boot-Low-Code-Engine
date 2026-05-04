package com.user.driven.operations.core.service;

import com.user.driven.operations.api.dto.ApiResponseDTO;
import com.user.driven.operations.api.dto.TodoItemDTO;

public interface TodoService {

	public ApiResponseDTO createTodoItem(TodoItemDTO todoItemDTO);
}
