package com.user.driven.operations.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuration class for setting up Swagger/OpenAPI documentation.
 * <p>
 * This class defines the OpenAPI bean with custom metadata such as title,
 * version, description, and contact information for the API documentation.
 * </p>
 * 
 * @author Jatin Raheja
 */
@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("Operation Management System API").version("1.0.0")
				.description("User-Driven Operation Management System for generating Spring Boot projects")
				.contact(new Contact().name("Development Team").email("jatin.raheja.dev@example.com")));
	}
}