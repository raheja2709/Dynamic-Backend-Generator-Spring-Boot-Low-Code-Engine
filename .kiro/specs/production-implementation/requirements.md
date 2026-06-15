# Requirements Document

## Introduction

The Dynamic Backend Generator is a Spring Boot Low-Code Engine that generates complete backend projects from entity definitions provided via API. This requirements document covers the full production implementation plan to transform the existing prototype into a production-ready, sellable product. The plan spans 11 phases: foundation cleanup, multi-environment configuration, error handling with database logging, security and authentication, API improvements, relationship handling (model, templates, DTOs, and generator completeness), async generation with file cleanup, comprehensive test coverage, documentation, dockerization, and frontend generation.

## Glossary

- **Generator**: The Spring Boot application that accepts project/entity definitions and produces complete Spring Boot projects as ZIP files
- **Generated_Project**: The output Spring Boot application produced by the Generator from entity and project definitions
- **Project_Definition**: A configuration object specifying project-level settings (name, package, database type, security type, caching, swagger)
- **Entity_Definition**: A model describing a database entity within a project, including its fields, operations, and relationships
- **Field_Definition**: A specification of a single column/property on an entity, including data type, validation rules, and defaults
- **Operation_Config**: A declaration of what operations (CRUD, bulk, export, import, etc.) an entity supports in the generated project
- **Relationship_Definition**: A specification of a JPA relationship between two entities, including type, fetch strategy, cascade behavior, and DTO representation strategy
- **DTO_Strategy**: The approach for representing a relationship in Data Transfer Objects (ID_ONLY, SUMMARY, NESTED, or IGNORE)
- **Generation_Job**: An asynchronous task representing a project generation request, tracking status, progress, and output location
- **Audit_Log**: A database record capturing details of every API request including method, endpoint, duration, and outcome
- **Application_Log**: A database record capturing ERROR and WARN level log entries with contextual metadata
- **Flyway_Migration**: A versioned SQL script that defines or alters the database schema, executed in order on application startup
- **FreeMarker_Template**: A template file using the FreeMarker engine to generate Java source code, configuration files, or build files for Generated_Projects
- **Host_API**: The REST API exposed by the Generator application itself (distinct from APIs in Generated_Projects)
- **Build_Verifier**: The component that compiles the Generated_Project using Maven to confirm correctness before delivering the ZIP
- **Security_Generator**: A component that produces security-related source files (config, filters, controllers, entities) for a Generated_Project based on the selected security type
- **Template_Engine**: The FreeMarker-based component responsible for rendering templates into source code using model data
- **Frontend_Generator**: The component responsible for producing React frontend source code for Generated_Projects

## Requirements

### Requirement 1: Codebase Cleanup

**User Story:** As a developer, I want the codebase free of dead code, hardcoded values, and inconsistencies, so that the foundation is clean and maintainable before adding production features.

#### Acceptance Criteria

1. THE Generator SHALL not contain any dead code including TestController, TodoController, TodoService, TodoServiceImpl, TodoItem, TodoItemDTO, TodoItemRepository, or ApiResponseDTO classes, and the project SHALL compile successfully after their removal
2. THE Generator SHALL use SLF4J placeholder logging for all log statements in production source files (src/main/java) instead of System.out.println or e.printStackTrace calls
3. THE Generator SHALL use constructor injection for all Spring bean dependencies in controller, service, and configuration classes instead of field-level @Autowired annotations
4. WHEN the Build_Verifier resolves the Maven executable path and no Maven Wrapper is present in the generated project, THE Build_Verifier SHALL read the fallback Maven path from a Spring application property (e.g., app.maven.executable) instead of using a hardcoded file system path
5. THE Generator SHALL not contain commented-out code blocks (2 or more consecutive commented lines) in SecurityConfig, ProjectGenerationServiceImpl, or pom.xml
6. THE Generator SHALL not include the spring-security-test dependency while the spring-boot-starter-security dependency is absent

### Requirement 2: Flyway Database Migration

**User Story:** As a developer, I want all database schema changes managed through Flyway migrations, so that schema evolution is versioned, repeatable, and safe for production deployments.

#### Acceptance Criteria

