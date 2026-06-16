# Implementation Plan: Production Implementation

## Overview

This plan transforms the Dynamic Backend Generator from a prototype into a production-ready Spring Boot Low-Code Engine across 11 phases. Tasks are ordered so that foundational infrastructure (cleanup, migrations, config, exceptions) is built first, followed by security, API standards, relationship handling, operation completeness, async pipeline, testing, documentation, containerization, and frontend generation. Each task builds incrementally on prior work.

## Tasks

- [x] 1. Codebase Cleanup and Foundation
  - [x] 1.1 Remove dead code (TestController, TodoController, TodoService, TodoServiceImpl, TodoItem, TodoItemDTO, TodoItemRepository, ApiResponseDTO) and verify the project compiles successfully
    - Delete all todo-related classes and unused DTOs
    - Remove any imports or references to deleted classes
    - Run `mvn compile` to verify
    - _Requirements: 1.1_

  - [x] 1.2 Replace all System.out.println and e.printStackTrace calls with SLF4J placeholder logging
    - Add SLF4J Logger to each class using `LoggerFactory.getLogger()`
    - Replace print statements with appropriate log levels (info, error, debug)
    - Use `{}` placeholders instead of string concatenation
    - _Requirements: 1.2_

  - [x] 1.3 Convert all field-level @Autowired annotations to constructor injection
    - Refactor all controller, service, and configuration classes
    - Use `@RequiredArgsConstructor` from Lombok or explicit constructors
    - Remove `@Autowired` field annotations
    - _Requirements: 1.3_

  - [x] 1.4 Externalize Maven executable path in Build_Verifier and remove hardcoded paths
    - Add `app.maven.executable` property to application configuration
    - Update BuildVerifier to inject the property instead of hardcoded path
    - _Requirements: 1.4_

  - [x] 1.5 Remove commented-out code blocks from SecurityConfig, ProjectGenerationServiceImpl, and pom.xml
    - Audit and remove consecutive commented lines (2+ lines)
    - Remove unused spring-security-test dependency if spring-boot-starter-security is absent
    - _Requirements: 1.5, 1.6_

- [x] 2. Flyway Database Migration Setup
  - [x] 2.1 Add Flyway dependencies and configure migration infrastructure
    - Add `flyway-core` and `flyway-database-postgresql` to pom.xml
    - Create `src/main/resources/db/migration/` directory
    - Configure Flyway to run before Hibernate validation in production profile
    - _Requirements: 2.1, 2.2_

  - [x] 2.2 Create V1 migration script for core tables (project_definitions, entity_definitions, field_definitions, operation_configs)
    - Use BIGSERIAL primary keys
    - Define foreign keys with ON DELETE CASCADE
    - Add indexes on all foreign key columns
    - _Requirements: 2.4_

  - [x] 2.3 Configure Hibernate ddl-auto to `validate` in prod/docker profiles and `create-drop` in dev profile with Flyway disabled in dev
    - Set `spring.jpa.hibernate.ddl-auto=validate` for prod
    - Set `spring.flyway.enabled=false` for dev profile
    - Ensure migration failure prevents application startup
    - _Requirements: 2.3, 2.5, 2.6_

- [x] 3. Multi-Environment Configuration
  - [x] 3.1 Create application-dev.yml, application-prod.yml, and application-docker.yml profile configurations
    - Dev: H2 in-memory, port 8083, create-drop, Flyway disabled
    - Prod: PostgreSQL, port 8080, validate, Flyway enabled
    - Docker: PostgreSQL with service name "db" as hostname, port 8080, validate, Flyway enabled
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 3.2 Create AppProperties class with @ConfigurationProperties and externalize environment variables
    - Define `app.generated-projects-directory` (default: `./generated-projects`)
    - Define `app.maven-executable` (default: `mvn`)
    - Externalize DB URL, username, password with `${ENV_VAR:default}` pattern
    - Default profile to dev when none specified
    - _Requirements: 3.5, 3.6, 3.8_

  - [x] 3.3 Create .env.example file and add startup validation for required variables in prod/docker
    - List every externalized environment variable with description and default
    - Add startup validation that fails with descriptive error for missing required variables
    - _Requirements: 3.7, 3.9_

