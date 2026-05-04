package com.user.driven.operations.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.driven.operations.core.model.TodoItem;

public interface TodoItemRepository extends JpaRepository<TodoItem, Integer> {

}