1. THE Generator SHALL include the flyway-core and flyway-database-postgresql dependencies in the Maven build
2. WHEN the Generator starts in production profile, THE Generator SHALL execute Flyway migrations from the classpath db/migration directory to create the database schema before Hibernate validation runs
3. WHILE the Generator is running in production profile, THE Generator SHALL set Hibernate ddl-auto to validate so that Flyway is the sole schema management mechanism
4. THE Flyway_Migration V1 SHALL create tables project_definitions, entity_definitions, field_definitions, and operation_configs each with a BIGSERIAL primary key, foreign keys linking entity_definitions.project_id to project_definitions.id, field_definitions.entity_id to entity_definitions.id, and operation_configs.entity_id to entity_definitions.id with ON DELETE CASCADE, and indexes on each foreign key column
5. IF a Flyway_Migration fails, THEN THE Generator SHALL prevent application startup and log the migration error including the failed script name and line number
6. WHILE the Generator is running in development profile, THE Generator SHALL set Hibernate ddl-auto to create-drop and disable Flyway automatic migration so that an in-memory database is used without migration scripts

### Requirement 3: Multi-Environment Configuration

**User Story:** As a DevOps engineer, I want environment-specific configuration profiles, so that the Generator can be deployed across development, production, and Docker environments without code changes.

#### Acceptance Criteria

1. THE Generator SHALL support three Spring profiles: dev, prod, and docker, where activating any one of the three profiles results in a successful application startup with profile-specific configuration loaded
2. WHILE the dev profile is active, THE Generator SHALL use H2 in-memory database with ddl-auto set to create-drop and server port 8083
3. WHILE the prod profile is active, THE Generator SHALL use PostgreSQL with ddl-auto set to validate and Flyway enabled, and server port 8080
4. WHILE the docker profile is active, THE Generator SHALL resolve the database connection URL using the Docker Compose service name (e.g., "db") as the hostname instead of "localhost"
5. THE Generator SHALL externalize database URL, username, and password as environment variables using the pattern `${ENV_VAR:default}`, where defaults are: database URL defaulting to `jdbc:h2:mem:testdb`, username defaulting to `sa`, and password defaulting to empty string
6. THE Generator SHALL provide a `@ConfigurationProperties`-annotated class exposing generated-projects directory path (default: `./generated-projects`) and Maven executable path (default: `mvn`) as injectable beans
7. THE Generator SHALL include a .env.example file listing every externalized environment variable with a one-line description and its default value
8. IF no Spring profile is explicitly activated, THEN THE Generator SHALL default to the dev profile
9. IF a required environment variable (database URL, username, or password) has no value and no default in the active prod or docker profile, THEN THE Generator SHALL fail to start and log an error message indicating the missing variable name

### Requirement 4: Custom Exception Hierarchy

**User Story:** As a developer, I want a structured exception hierarchy with meaningful error responses, so that API consumers receive actionable error information and errors are traceable through the system.

#### Acceptance Criteria

1. THE Generator SHALL define a base exception class from which all custom exceptions (ProjectNotFoundException, DuplicateNameException, GenerationFailedException, ValidationException, BuildVerificationException, UnsupportedSecurityTypeException) inherit
2. WHEN an exception occurs during API request processing, THE GlobalExceptionHandler SHALL return a consistent ErrorResponse containing timestamp, HTTP status, error type, message, request path, request ID, and field-level validation errors (included only for MethodArgumentNotValidException and ValidationException; omitted or empty for all other exception types)
3. WHEN a MethodArgumentNotValidException occurs, THE GlobalExceptionHandler SHALL return HTTP 400 with an array of field errors each containing the field name, rejection message, and rejected value
4. WHEN a DataIntegrityViolationException occurs, THE GlobalExceptionHandler SHALL return HTTP 409 with a message identifying the constraint violation
5. WHEN a ProjectNotFoundException occurs, THE GlobalExceptionHandler SHALL return HTTP 404 with the resource type and identifier in the message
6. THE Generator SHALL not use RuntimeException directly for business logic failures; each failure mode SHALL use a specific custom exception carrying at minimum the resource type, the resource identifier or input that caused the failure, and the operation stage where the failure occurred
7. WHEN a DuplicateNameException occurs, THE GlobalExceptionHandler SHALL return HTTP 409 with the resource type and duplicate value in the message; WHEN a ValidationException occurs, THE GlobalExceptionHandler SHALL return HTTP 400; WHEN a GenerationFailedException occurs, THE GlobalExceptionHandler SHALL return HTTP 500 with the generation stage in the message; WHEN a BuildVerificationException occurs, THE GlobalExceptionHandler SHALL return HTTP 500 with the build stage in the message; WHEN an UnsupportedSecurityTypeException occurs, THE GlobalExceptionHandler SHALL return HTTP 400 with the unsupported type value in the message
8. IF an exception occurs that is not matched by any specific exception handler, THEN THE GlobalExceptionHandler SHALL return HTTP 500 with a generic error message that does not expose internal implementation details, stack traces, or class names
9. WHEN any exception is handled, THE GlobalExceptionHandler SHALL include the X-Request-Id value in the ErrorResponse so that the error is traceable to the originating request

