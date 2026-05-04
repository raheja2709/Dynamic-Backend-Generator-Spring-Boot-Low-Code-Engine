package com.user.driven.operations.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.driven.operations.api.dto.ApiResponseDTO;
import com.user.driven.operations.api.dto.TodoItemDTO;
import com.user.driven.operations.core.service.TodoService;
import com.user.driven.operations.common.util.ApiPathConstants;

import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiPathConstants.TODO_API)
public class TodoController {

	@Autowired
	TodoService todoService;

	@PostMapping
	public ApiResponseDTO creatTodoItem(@Valid @RequestBody TodoItemDTO todoItem) {
		return todoService.createTodoItem(todoItem);
	}
}