- [ ] 4. Checkpoint - Ensure project compiles and starts in dev profile
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Custom Exception Hierarchy
  - [ ] 5.1 Create BaseApplicationException and all custom exception classes
    - Create abstract `BaseApplicationException` with resourceType, resourceIdentifier, operationStage fields
    - Create `ProjectNotFoundException`, `DuplicateNameException`, `GenerationFailedException`, `ValidationException`, `BuildVerificationException`, `UnsupportedSecurityTypeException`
    - _Requirements: 4.1, 4.6_

  - [ ] 5.2 Implement GlobalExceptionHandler with consistent ErrorResponse format
    - Create `ErrorResponse` record with timestamp, status, errorType, message, path, requestId, fieldErrors
    - Handle MethodArgumentNotValidException → HTTP 400 with field errors
    - Handle DataIntegrityViolationException → HTTP 409
    - Handle ProjectNotFoundException → HTTP 404
    - Handle DuplicateNameException → HTTP 409
    - Handle ValidationException → HTTP 400
    - Handle GenerationFailedException → HTTP 500 with stage
    - Handle BuildVerificationException → HTTP 500 with stage
    - Handle UnsupportedSecurityTypeException → HTTP 400 with type value
    - Generic fallback → HTTP 500 without internal details
    - Include X-Request-Id in all error responses
    - _Requirements: 4.2, 4.3, 4.4, 4.5, 4.7, 4.8, 4.9_

  - [ ]* 5.3 Write unit tests for GlobalExceptionHandler verifying each exception type returns correct status and response structure
    - Test each custom exception handler method
    - Verify ErrorResponse fields are populated correctly
    - _Requirements: 4.2, 4.7_

