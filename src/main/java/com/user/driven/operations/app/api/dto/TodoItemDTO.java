package com.user.driven.operations.app.api.dto;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemDTO {

	private Integer id;
	@NotNull(message = "Title Cannot be Null or Empty")
	@NotBlank(message = "Title Cannot be Null or Empty")
	private String title;
	private String description;
	private Date dueDate;
	private boolean isCompleted = false;
}
