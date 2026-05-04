package com.user.driven.operations.generator.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.user.driven.operations.core.model.EntityDefinition;
import com.user.driven.operations.core.model.ProjectDefinition;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * CodeGenerator is responsible for generating a complete Java Spring Boot project
 * based on provided metadata using Freemarker templates.
 * It supports generation of entities, repositories, services, controllers,
 * configurations, and security mechanisms.
 * 
 * @author Jatin Raheja
 */
@Component
public class CodeGenerator {

	private final Configuration freemarkerConfig;

	/**
	 * Constructor initializes Freemarker configuration for template processing.
	 */
	public CodeGenerator() {
		this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
		this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
		this.freemarkerConfig.setDefaultEncoding("UTF-8");
	}

	/**
	 * Generates an entire Spring Boot project based on the provided project definition.
	 * 
	 * @param project     The project metadata.
	 * @param projectPath The root path where the project should be created.
	 * @throws IOException if an I/O error occurs during generation.
	 */
	public void generateProject(ProjectDefinition project, Path projectPath) throws IOException {
		// Create project directory structure
		createProjectStructure(projectPath, project.getPackageName());

		// Generate pom.xml
		generatePomXml(project, projectPath);

		// Generate application.properties
		generateApplicationProperties(project, projectPath);

		// Generate main application class
		generateMainClass(project, projectPath);

		// Generate entities, repositories, services, controllers for each entity
		for (EntityDefinition entity : project.getEntities()) {
			generateEntityFiles(project, entity, projectPath);
		}

		// Generate security files
		generateSecurityFiles(project, projectPath);

		// Generate swagger configuration if enabled
		if (project.isSwaggerEnabled()) {
			generateSwaggerConfig(project, projectPath);
		}
	}