### Requirement 5: Database Audit Logging

**User Story:** As an operator, I want every API request and application error persisted to the database with contextual metadata, so that issues are diagnosable and usage patterns are auditable.

#### Acceptance Criteria

1. WHEN any API request is received, THE AuditInterceptor SHALL create an Audit_Log record containing the request ID (UUID), HTTP method, endpoint path (max 500 characters), request body (truncated to 10,000 characters if larger), response status, duration in milliseconds, client IP address, and user agent (max 500 characters)
2. WHEN an incoming request is received, THE Generator SHALL generate a unique X-Request-Id UUID and include the value in the response headers and all log entries via MDC propagation
3. WHEN an ERROR or WARN level log event occurs, THE DatabaseAppender SHALL write an Application_Log record containing the log level, logger name, message, stack trace, request ID, user ID, and JSONB context
4. WHEN the generation pipeline transitions to a new stage, THE Generator SHALL update the Audit_Log record for the current request with the stage value (one of VALIDATION, STRUCTURE, POM_GENERATION, APP_GENERATION, ENTITY_GENERATION, SECURITY_GENERATION, BUILD_VERIFICATION, ZIP)
5. THE Generator SHALL execute a scheduled cleanup job once per day that deletes Audit_Log and Application_Log records older than 30 days
6. THE Generator SHALL configure Logback with console, rolling-file (max 10 MB per file, retaining 30 days of history), and database appenders, using JSON format output when the prod or docker profile is active
7. IF the AuditInterceptor or DatabaseAppender fails to persist a log record, THEN THE Generator SHALL log the failure to the console appender and continue processing the request without interruption

### Requirement 6: Host API Authentication and Authorization

**User Story:** As a platform owner, I want the Host API secured with JWT authentication and API key support with role-based access control, so that only authorized users can create projects and access generation features.

#### Acceptance Criteria

