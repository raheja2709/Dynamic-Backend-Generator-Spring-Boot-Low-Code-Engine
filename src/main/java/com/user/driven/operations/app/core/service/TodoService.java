package com.user.driven.operations.app.core.service;

import com.user.driven.operations.app.api.dto.ApiResponseDTO;
import com.user.driven.operations.app.api.dto.TodoItemDTO;

public interface TodoService {

	public ApiResponseDTO createTodoItem(TodoItemDTO todoItemDTO);
}