	/**
	 * Creates the basic folder structure for the project.
	 */
	private void createProjectStructure(Path projectPath, String packageName) throws IOException {
		String packagePath = packageName.replace(".", "/");

		// Create main directories
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath));
		Files.createDirectories(projectPath.resolve("src/main/resources"));
		Files.createDirectories(projectPath.resolve("src/test/java/" + packagePath));

		// Create package subdirectories
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/model"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/repository"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/service"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/controller"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/config"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/dto"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/security"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/payload/request"));
		Files.createDirectories(projectPath.resolve("src/main/java/" + packagePath + "/payload/response"));
	}

	/**
	 * Generates the full set of JWT-related security files.
	 */
	private void generateJwtSecurityFiles(ProjectDefinition project, Path projectPath) throws IOException {
		String packagePath = project.getPackageName().replace(".", "/");
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		// Generate JWT utility classes
		generateFile("JwtUtils.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/security/JwtUtils.java"));
		generateFile("JwtAuthenticationFilter.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/security/JwtAuthenticationFilter.java"));
		generateFile("JwtAuthenticationEntryPoint.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/security/JwtAuthenticationEntryPoint.java"));
		generateFile("UserPrincipal.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/security/UserPrincipal.java"));

		// Generate User and Role entities
		generateFile("User.java.ftl", model, projectPath.resolve("src/main/java/" + packagePath + "/model/User.java"));
		generateFile("Role.java.ftl", model, projectPath.resolve("src/main/java/" + packagePath + "/model/Role.java"));
		generateFile("ERole.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/model/ERole.java"));

		// Generate repositories
		generateFile("UserRepository.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/repository/UserRepository.java"));
		generateFile("RoleRepository.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/repository/RoleRepository.java"));

		// Generate UserDetailsService
		generateFile("UserDetailsServiceImpl.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/service/UserDetailsServiceImpl.java"));

		// Generate Auth Controller
		generateFile("AuthController.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/controller/AuthController.java"));

		// Generate request/response DTOs
		generateFile("LoginRequest.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/payload/request/LoginRequest.java"));
		generateFile("SignupRequest.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/payload/request/SignupRequest.java"));
		generateFile("UserInfoResponse.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/payload/response/UserInfoResponse.java"));
		generateFile("MessageResponse.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/payload/response/MessageResponse.java"));
	}

	/**
	 * Generates security configuration and delegates to appropriate method depending on the selected security type.
	 */
	private void generateSecurityFiles(ProjectDefinition project, Path projectPath) throws IOException {
		if (!project.isSecurityEnabled()) {
			return;
		}

		String packagePath = project.getPackageName().replace(".", "/");
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		// Generate security configuration
		generateSecurityConfig(project, projectPath);

		// Generate security-specific files based on type
		switch (project.getSecurityType()) {
		case JWT:
			generateJwtSecurityFiles(project, projectPath);
			break;
		case OAUTH2:
			generateOAuth2SecurityFiles(project, projectPath);
			break;
		case SESSION_BASED:
			generateSessionSecurityFiles(project, projectPath);
			break;
		case BASIC_AUTH:
			generateBasicAuthSecurityFiles(project, projectPath);
			break;
		}
	}

	/**
	 * Generates OAuth2-related security files.
	 */
	private void generateOAuth2SecurityFiles(ProjectDefinition project, Path projectPath) throws IOException {
		String packagePath = project.getPackageName().replace(".", "/");
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		generateFile("OAuth2Config.java.ftl", model,
				projectPath.resolve("src/main/java/" + packagePath + "/config/OAuth2Config.java"));
	}

	/**
	 * Generates configuration files for session-based security using Spring Security.
	 * 
	 * @param project     The project definition containing metadata.
	 * @param projectPath The root path where files should be generated.
	 * @throws IOException If file writing fails.
	 */
	private void generateSessionSecurityFiles(ProjectDefinition project, Path projectPath) throws IOException {
		String packagePath = project.getPackageName().replace(".", "/");
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		// Session-based security configuration template
		generateFile("SessionSecurityConfig.java.ftl", model,
			projectPath.resolve("src/main/java/" + packagePath + "/config/SessionSecurityConfig.java"));

		// Login and logout controller
		generateFile("SessionAuthController.java.ftl", model,
			projectPath.resolve("src/main/java/" + packagePath + "/controller/SessionAuthController.java"));
	}

	/**
	 * Generates configuration files for Basic Authentication using Spring Security.
	 * 
	 * @param project     The project definition containing metadata.
	 * @param projectPath The root path where files should be generated.
	 * @throws IOException If file writing fails.
	 */
	private void generateBasicAuthSecurityFiles(ProjectDefinition project, Path projectPath) throws IOException {
		String packagePath = project.getPackageName().replace(".", "/");
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		// Basic auth configuration template
		generateFile("BasicAuthSecurityConfig.java.ftl", model,
			projectPath.resolve("src/main/java/" + packagePath + "/config/BasicAuthSecurityConfig.java"));
	}

	/**
	 * Writes a generated file to the given path using a Freemarker template.
	 */
	private void generateFile(String templateName, Map<String, Object> model, Path outputPath) throws IOException {
		String content = processTemplate(templateName, model);
		Files.writeString(outputPath, content);
	}

	/**
	 * Generates the Maven POM file.
	 */
	private void generatePomXml(ProjectDefinition project, Path projectPath) throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		String content = processTemplate("pom.xml.ftl", model);
		Files.writeString(projectPath.resolve("pom.xml"), content);
	}

	/**
	 * Generates the application.properties file.
	 */
	private void generateApplicationProperties(ProjectDefinition project, Path projectPath) throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		String content = processTemplate("application.properties.ftl", model);
		Files.writeString(projectPath.resolve("src/main/resources/application.properties"), content);
	}

	/**
	 * Generates all related files (entity, repo, service, controller, DTO) for a given entity.
	 */
	private void generateMainClass(ProjectDefinition project, Path projectPath) throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		String content = processTemplate("Application.java.ftl", model);
		String packagePath = project.getPackageName().replace(".", "/");
		String className = toPascalCase(project.getName()) + "Application";
		Files.writeString(projectPath.resolve("src/main/java/" + packagePath + "/" + className + ".java"), content);
	}

	private void generateEntityFiles(ProjectDefinition project, EntityDefinition entity, Path projectPath)
			throws IOException {
		String packagePath = project.getPackageName().replace(".", "/");

		// Generate Entity
		generateEntity(project, entity, projectPath.resolve("src/main/java/" + packagePath + "/model"));

		// Generate Repository
		generateRepository(project, entity, projectPath.resolve("src/main/java/" + packagePath + "/repository"));

		// Generate Service Interface and Implementation
		generateService(project, entity, projectPath.resolve("src/main/java/" + packagePath + "/service"));

		// Generate Controller
		generateController(project, entity, projectPath.resolve("src/main/java/" + packagePath + "/controller"));

		// Generate DTO
		generateDto(project, entity, projectPath.resolve("src/main/java/" + packagePath + "/dto"));
	}

	private void generateEntity(ProjectDefinition project, EntityDefinition entity, Path entityPath)
			throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);
		model.put("entity", entity);

		String content = processTemplate("Entity.java.ftl", model);
		Files.writeString(entityPath.resolve(toPascalCase(entity.getName()) + ".java"), content);
	}

	private void generateRepository(ProjectDefinition project, EntityDefinition entity, Path repositoryPath)
			throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);
		model.put("entity", entity);

		String content = processTemplate("Repository.java.ftl", model);
		Files.writeString(repositoryPath.resolve(toPascalCase(entity.getName()) + "Repository.java"), content);
	}

	private void generateService(ProjectDefinition project, EntityDefinition entity, Path servicePath)
			throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);
		model.put("entity", entity);

		// Generate Service Interface
		String interfaceContent = processTemplate("Service.java.ftl", model);
		Files.writeString(servicePath.resolve(toPascalCase(entity.getName()) + "Service.java"), interfaceContent);

		// Generate Service Implementation
		String implContent = processTemplate("ServiceImpl.java.ftl", model);
		Files.createDirectories(servicePath.resolve("impl"));
		Files.writeString(servicePath.resolve("impl/" + toPascalCase(entity.getName()) + "ServiceImpl.java"),
				implContent);
	}

	private void generateController(ProjectDefinition project, EntityDefinition entity, Path controllerPath)
			throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);
		model.put("entity", entity);

		String content = processTemplate("Controller.java.ftl", model);
		Files.writeString(controllerPath.resolve(toPascalCase(entity.getName()) + "Controller.java"), content);
	}

	private void generateDto(ProjectDefinition project, EntityDefinition entity, Path dtoPath) throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);
		model.put("entity", entity);

		String content = processTemplate("Dto.java.ftl", model);
		Files.writeString(dtoPath.resolve(toPascalCase(entity.getName()) + "Dto.java"), content);
	}

	private void generateSecurityConfig(ProjectDefinition project, Path projectPath) throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		String content = processTemplate("SecurityConfig.java.ftl", model);
		String packagePath = project.getPackageName().replace(".", "/");
		Files.writeString(projectPath.resolve("src/main/java/" + packagePath + "/config/SecurityConfig.java"), content);
	}

	private void generateSwaggerConfig(ProjectDefinition project, Path projectPath) throws IOException {
		Map<String, Object> model = new HashMap<>();
		model.put("project", project);

		String content = processTemplate("SwaggerConfig.java.ftl", model);
		String packagePath = project.getPackageName().replace(".", "/");
		Files.writeString(projectPath.resolve("src/main/java/" + packagePath + "/config/SwaggerConfig.java"), content);
	}

	/**
	 * Processes a Freemarker template with the given model.
	 * 
	 * @param templateName Template file name.
	 * @param model        Data model to populate the template.
	 * @return Rendered template as a String.
	 * @throws IOException if the template processing fails.
	 */
	private String processTemplate(String templateName, Map<String, Object> model) throws IOException {
		try {
			Template template = freemarkerConfig.getTemplate(templateName);
			StringWriter writer = new StringWriter();
			template.process(model, writer);
			return writer.toString();
		} catch (TemplateException e) {
			throw new IOException("Error processing template: " + templateName, e);
		}
	}

	/**
	 * Converts a string to PascalCase format.
	 * 
	 * @param input The input string.
	 * @return PascalCase version of the input.
	 */
	private String toPascalCase(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		StringBuilder result = new StringBuilder();
		boolean capitalizeNext = true;

		for (char c : input.toCharArray()) {
			if (Character.isLetterOrDigit(c)) {
				if (capitalizeNext) {
					result.append(Character.toUpperCase(c));
					capitalizeNext = false;
				} else {
					result.append(Character.toLowerCase(c));
				}
			} else {
				capitalizeNext = true;
			}
		}

		return result.toString();
	}
}
