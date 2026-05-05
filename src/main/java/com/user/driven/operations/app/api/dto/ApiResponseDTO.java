package com.user.driven.operations.app.api.dto;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDTO {

	private HttpStatusCode status;
	private TodoItemDTO responseBody;
	private boolean isError;
}