- [ ] 6. Audit Logging Infrastructure
  - [ ] 6.1 Implement RequestIdFilter for X-Request-Id generation and MDC propagation
    - Generate UUID on each request
    - Set MDC context with requestId
    - Add X-Request-Id to response headers
    - _Requirements: 5.2_

  - [ ] 6.2 Create AuditInterceptor and Audit_Log entity/repository
    - Create `AuditLog` JPA entity with all required fields (requestId, method, endpoint, requestBody, responseStatus, durationMs, clientIp, userAgent, generationStage)
    - Implement `HandlerInterceptor` preHandle/afterCompletion
    - Truncate request body at 10,000 chars and endpoint at 500 chars
    - _Requirements: 5.1, 5.4_

  - [ ] 6.3 Implement DatabaseAppender for ERROR/WARN log persistence
    - Create `ApplicationLog` JPA entity with level, loggerName, message, stackTrace, requestId, userId, context (JSONB)
    - Implement custom Logback `AppenderBase<ILoggingEvent>` that writes to application_logs table
    - Handle appender failures gracefully (log to console, don't interrupt request)
    - _Requirements: 5.3, 5.7_

  - [ ] 6.4 Configure Logback with console, rolling-file, and database appenders
    - Console appender for all profiles
    - Rolling-file: max 10 MB per file, 30 days retention
    - JSON format for prod/docker profiles
    - Database appender for ERROR/WARN
    - _Requirements: 5.6_

  - [ ] 6.5 Implement scheduled cleanup job for audit and application logs older than 30 days
    - Create `@Scheduled` method running daily
    - Delete AuditLog and ApplicationLog records older than configurable retention
    - _Requirements: 5.5_

  - [ ] 6.6 Create Flyway V4 migration for audit_logs and application_logs tables
    - Define audit_logs and application_logs table schemas
    - Add indexes on request_id and created_at columns
    - _Requirements: 5.1, 5.3_

- [ ] 7. Host API Authentication and Authorization
  - [ ] 7.1 Create AppUser entity, repository, and Flyway V3 migration
    - Define AppUser with id, email (unique), passwordHash, fullName, role, apiKey (unique), createdAt, updatedAt
    - Create generation_jobs table in same migration
    - _Requirements: 6.1_

  - [ ] 7.2 Implement JwtService (access token 15 min, refresh token 7 days) and JwtAuthenticationFilter
    - Generate and validate JWT tokens with claims
    - Implement `OncePerRequestFilter` extracting Bearer token from Authorization header
    - Reject expired/malformed/invalid tokens with HTTP 401
    - _Requirements: 6.2, 6.3, 6.4_

  - [ ] 7.3 Implement ApiKeyAuthenticationFilter for X-API-Key header authentication
    - Look up user by API key
    - Populate security context with user details and roles
    - _Requirements: 6.5_

  - [ ] 7.4 Implement AuthController with register and login endpoints
    - POST /api/v1/auth/register: validate password rules (8-128 chars, uppercase, lowercase, digit, special char), BCrypt hash, default USER role, return HTTP 201
    - POST /api/v1/auth/login: validate credentials, return access + refresh tokens, reject invalid credentials with HTTP 401
    - _Requirements: 6.1, 6.2, 6.11, 6.12_

  - [ ] 7.5 Configure SecurityFilterChain with role-based access control and public endpoints
    - Filter order: RateLimit → ApiKey → JWT → Authorization
    - Public endpoints: `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**`, `/actuator/health`
    - USER role: restrict to own projects only
    - ADMIN role: access all resources
    - Return HTTP 403 for unauthorized project access
    - _Requirements: 6.6, 6.7, 6.8, 6.9_

  - [ ] 7.6 Implement RateLimitFilter for generation endpoint (10 requests/minute per user)
    - Track request counts per user with sliding window
    - Return HTTP 429 when limit exceeded
    - _Requirements: 6.10_

  - [ ]* 7.7 Write security integration tests for auth flows, token validation, and access control
    - Test registration, login, token refresh
    - Test protected endpoint rejection without token
    - Test role-based access enforcement
    - _Requirements: 14.7_

- [ ] 8. Checkpoint - Verify authentication and authorization work end-to-end
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. API Versioning and Response Standards
  - [ ] 9.1 Create ApiEnvelope, PaginationMeta, and ErrorInfo response wrapper classes
    - Define `ApiEnvelope<T>` with success, data, pagination, timestamp (ISO-8601 UTC), requestId, error fields
    - Define `PaginationMeta` with page, size, totalElements, totalPages
    - _Requirements: 8.2_

  - [ ] 9.2 Refactor all controllers to use /api/v1/ prefix and return ApiEnvelope responses
    - Move all endpoints under `/api/v1/` path
    - Wrap all successful responses in ApiEnvelope
    - Include pagination metadata for collection endpoints
    - _Requirements: 8.1, 8.2_

  - [ ] 9.3 Implement pagination support with page/size/sort parameters and validation
    - Accept page (zero-based, default 0), size (default 20, min 1, max 100), sort (fieldName,asc|desc, default: createdAt,desc)
    - Reject invalid page/size with HTTP 400
    - _Requirements: 8.3, 8.9_

  - [ ] 9.4 Configure CORS from application properties with deny-all default
    - Read allowed origins, methods, headers from configuration
    - Default to deny-all when no properties configured
    - _Requirements: 8.4_

  - [ ] 9.5 Add entity/field limit enforcement (50 entities per project, 100 fields per entity)
    - Validate on create/update operations
    - Return HTTP 400 with descriptive error when limits exceeded
    - _Requirements: 8.7_

  - [ ] 9.6 Implement project list filtering by name (case-insensitive partial) and databaseType (exact match)
    - Add query parameters for filtering
    - Support sorting by createdAt
    - _Requirements: 8.8_

  - [ ] 9.7 Add OpenAPI annotations to all controller endpoints
    - Annotate with @Operation, @ApiResponse, @Schema
    - Ensure Swagger UI renders all endpoints with request/response schemas
    - _Requirements: 8.6, 15.2_

  - [ ]* 9.8 Write integration tests for pagination, filtering, envelope structure, and error responses
    - Verify envelope structure for success and error cases
    - Test pagination edge cases
    - _Requirements: 8.2, 8.3, 8.9, 8.10_

- [ ] 10. Relationship Model and Data Layer
  - [ ] 10.1 Create RelationshipDefinition entity, repository, and Flyway V2 migration
    - Define entity with all fields: sourceEntityId, fieldName, relationshipType, targetEntityName, mappedBy, fetchType, cascadeTypes, orphanRemoval, joinColumnName, joinTableName, inverseJoinColumnName, nullable, dtoStrategy
    - Create migration script that also migrates legacy field_definitions relationship data
    - _Requirements: 9.1, 9.5_

  - [ ] 10.2 Implement relationship CRUD operations in EntityService with validation
    - Accept up to 50 relationships per entity
    - Validate target entity exists within same project
    - Validate cascade types against allowed set (ALL, PERSIST, MERGE, REMOVE, REFRESH, DETACH)
    - Deduplicate cascade types before persisting
    - Deprecate legacy fields in API schema
    - _Requirements: 9.2, 9.3, 9.4, 9.5, 9.6_

  - [ ] 10.3 Add Flyway V5 migration for owner_id on project_definitions and frontend_enabled column
    - Add owner_id FK to app_users
    - Add frontend_enabled boolean column
    - _Requirements: (design data model)_

  - [ ] 10.4 Add Flyway V6 migration for indexes on foreign keys and frequently queried columns
    - Add indexes as specified in the migration strategy
    - _Requirements: (design data model)_

  - [ ]* 10.5 Write unit tests for relationship validation logic (target entity existence, cascade type validation, deduplication)
    - Test invalid target entity rejection
    - Test invalid cascade type rejection
    - Test deduplication behavior
    - _Requirements: 9.4, 9.6_

- [ ] 11. Relationship Code Generation (Templates)
  - [ ] 11.1 Implement RelationshipCodeGenerator for JPA annotation generation
    - Generate @ManyToOne with FetchType and @JoinColumn (default: fieldName + "_id")
    - Generate @OneToMany with mappedBy, cascade, orphanRemoval, fetch, List<TargetEntity> type
    - Generate @ManyToMany with @JoinTable, @JoinColumn, inverseJoinColumns
    - Generate single cascade value vs array format based on count
    - _Requirements: 10.1, 10.2, 10.3, 10.5_

  - [ ] 11.2 Implement bidirectional relationship handling with JSON serialization annotations
    - Add @JsonManagedReference on non-owning side (mappedBy side)
    - Add @JsonBackReference on owning side (@JoinColumn side)
    - Omit both annotations for unidirectional relationships
    - _Requirements: 10.4, 10.7_

  - [ ] 11.3 Implement dynamic import generation based on entity annotations and types
    - Include only required jakarta.persistence imports
    - Include com.fasterxml.jackson.annotation imports when needed
    - Include java.util.List only for collection relationships
    - _Requirements: 10.6_

  - [ ] 11.4 Create/update FreeMarker entity template to integrate relationship annotations
    - Update entity.ftl template to render relationship fields with all annotations
    - Integrate with existing field generation
    - _Requirements: 10.1, 10.2, 10.3_

  - [ ]* 11.5 Write unit tests for relationship code generation covering all relationship types and edge cases
    - Test each relationship type annotation output
    - Test bidirectional vs unidirectional handling
    - Test cascade array formatting
    - _Requirements: 10.1, 10.4, 10.5_

- [ ] 12. DTO Strategy Generation
  - [ ] 12.1 Implement DtoStrategyGenerator interface and four strategy implementations (IdOnly, Summary, Nested, Ignore)
    - ID_ONLY: generate primary key type field (Long departmentId), List<Long> for collections
    - SUMMARY: generate SummaryDto with id + first String field as label
    - NESTED: embed DetailDto one level deep, nested relationships default to ID_ONLY
    - IGNORE: omit field from DTO entirely
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [ ] 12.2 Generate ListDto and DetailDto templates for each entity
    - ListDto: always uses ID_ONLY for all relationships
    - DetailDto: uses configured DTO_Strategy per relationship
    - _Requirements: 11.5_

  - [ ] 12.3 Generate CreateRequest and UpdateRequest DTOs with foreign key ID references
    - Single ID field for ManyToOne/OneToOne
    - List of IDs for OneToMany/ManyToMany
    - _Requirements: 11.6_

  - [ ] 12.4 Generate service implementation code that resolves FK IDs to entity references
    - Repository lookups during create/update
    - Throw exception with entity type and ID when not found
    - _Requirements: 11.7, 11.8_

  - [ ]* 12.5 Write unit tests for each DTO strategy output and CreateRequest/UpdateRequest generation
    - Verify ID_ONLY, SUMMARY, NESTED, IGNORE outputs
    - Test ListDto vs DetailDto strategy application
    - _Requirements: 11.1, 11.5, 11.6_

- [ ] 13. Checkpoint - Verify relationship generation produces compilable output
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Generator Operation Completeness
  - [ ] 14.1 Implement PAGINATION and SEARCH operation templates
    - PAGINATION: controller with Pageable (default 20, max 100), PagingAndSortingRepository
    - SEARCH: Spring Data JPA Specification-based dynamic queries
    - _Requirements: 12.1, 12.2_

  - [ ] 14.2 Implement SOFT_DELETE and RESTORE operation templates
    - SOFT_DELETE: deleted boolean field, deletedAt timestamp, query restriction excluding soft-deleted
    - RESTORE: PATCH /{id}/restore endpoint
    - _Requirements: 12.3, 12.18_

  - [ ] 14.3 Implement BULK_INSERT, BULK_UPDATE, and BULK_DELETE operation templates
    - BULK_INSERT: POST /bulk, up to 500 items, all-or-nothing validation
    - BULK_UPDATE: PUT /bulk with list of UpdateRequest DTOs
    - BULK_DELETE: DELETE /bulk with list of IDs
    - _Requirements: 12.4, 12.18_

  - [ ] 14.4 Implement EXPORT_CSV, EXPORT_EXCEL, and EXPORT_PDF operation templates
    - CSV: GET /export/csv with downloadable file
    - Excel: GET /export/excel using Apache POI
    - PDF: GET /export/pdf using OpenPDF
    - _Requirements: 12.5, 12.6, 12.7_

  - [ ] 14.5 Implement IMPORT_CSV and IMPORT_EXCEL operation templates
    - CSV: POST /import/csv multipart upload, skip malformed rows, return import summary with per-row errors
    - Excel: POST /import/excel multipart upload
    - _Requirements: 12.8, 12.18_

  - [ ] 14.6 Implement AUDIT_LOG and VERSIONING operation templates
    - AUDIT_LOG: change tracking table, JPA event listener for field-level changes
    - VERSIONING: @Version field for optimistic locking, version history table
    - _Requirements: 12.9, 12.10_

  - [ ] 14.7 Implement STATUS_TRANSITION and WEBHOOK_INTEGRATION operation templates
    - STATUS_TRANSITION: state machine with configurable transitions, validation rejecting invalid transitions
    - WEBHOOK_INTEGRATION: event publisher, async webhook caller on entity changes
    - _Requirements: 12.11, 12.12_

  - [ ] 14.8 Implement FILE_UPLOAD and FILE_DOWNLOAD operation templates
    - FILE_UPLOAD: POST /upload endpoint
    - FILE_DOWNLOAD: GET /download/{id} endpoint
    - _Requirements: 12.18_

  - [ ] 14.9 Implement generated project boilerplate templates (Maven Wrapper, README, Dockerfile, .gitignore, Flyway V1, application.properties)
    - Generate mvnw, mvnw.cmd, .mvn/wrapper/ files
    - Generate README.md, Dockerfile, .gitignore
    - Generate V1 migration SQL matching entity definitions
    - Generate application.properties based on database type
    - _Requirements: 12.13, 12.14, 12.15_

  - [ ] 14.10 Add handling for unsupported OperationType (log warning and skip without failing)
    - Log warning with operation type name
    - Continue generation pipeline
    - _Requirements: 12.17_

  - [ ]* 14.11 Write unit tests verifying each operation template produces compilable code
    - Test at least one entity config per operation type
    - Verify generated code structure
    - _Requirements: 12.16, 14.2_

- [ ] 15. Security Generators for Output Projects
  - [ ] 15.1 Implement JwtSecurityGenerator for Generated_Projects
    - Generate SecurityConfig, JwtFilter, JwtUtils, AuthController, User entity, UserDetailsService, LoginRequestDTO, SignupRequestDTO
    - Organize templates under /templates/security/jwt/
    - _Requirements: 7.1, 7.5_

  - [ ] 15.2 Implement BasicAuthSecurityGenerator for Generated_Projects
    - Generate SecurityConfig with HTTP Basic and BCryptPasswordEncoder bean
    - Organize templates under /templates/security/basic/
    - _Requirements: 7.2, 7.5_

  - [ ] 15.3 Implement OAuth2SecurityGenerator for Generated_Projects
    - Generate SecurityConfig with OAuth2 login and resource server
    - Generate application.properties fragment with provider placeholders
    - Organize templates under /templates/security/oauth2/
    - _Requirements: 7.3, 7.5_

  - [ ] 15.4 Implement SessionSecurityGenerator for Generated_Projects
    - Generate SecurityConfig with form login, max 1 concurrent session
    - Generate login page HTML template
    - Organize templates under /templates/security/session/
    - _Requirements: 7.4, 7.5_

  - [ ] 15.5 Implement SecurityGeneratorFactory with validation for unsupported security types
    - Route to correct generator based on securityType
    - Throw UnsupportedSecurityTypeException for null/invalid types listing valid options
    - Skip generation entirely when securityEnabled is false
    - _Requirements: 7.6, 7.7_

  - [ ]* 15.6 Write unit tests for each security generator verifying correct file output
    - Test JWT, Basic, OAuth2, Session generator outputs
    - Test factory routing and error cases
    - _Requirements: 7.1, 7.6_

- [ ] 16. Asynchronous Generation Pipeline
  - [ ] 16.1 Create GenerationJob entity, repository, and job state management
    - Define entity with id (UUID), projectId, userId, status, currentStage, progress, errorMessage, outputPath, createdAt, completedAt, expiresAt
    - Implement state transitions: QUEUED → PROCESSING → COMPLETED/FAILED, COMPLETED → EXPIRED
    - _Requirements: 13.1, 13.4_

  - [ ] 16.2 Implement AsyncGenerationExecutor with configurable thread pool
    - Configure thread pool (default 5 threads, range 1-20)
    - Execute generation pipeline: VALIDATION(10%) → STRUCTURE(30%) → ENTITIES(50%) → SECURITY(70%) → BUILD(90%) → ZIP(100%)
    - Update job progress at each stage
    - Handle failures: set FAILED status, record stage and error, delete partial files
    - _Requirements: 13.2, 13.3, 13.9_

  - [ ] 16.3 Implement GeneratorController endpoints (submit, status, download)
    - POST /api/v1/generator/generate: return HTTP 202 with job ID within 500ms
    - GET /api/v1/generator/status/{jobId}: return status, stage, progress, error
    - GET /api/v1/generator/download/{jobId}: return ZIP (COMPLETED), 404 (not found/in progress), 410 (EXPIRED)
    - Reject if user has 3+ concurrent jobs (HTTP 429)
    - _Requirements: 13.1, 13.4, 13.5, 13.6, 13.7, 13.8, 13.11_

  - [ ] 16.4 Implement scheduled cleanup for expired generation jobs and files
    - Run every 1 hour
    - Delete ZIP files and folders older than configurable retention (default 24 hours)
    - Mark corresponding jobs as EXPIRED
    - _Requirements: 13.10_

  - [ ]* 16.5 Write integration tests for async generation pipeline (submit, poll, download flow)
    - Test job submission and immediate response
    - Test status polling through state transitions
    - Test download for completed job
    - _Requirements: 13.1, 13.4, 13.6_

- [ ] 17. Checkpoint - Verify async generation pipeline works end-to-end
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 18. Frontend Generation
  - [ ] 18.1 Implement FrontendGenerator producing React CRUD pages for each entity
    - Generate list page with paginated table (default 10 rows)
    - Generate create form, edit form, and detail view
    - Map field types to input components (String→text, numeric→number, Boolean→checkbox, Date→date picker)
    - _Requirements: 17.1, 17.9_

  - [ ] 18.2 Implement relationship UI components (dropdown selects, multi-selects)
    - ManyToOne: dropdown select populated via API
    - ManyToMany: multi-select component
    - _Requirements: 17.2, 17.3_

  - [ ] 18.3 Generate typed API client module with Axios and configurable base URL
    - Generate methods matching all backend endpoints
    - Configure base URL defaulting to backend server port
    - _Requirements: 17.4_

  - [ ] 18.4 Generate admin dashboard layout with sidebar navigation and entity overview
    - Sidebar listing all entities
    - Overview page with record counts per entity
    - _Requirements: 17.5_

  - [ ] 18.5 Generate package.json and integrate frontend into ZIP output
    - List React, Axios, React Router, and all required dependencies
    - Include frontend source in ZIP alongside backend
    - Validate at least one entity exists when frontendEnabled is true
    - _Requirements: 17.6, 17.7, 17.8_

  - [ ]* 18.6 Write unit tests for frontend generation verifying file output and component mapping
    - Test CRUD page generation
    - Test field type to input type mapping
    - _Requirements: 17.1, 17.9_

- [ ] 19. Test Coverage and Quality
  - [ ] 19.1 Configure JaCoCo with 80% minimum line coverage threshold
    - Exclude configuration, DTO, and enum classes from measurement
    - Fail Maven build when threshold not met
    - Configure test profile with H2 and @ActiveProfiles("test")
    - _Requirements: 14.1, 14.8_

  - [ ] 19.2 Write unit tests for all FreeMarker templates (field types, relationship types, operation types)
    - Test each supported field type (String, Integer, Long, Double, Boolean, LocalDate, LocalDateTime, BigDecimal)
    - Test each relationship type (OneToOne, OneToMany, ManyToOne, ManyToMany)
    - Test each operation type in OperationType enum
    - _Requirements: 14.2_

  - [ ] 19.3 Write unit tests for all service layer classes with mocked repositories
    - Test business logic, validation, error handling
    - Test not-found and duplicate-name scenarios
    - _Requirements: 14.3_

  - [ ] 19.4 Write @WebMvcTest integration tests for all controller endpoints
    - Verify request validation, response structure, status codes
    - Test 400, 404, 409 error scenarios
    - _Requirements: 14.4_

  - [ ] 19.5 Write @DataJpaTest integration tests for repository operations
    - Test custom queries, cascading deletes, orphan removal, unique constraints
    - _Requirements: 14.5_

  - [ ] 19.6 Write end-to-end generation test (3+ entities, 2+ relationship types, verify compilation)
    - Generate project and verify Maven compile succeeds within 120 seconds
    - _Requirements: 14.6_

  - [ ]* 19.7 Ensure full test suite completes within 300 seconds
    - Profile and optimize slow tests
    - _Requirements: 14.9_

- [ ] 20. Documentation
  - [ ] 20.1 Write comprehensive README.md with architecture, setup, API summary, tech stack, and DB setup
    - Include layered package structure description
    - Include prerequisites, build/run commands
    - Include API endpoint summary table
    - Include technology stack with versions
    - Include database setup for each supported driver
    - _Requirements: 15.1_

  - [ ] 20.2 Create Postman collection JSON with request examples for all endpoints
    - One request per endpoint with valid URL, method, headers, body
    - _Requirements: 15.3_

  - [ ] 20.3 Write documentation for all OperationTypes with examples
    - Explain generated artifacts for each operation
    - Include input/output snippet per type
    - _Requirements: 15.4_

  - [ ] 20.4 Write relationship and DTO strategy documentation with JSON examples and generated code samples
    - Document each DTO_Strategy (ID_ONLY, SUMMARY, NESTED, IGNORE)
    - Include JSON input and generated Java output for each
    - _Requirements: 15.5_

  - [ ] 20.5 Create CONTRIBUTING.md and add pom.xml metadata (license, developer, scm)
    - Code style guidelines, PR process, branch naming, testing requirements
    - Add license name/URL, developer name/email, scm connection URL to pom.xml
    - _Requirements: 15.6, 15.7_

- [ ] 21. Containerization
  - [ ] 21.1 Create multi-stage Dockerfile (JRE 17, under 250MB runtime image)
    - Build stage: Maven compile
    - Runtime stage: JRE 17 + compiled artifact only
    - Add HEALTHCHECK polling /actuator/health (interval 30s, timeout 10s, start-period 60s, retries 3)
    - Configure graceful shutdown (30s drain)
    - _Requirements: 16.1, 16.3, 16.4_

  - [ ] 21.2 Create docker-compose.yml with app, PostgreSQL, pgAdmin services and Maven support
    - Configure database credentials via environment variables
    - Mount generated-projects as named volume
    - Include Maven in runtime for Build_Verifier (extended image if needed)
    - Support dev and prod profiles (dev: 512MB, debug; prod: 1024MB, info)
    - _Requirements: 16.2, 16.5, 16.6, 16.7, 16.8_

- [ ] 22. Final Checkpoint - Full system verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key integration points
- The project uses Java 17 with Spring Boot, FreeMarker templates, and Maven build system
- Flyway migrations are numbered V1-V6 as defined in the design document
- Security filter chain order: RateLimit → ApiKey → JWT → Authorization
- Generated projects are compiled via Maven for build verification

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4", "1.5"] },
    { "id": 1, "tasks": ["2.1", "3.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "3.2", "3.3"] },
    { "id": 3, "tasks": ["5.1", "6.1"] },
    { "id": 4, "tasks": ["5.2", "6.2", "6.3"] },
    { "id": 5, "tasks": ["5.3", "6.4", "6.5", "6.6"] },
    { "id": 6, "tasks": ["7.1"] },
    { "id": 7, "tasks": ["7.2", "7.3", "7.4"] },
    { "id": 8, "tasks": ["7.5", "7.6"] },
    { "id": 9, "tasks": ["7.7", "9.1"] },
    { "id": 10, "tasks": ["9.2", "9.3", "9.4"] },
    { "id": 11, "tasks": ["9.5", "9.6", "9.7"] },
    { "id": 12, "tasks": ["9.8", "10.1"] },
    { "id": 13, "tasks": ["10.2", "10.3", "10.4"] },
    { "id": 14, "tasks": ["10.5", "11.1"] },
    { "id": 15, "tasks": ["11.2", "11.3"] },
    { "id": 16, "tasks": ["11.4"] },
    { "id": 17, "tasks": ["11.5", "12.1"] },
    { "id": 18, "tasks": ["12.2", "12.3"] },
    { "id": 19, "tasks": ["12.4"] },
    { "id": 20, "tasks": ["12.5", "14.1", "14.2"] },
    { "id": 21, "tasks": ["14.3", "14.4", "14.5"] },
    { "id": 22, "tasks": ["14.6", "14.7", "14.8"] },
    { "id": 23, "tasks": ["14.9", "14.10"] },
    { "id": 24, "tasks": ["14.11", "15.1", "15.2"] },
    { "id": 25, "tasks": ["15.3", "15.4"] },
    { "id": 26, "tasks": ["15.5"] },
    { "id": 27, "tasks": ["15.6", "16.1"] },
    { "id": 28, "tasks": ["16.2"] },
    { "id": 29, "tasks": ["16.3"] },
    { "id": 30, "tasks": ["16.4", "16.5"] },
    { "id": 31, "tasks": ["18.1"] },
    { "id": 32, "tasks": ["18.2", "18.3"] },
    { "id": 33, "tasks": ["18.4", "18.5"] },
    { "id": 34, "tasks": ["18.6", "19.1"] },
    { "id": 35, "tasks": ["19.2", "19.3"] },
    { "id": 36, "tasks": ["19.4", "19.5"] },
    { "id": 37, "tasks": ["19.6", "19.7"] },
    { "id": 38, "tasks": ["20.1", "20.2"] },
    { "id": 39, "tasks": ["20.3", "20.4", "20.5"] },
    { "id": 40, "tasks": ["21.1"] },
    { "id": 41, "tasks": ["21.2"] }
  ]
}
```