1. WHEN a user registers via POST /api/v1/auth/register with a valid email, password, and full name, THE AuthService SHALL create an AppUser record with a BCrypt-hashed password and default USER role, and return the created user details (excluding the password hash) with HTTP 201
2. WHEN a user authenticates via POST /api/v1/auth/login with valid credentials, THE AuthService SHALL return a JWT access token with an expiration of 15 minutes and a refresh token with an expiration of 7 days
3. WHEN a request includes a valid, non-expired JWT in the Authorization Bearer header, THE JwtAuthenticationFilter SHALL authenticate the request and populate the security context with user details and roles
4. IF a request includes an expired, malformed, or invalid JWT in the Authorization Bearer header, THEN THE JwtAuthenticationFilter SHALL reject the request with HTTP 401 and an error message indicating the authentication failure reason
5. WHEN a request includes a valid API key in the X-API-Key header, THE ApiKeyAuthenticationFilter SHALL authenticate the request and populate the security context with the associated user's details and roles
6. WHILE a user has the USER role, THE Generator SHALL restrict project creation, listing, update, deletion, and generation operations to projects owned by that user only
7. IF a user with the USER role attempts to access a project not owned by that user, THEN THE Generator SHALL reject the request with HTTP 403
8. WHILE a user has the ADMIN role, THE Generator SHALL permit access to all projects and all user management operations
9. THE SecurityFilterChain SHALL permit unauthenticated access to /api/v1/auth/**, /swagger-ui/**, /api-docs/**, and /actuator/health endpoints only
10. IF an authenticated user exceeds 10 requests per minute on the generation endpoint, THEN THE Generator SHALL reject subsequent requests with HTTP 429 until the rate limit window resets
11. WHEN a user provides a password shorter than 8 characters, longer than 128 characters, or missing at least one uppercase letter, one lowercase letter, one digit, or one special character, THE AuthService SHALL reject registration with HTTP 400 and an error message indicating which password rules were violated
12. IF a user authenticates via POST /api/v1/auth/login with an invalid email or incorrect password, THEN THE AuthService SHALL reject the request with HTTP 401 and an error message indicating invalid credentials without revealing which field was incorrect

### Requirement 7: Security Generators for Output Projects

**User Story:** As a user defining a project, I want to select a security type and receive a fully working security implementation in my generated project, so that the Generated_Project is secure out of the box.

#### Acceptance Criteria

1. WHEN a Project_Definition has securityEnabled set to true and securityType set to JWT, THE JwtSecurityGenerator SHALL generate SecurityConfig, JwtFilter, JwtUtils, AuthController, User entity, UserDetailsService, LoginRequestDTO, and SignupRequestDTO in the Generated_Project
2. WHEN a Project_Definition has securityEnabled set to true and securityType set to BASIC_AUTH, THE BasicAuthSecurityGenerator SHALL generate a SecurityConfig with HTTP Basic authentication and a BCryptPasswordEncoder bean configuration
3. WHEN a Project_Definition has securityEnabled set to true and securityType set to OAUTH2, THE OAuth2SecurityGenerator SHALL generate a SecurityConfig with OAuth2 login and resource server configuration, and an application.properties fragment containing client-id, client-secret, and authorization-uri placeholder entries for at least one provider
4. WHEN a Project_Definition has securityEnabled set to true and securityType set to SESSION_BASED, THE SessionSecurityGenerator SHALL generate a SecurityConfig with form login, session management with a maximum of 1 concurrent session per user, and a login page HTML template
5. THE Generator SHALL organize security templates under /templates/security/{type}/ subdirectories (jwt/, basic/, oauth2/, session/)
6. IF a Project_Definition has securityEnabled set to true but securityType is null or not one of JWT, OAUTH2, SESSION_BASED, or BASIC_AUTH, THEN THE Generator SHALL throw an UnsupportedSecurityTypeException with a message listing the four valid security types
7. IF a Project_Definition has securityEnabled set to false, THEN THE Generator SHALL skip security file generation and produce no security-related classes in the Generated_Project

### Requirement 8: API Versioning and Response Standards

**User Story:** As an API consumer, I want versioned endpoints with consistent response envelopes and pagination, so that integration is predictable and forward-compatible.

#### Acceptance Criteria

1. THE Host_API SHALL serve all endpoints under the /api/v1/ path prefix
2. THE Host_API SHALL wrap all successful responses in an envelope containing success (boolean set to true), data (payload), pagination (included only when the endpoint returns a collection), timestamp (ISO-8601 UTC), and requestId (UUID) fields
3. WHEN a list endpoint is requested, THE Host_API SHALL accept page (zero-based, default 0), size (default 20, minimum 1, maximum 100), and sort (format: fieldName,asc|desc, default: createdAt,desc) query parameters and return pagination metadata containing page number, page size, total elements, and total pages
4. THE Host_API SHALL configure CORS with allowed origins, methods, and headers readable from application properties, defaulting to deny-all when no properties are configured
5. WHEN validation errors occur, THE Host_API SHALL return a response envelope with success set to false and a fieldErrors array where each entry contains field name, message, and rejected value
6. THE Host_API SHALL annotate all controller endpoints with OpenAPI @Operation, @ApiResponse, and @Schema annotations for Swagger documentation generation
7. THE Host_API SHALL enforce a maximum of 50 entities per project and 100 fields per entity, returning HTTP 400 with a response envelope containing success set to false and an error message indicating which limit was exceeded
8. WHEN filtering the project list endpoint, THE Host_API SHALL support filtering by name (case-insensitive partial match), databaseType (exact match), and sorting by createdAt via query parameters
9. IF a page or size query parameter value is outside its valid range, THEN THE Host_API SHALL return HTTP 400 with a response envelope containing success set to false and an error message indicating the accepted range
10. IF a non-validation error occurs, THEN THE Host_API SHALL return a response envelope with success set to false, an error field containing a message indicating the failure reason, timestamp (ISO-8601 UTC), and requestId (UUID)

### Requirement 9: Relationship Model

**User Story:** As a user, I want to define JPA relationships between entities with full control over fetch strategy, cascade behavior, and DTO representation, so that generated projects have production-quality data models.

#### Acceptance Criteria

1. THE Generator SHALL store relationship definitions in a dedicated relationship_definitions table separate from field_definitions
2. WHEN a user creates or updates an Entity_Definition, THE Host_API SHALL accept a list of up to 50 Relationship_Definition objects where each object requires relationship type (ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY), target entity name, and field name as mandatory fields, and accepts mapped-by field, fetch type (LAZY or EAGER, defaulting to LAZY), cascade types (defaulting to empty), orphan removal flag (defaulting to false), join column name, join table name, inverse join column name, nullable flag (defaulting to true), and DTO strategy (ID_ONLY, SUMMARY, NESTED, or IGNORE, defaulting to ID_ONLY) as optional fields
3. THE Generator SHALL support multiple cascade types per relationship (ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH) stored as a comma-separated list and SHALL deduplicate any repeated values before persisting
4. IF a relationship references a target entity that does not exist within the same project, THEN THE Host_API SHALL reject the create or update request and return a validation error response identifying the invalid target entity name and the relationship field name that references it
5. THE Generator SHALL deprecate the legacy relationshipType and relationshipTarget fields on Field_Definition by marking them as deprecated in the API schema while continuing to accept them on input, and SHALL provide a data migration that copies existing relationship data from field_definitions into the relationship_definitions table without deleting the original field_definitions rows
6. IF a Relationship_Definition specifies a cascade type value not in the set (ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH), THEN THE Host_API SHALL reject the request and return a validation error identifying the invalid cascade type value

### Requirement 10: Relationship Code Generation

**User Story:** As a user, I want the Generator to produce correct JPA relationship annotations with proper serialization handling, so that generated entities work correctly without circular reference issues.

#### Acceptance Criteria

1. WHEN an entity has a MANY_TO_ONE relationship, THE Template_Engine SHALL generate @ManyToOne with the fetch type from the relationship configuration (defaulting to FetchType.LAZY when not specified) and @JoinColumn with the column name from the relationship configuration (defaulting to the field name appended with "_id" in lowercase when not specified)
2. WHEN an entity has a ONE_TO_MANY relationship, THE Template_Engine SHALL generate @OneToMany with the mappedBy attribute set to the owning entity's field name, cascade type(s) from the relationship configuration, orphanRemoval from the relationship configuration, fetch type from the relationship configuration, and the field typed as List<TargetEntity>
3. WHEN an entity has a MANY_TO_MANY relationship, THE Template_Engine SHALL generate @ManyToMany with @JoinTable specifying the join table name, @JoinColumn with the owning entity join column name, and inverseJoinColumns with the target entity join column name, and the field typed as List<TargetEntity>
4. WHEN a bidirectional relationship exists, THE Template_Engine SHALL generate @JsonManagedReference on the non-owning side (the side declaring mappedBy) and @JsonBackReference on the owning side (the side declaring @JoinColumn or @JoinTable)
5. WHEN a single cascade type is specified, THE Template_Engine SHALL generate the cascade attribute as a single value (e.g., cascade = CascadeType.ALL), and WHEN multiple cascade types are specified, THE Template_Engine SHALL generate the cascade attribute as an array (e.g., cascade = {CascadeType.PERSIST, CascadeType.MERGE})
6. THE Template_Engine SHALL dynamically include only the Java import statements required by the annotations and types present on each entity, including jakarta.persistence imports, com.fasterxml.jackson.annotation imports, and java.util.List only when collection relationships exist
7. IF a relationship is unidirectional (no corresponding inverse mapping on the target entity), THEN THE Template_Engine SHALL omit @JsonManagedReference and @JsonBackReference annotations from that relationship field

### Requirement 11: DTO Relationship Strategies

**User Story:** As a user, I want the Generator to produce DTOs that handle relationships without circular references using configurable strategies, so that API responses are safe and appropriately shaped.

#### Acceptance Criteria

1. WHEN a relationship has DTO_Strategy set to ID_ONLY, THE Template_Engine SHALL generate the DTO field as the target entity primary key type (e.g., Long departmentId), and for collection relationships (OneToMany, ManyToMany) SHALL generate a List of primary key values (e.g., List<Long> employeeIds)
2. WHEN a relationship has DTO_Strategy set to SUMMARY, THE Template_Engine SHALL generate a SummaryDto class for the target entity containing the id field and the first String-type field defined on that entity as the display label, and reference that type in the parent DTO
3. WHEN a relationship has DTO_Strategy set to NESTED, THE Template_Engine SHALL embed the target entity DetailDto one level deep, with all nested relationships within that embedded DTO defaulting to ID_ONLY strategy to prevent recursion
4. WHEN a relationship has DTO_Strategy set to IGNORE, THE Template_Engine SHALL omit the relationship field from the DTO entirely
5. THE Template_Engine SHALL generate separate ListDto and DetailDto for each entity, where ListDto applies ID_ONLY strategy to all relationships regardless of configured DTO_Strategy, and DetailDto applies the configured DTO_Strategy for each relationship
6. THE Template_Engine SHALL generate CreateRequest and UpdateRequest DTOs that accept relationship references as foreign key IDs rather than nested objects, using a single ID field for ManyToOne and OneToOne relationships and a List of IDs for OneToMany and ManyToMany relationships
7. THE Template_Engine SHALL generate service implementation code that resolves foreign key IDs to entity references using repository lookups during create and update operations
8. IF a foreign key ID provided in a CreateRequest or UpdateRequest does not correspond to an existing entity, THEN THE Template_Engine SHALL generate service code that throws an exception with an error message indicating which entity type and ID was not found

### Requirement 12: Generator Operation Completeness

**User Story:** As a user, I want every declared OperationType to produce functional code in the Generated_Project, so that the generated application is complete and deployment-ready.

#### Acceptance Criteria

1. WHEN an entity has PAGINATION operation enabled, THE Template_Engine SHALL generate a controller accepting Pageable parameters with a default page size of 20 and a maximum page size of 100, and a repository extending PagingAndSortingRepository
2. WHEN an entity has SEARCH operation enabled, THE Template_Engine SHALL generate Spring Data JPA Specification-based dynamic query building from filter parameters
3. WHEN an entity has SOFT_DELETE operation enabled, THE Template_Engine SHALL generate a deleted boolean field, a deletedAt timestamp, and a repository query restriction excluding soft-deleted records from standard queries
4. WHEN an entity has BULK_INSERT operation enabled, THE Template_Engine SHALL generate a POST /bulk endpoint accepting a list of up to 500 CreateRequest DTOs with batch persistence, and if any single record fails validation the entire batch SHALL be rejected
5. WHEN an entity has EXPORT_CSV operation enabled, THE Template_Engine SHALL generate a GET /export/csv endpoint producing a downloadable CSV file with entity data
6. WHEN an entity has EXPORT_EXCEL operation enabled, THE Template_Engine SHALL generate a GET /export/excel endpoint using Apache POI to produce an Excel file
7. WHEN an entity has EXPORT_PDF operation enabled, THE Template_Engine SHALL generate a GET /export/pdf endpoint using OpenPDF to produce a PDF file
8. WHEN an entity has IMPORT_CSV operation enabled, THE Template_Engine SHALL generate a POST /import/csv endpoint accepting multipart file upload and parsing CSV records into entities, skipping malformed rows and returning a response indicating how many rows were imported and how many were skipped with per-row error details
9. WHEN an entity has AUDIT_LOG operation enabled, THE Template_Engine SHALL generate an entity change tracking table and a JPA event listener recording field-level changes
10. WHEN an entity has VERSIONING operation enabled, THE Template_Engine SHALL generate a @Version field for optimistic locking and a version history table
11. WHEN an entity has STATUS_TRANSITION operation enabled, THE Template_Engine SHALL generate a state machine pattern with configurable allowed transitions and a transition validation check that rejects invalid transitions with an error message indicating the current status and the attempted target status
12. WHEN an entity has WEBHOOK_INTEGRATION operation enabled, THE Template_Engine SHALL generate an event publisher and an asynchronous webhook caller triggered on entity changes
13. THE Template_Engine SHALL generate a Maven Wrapper (mvnw, mvnw.cmd, .mvn/wrapper/), README.md, Dockerfile, and .gitignore in every Generated_Project
14. THE Template_Engine SHALL generate a Flyway V1 migration SQL file containing CREATE TABLE statements matching the entity definitions in the Generated_Project
15. THE Template_Engine SHALL generate application.properties with database configuration, server port, and JPA settings (hibernate.dialect, ddl-auto, show-sql) based on the Project_Definition database type
16. THE Template_Engine SHALL produce Generated_Project source code that compiles without errors using Maven when all declared operations are enabled
17. IF an entity declares an OperationType not supported by the Generator, THEN THE Generator SHALL log a warning identifying the unsupported operation and skip code generation for that operation without failing the overall generation
18. WHEN an entity has BULK_UPDATE operation enabled, THE Template_Engine SHALL generate a PUT /bulk endpoint accepting a list of UpdateRequest DTOs; WHEN BULK_DELETE is enabled, THE Template_Engine SHALL generate a DELETE /bulk endpoint accepting a list of IDs; WHEN RESTORE is enabled, THE Template_Engine SHALL generate a PATCH /{id}/restore endpoint; WHEN FILE_UPLOAD is enabled, THE Template_Engine SHALL generate a POST /upload endpoint; WHEN FILE_DOWNLOAD is enabled, THE Template_Engine SHALL generate a GET /download/{id} endpoint; WHEN IMPORT_EXCEL is enabled, THE Template_Engine SHALL generate a POST /import/excel endpoint

### Requirement 13: Asynchronous Generation Pipeline

**User Story:** As a user generating large projects, I want the generation process to execute asynchronously with progress tracking, so that API requests return immediately and I can poll for status.

#### Acceptance Criteria

1. WHEN a generation request is submitted via POST /api/v1/generator/generate, THE Generator SHALL return HTTP 202 Accepted with a Generation_Job ID and QUEUED status within 500 milliseconds
2. THE Generator SHALL process generation requests asynchronously using a configurable thread pool with a default size of 5 threads (configurable range: 1 to 20)
3. WHILE a Generation_Job is processing, THE Generator SHALL update the job progress (0-100) and current stage at each pipeline step (10% validation, 30% structure, 50% entities, 70% security, 90% build, 100% zip)
4. WHEN a client requests GET /api/v1/generator/status/{jobId}, THE Generator SHALL return the current job status (QUEUED, PROCESSING, COMPLETED, FAILED, or EXPIRED), current stage name, progress percentage, and error message if status is FAILED
5. IF a client requests GET /api/v1/generator/status/{jobId} with a jobId that does not exist, THEN THE Generator SHALL return HTTP 404 with an error message indicating the job was not found
6. WHEN a client requests GET /api/v1/generator/download/{jobId} and the job status is COMPLETED, THE Generator SHALL return the ZIP file as an application/octet-stream response with a Content-Disposition attachment header
7. IF a client requests GET /api/v1/generator/download/{jobId} and the job status is QUEUED or PROCESSING, THEN THE Generator SHALL return HTTP 404 with a message indicating the job is not yet complete
8. IF a client requests GET /api/v1/generator/download/{jobId} and the job status is EXPIRED, THEN THE Generator SHALL return HTTP 410 with a message indicating the generated file has been removed
9. IF a generation pipeline step fails, THEN THE Generator SHALL set the job status to FAILED, record the failure stage and error description, and delete any partially generated project files
10. THE Generator SHALL execute a scheduled cleanup job every 1 hour that deletes generated ZIP files and project folders older than a configurable retention period (default 24 hours) and marks corresponding jobs as EXPIRED
11. IF a user submits a generation request and already has 3 or more Generation_Jobs in QUEUED or PROCESSING status, THEN THE Generator SHALL reject the request with HTTP 429 and a message indicating the concurrent job limit has been reached

### Requirement 14: Test Coverage

**User Story:** As a developer, I want comprehensive automated test coverage of at least 80% line coverage, so that regressions are caught early and system behavior is verified.

#### Acceptance Criteria

1. THE Generator SHALL include JaCoCo configured with a minimum 80% line coverage enforcement threshold that fails the Maven build when not met, excluding configuration classes, DTO classes, and enum classes from coverage measurement
2. THE Generator SHALL include unit tests for all FreeMarker templates verifying correct code generation for at least one entity configuration per supported field type (String, Integer, Long, Double, Boolean, LocalDate, LocalDateTime, BigDecimal), per relationship type (OneToOne, OneToMany, ManyToOne, ManyToMany), and per operation type defined in the OperationType enum
3. THE Generator SHALL include unit tests for all service layer classes with mocked repository dependencies testing business logic, validation, and error handling including not-found scenarios and duplicate-name scenarios
4. THE Generator SHALL include integration tests using @WebMvcTest for all controller endpoints verifying request validation, response structure, status codes, and error scenarios including 400 for invalid input, 404 for missing resources, and 409 for conflicts
5. THE Generator SHALL include integration tests using @DataJpaTest for repository operations verifying custom queries, cascading deletes, orphan removal, and unique constraint enforcement
6. THE Generator SHALL include end-to-end tests that generate a project with at least 3 entities and at least 2 relationship types between them and verify the output compiles successfully using Maven within 120 seconds
7. THE Generator SHALL include security tests verifying authentication enforcement, role-based access control, token validation, and API key validation for at least one protected endpoint and one public endpoint
8. THE Generator SHALL use H2 in-memory database with a test profile named "test" activated via @ActiveProfiles("test") for all automated tests
9. WHEN the full test suite is executed, THE Generator SHALL complete all tests within 300 seconds on a standard build environment

### Requirement 15: Documentation

**User Story:** As an API consumer or contributor, I want complete and accurate documentation, so that I can integrate with the API, contribute to development, or deploy the application without guesswork.

#### Acceptance Criteria

1. THE Generator SHALL include a README.md containing all of the following sections: architecture overview describing the layered package structure, setup instructions with prerequisites and build/run commands, a summary table of all API endpoints with HTTP method and path, the technology stack with version numbers, and database setup steps for each supported database driver
2. THE Generator SHALL include OpenAPI/Swagger annotations on every public controller endpoint method such that accessing /swagger-ui/ in a running instance renders an interactive API documentation page listing all annotated endpoints with request/response schemas
3. THE Generator SHALL include a Postman collection JSON file containing at least one request example per API endpoint, where each request includes a valid URL, HTTP method, headers, and request body matching the endpoint's expected input schema
4. THE Generator SHALL include documentation for all supported OperationTypes (CREATE, READ, UPDATE, DELETE, SEARCH, PAGINATION, BULK_INSERT, BULK_UPDATE, BULK_DELETE, SOFT_DELETE, RESTORE, STATUS_TRANSITION, IMPORT_CSV, EXPORT_CSV, IMPORT_EXCEL, EXPORT_EXCEL, EXPORT_PDF, FILE_UPLOAD, FILE_DOWNLOAD, AUDIT_LOG, VERSIONING, WEBHOOK_INTEGRATION) explaining what code artifacts each operation generates with at least one example input/output snippet per type
5. THE Generator SHALL include documentation for relationship configuration explaining each of the four DTO_Strategy values (ID_ONLY, SUMMARY, NESTED, IGNORE) with a JSON input example and the corresponding generated Java code output for each strategy
6. THE Generator SHALL include a CONTRIBUTING.md containing all of the following sections: code style guidelines referencing the project's formatting rules, pull request process with review requirements, branch naming conventions with pattern examples, and testing requirements specifying minimum coverage or mandatory test types
7. THE Generator SHALL include pom.xml metadata where the license element contains a non-empty name and URL, the developer element contains a non-empty name and email, and the scm element contains a non-empty connection URL pointing to the project's source repository

### Requirement 16: Containerization

**User Story:** As a DevOps engineer, I want the Generator fully containerized with Docker Compose, so that the entire stack starts with a single command in any environment.

#### Acceptance Criteria

1. THE Generator SHALL include a multi-stage Dockerfile producing a runtime image under 250MB using a JRE 17 base image, with a build stage using Maven to compile the application and a runtime stage containing only the compiled artifact and JRE
2. THE Generator SHALL include a docker-compose.yml defining services for the application, PostgreSQL database, and optional pgAdmin, with database connection credentials configurable via environment variables
3. WHEN the Docker container starts, THE Generator SHALL expose an /actuator/health endpoint and the Docker HEALTHCHECK instruction SHALL poll that endpoint with an interval of 30 seconds, a timeout of 10 seconds, a start period of 60 seconds, and 3 retries before marking the container as unhealthy
4. WHEN the container receives SIGTERM, THE Generator SHALL stop accepting new requests and allow up to 30 seconds for in-flight requests to complete before forcing shutdown
5. THE Generator SHALL mount the generated-projects directory as a named Docker volume at a configured container path for persistence across container restarts
6. THE Generator SHALL include Maven in the runtime container so that the Build_Verifier can compile Generated_Projects within the container, with a separate Dockerfile or extended image target if the combined image exceeds the 250MB limit defined in criterion 1
7. THE Generator SHALL support Docker Compose profiles named "dev" and "prod" where the dev profile sets container memory limits to 512MB and enables debug-level logging, and the prod profile sets container memory limits to 1024MB and enables info-level logging
8. WHEN `docker-compose --profile dev up` or `docker-compose --profile prod up` is executed, THE Generator SHALL start all required services without requiring manual setup steps beyond having Docker and Docker Compose installed

### Requirement 17: Frontend Generation

**User Story:** As a user, I want the Generator to optionally produce a React frontend alongside the backend, so that I receive a complete full-stack application from a single project definition.

#### Acceptance Criteria

1. WHEN a Project_Definition has frontendEnabled set to true, THE Generator SHALL produce a React application with CRUD pages for each entity consisting of: a list page with paginated table (default 10 rows per page), a create form, an edit form, and a detail view
2. WHEN a Generated_Project has entities with MANY_TO_ONE relationships, THE Frontend_Generator SHALL generate dropdown select components populated via API calls to the related entity endpoint
3. WHEN a Generated_Project has entities with MANY_TO_MANY relationships, THE Frontend_Generator SHALL generate multi-select components for relationship management
4. THE Frontend_Generator SHALL generate a typed API client module (using Axios) with methods matching all Generated_Project backend endpoints and a configurable base URL defaulting to the backend server port defined in the Project_Definition
5. THE Frontend_Generator SHALL generate an admin dashboard layout with sidebar navigation listing all entities and an overview page displaying the total record count for each entity
6. THE Frontend_Generator SHALL generate a package.json listing React, Axios, React Router, and all dependencies required to build and run the frontend application
7. WHEN generation completes for a Project_Definition with frontendEnabled set to true, THE Generator SHALL include the frontend source directory in the ZIP archive delivered to the user alongside the backend source
8. IF a Project_Definition has frontendEnabled set to true but has no entities defined, THEN THE Generator SHALL return a validation error indicating that at least one entity is required for frontend generation
9. WHEN generating form components, THE Frontend_Generator SHALL map entity field data types to input types: String to text input, numeric types to number input, Boolean to checkbox, Date and DateTime to date picker, and enumerated types to select dropdown
