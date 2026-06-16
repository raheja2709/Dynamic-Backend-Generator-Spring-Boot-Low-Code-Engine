# Dynamic Backend Generator - Full Production Implementation Plan

> **Project:** Dynamic Backend Generator (Spring Boot Low-Code Engine)  
> **Author:** Jatin Raheja  
> **Plan Version:** 2.0 (Unified & Final)  
> **Last Updated:** June 2026  
> **Database:** PostgreSQL  
> **Target:** Production-ready, sellable product

---

## Table of Contents

- [Phase 1: Clean Foundation](#phase-1-clean-foundation)
- [Phase 2: Multi-Environment Configuration](#phase-2-multi-environment-configuration)
- [Phase 3: Error Handling + DB Logging](#phase-3-error-handling--db-logging)
- [Phase 4: Security + Authentication](#phase-4-security--authentication)
- [Phase 5: API Improvements](#phase-5-api-improvements)
- [Phase 6A: Relationship Model Refactor](#phase-6a-relationship-model-refactor)
- [Phase 6B: Relationship Template Generation](#phase-6b-relationship-template-generation)
- [Phase 6C: DTO Relationship Handling](#phase-6c-dto-relationship-handling)
- [Phase 6D: Generator Completeness](#phase-6d-generator-completeness)
- [Phase 7: Async Generation + File Cleanup](#phase-7-async-generation--file-cleanup)
- [Phase 8: Test Coverage (80%+)](#phase-8-test-coverage-80)
- [Phase 9: Documentation](#phase-9-documentation)
- [Phase 10: Dockerization](#phase-10-dockerization)
- [Phase 11: Frontend Generation](#phase-11-frontend-generation)
- [Database Schema](#database-schema)
- [Flyway Migrations](#flyway-migrations)
- [Estimated Timeline](#estimated-timeline)

---

## Phase 1: Clean Foundation

**Goal:** Remove all noise, fix hardcoded values, set up proper database with Flyway.

| # | Task | Details | Status |
|---|------|---------|--------|
| 1.1 | Remove dead code | Delete `TestController.java`, `TodoController.java`, `TodoService.java`, `TodoServiceImpl.java`, `TodoItem.java`, `TodoItemDTO.java`, `ApiResponseDTO.java` | ⬜ |
| 1.2 | Remove file "4" | Delete the accidental binary file from root | ⬜ |
| 1.3 | Remove all commented-out code | Clean `SecurityConfig.java`, `ProjectGenerationServiceImpl.java`, `pom.xml` (security dep comment) | ⬜ |
| 1.4 | Fix hardcoded Maven path | Refactor `BuildVerifier.java` to use PATH-based resolution (`mvn` command) or bundled Maven Wrapper | ⬜ |
| 1.5 | Remove `System.out.println` and `e.printStackTrace()` | Replace with SLF4J placeholder logging | ⬜ |
| 1.6 | Fix `@Autowired` field injection | Convert to constructor injection in `ProjectGenerationServiceImpl` | ⬜ |
| 1.7 | Remove `spring-security-test` from pom.xml | It's included but security starter is commented out — inconsistent | ⬜ |
| 1.8 | Create PostgreSQL database | `CREATE DATABASE user_driven_operation_mng_sys;` | ⬜ |
| 1.9 | Add Flyway dependency to pom.xml | `org.flywaydb:flyway-core` + `flyway-database-postgresql` | ⬜ |
| 1.10 | Create V1 migration | Tables: `project_definitions`, `entity_definitions`, `field_definitions`, `operation_configs` | ⬜ |
| 1.11 | Switch `ddl-auto` to `validate` | Flyway manages schema, Hibernate only validates | ⬜ |
| 1.12 | Remove unused imports and fix double semicolons | Clean up `ApiPathConstants.java` and others | ⬜ |
| 1.13 | Verify app starts successfully | Run and confirm no errors | ⬜ |

**Deliverable:** Clean codebase, Flyway-managed database, no dead code.

---

## Phase 2: Multi-Environment Configuration

**Goal:** Make the app deployable anywhere without code changes.

| # | Task | Details | Status |
|---|------|---------|--------|
| 2.1 | Create `application.properties` | Shared config only (app name, swagger paths, file upload limits) | ⬜ |
| 2.2 | Create `application-dev.properties` | H2 database, `ddl-auto=create-drop`, show SQL, port 8083 | ⬜ |
| 2.3 | Create `application-prod.properties` | PostgreSQL, `ddl-auto=validate`, Flyway enabled, no show SQL, port 8080 | ⬜ |
| 2.4 | Create `application-docker.properties` | Container-friendly URLs (`jdbc:postgresql://db:5432/...`) | ⬜ |
| 2.5 | Externalize all secrets | Use `${DB_URL:jdbc:h2:mem:testdb}`, `${DB_USERNAME:sa}`, `${DB_PASSWORD:}` pattern | ⬜ |
| 2.6 | Externalize generator config | `${APP_GENERATED_PROJECTS_DIR:./generated-projects}`, `${APP_MAVEN_PATH:mvn}` | ⬜ |
| 2.7 | Create `.env.example` | Document all required environment variables with descriptions | ⬜ |
| 2.8 | Add `@ConfigurationProperties` class | Type-safe config: `AppProperties.java` with nested groups | ⬜ |
| 2.9 | Update `BuildVerifier` to read Maven path from config | Inject from `AppProperties` instead of hardcoding | ⬜ |

**Deliverable:** Profile-based config, zero secrets in source code.

---

## Phase 3: Error Handling + DB Logging

**Goal:** Never lose track of what happened or why something failed. Track every request and error in the database.

| # | Task | Details | Status |
|---|------|---------|--------|
| 3.1 | Create custom exception hierarchy | `BaseException`, `ProjectNotFoundException`, `DuplicateNameException`, `GenerationFailedException`, `UnsupportedSecurityTypeException`, `ValidationException`, `BuildVerificationException` | ⬜ |
| 3.2 | Create `ErrorResponse` DTO | Consistent structure: `timestamp`, `status`, `error`, `message`, `path`, `requestId`, `fieldErrors[]` | ⬜ |
| 3.3 | Refactor `GlobalExceptionHandler` | Proper HTTP status per exception type, handle `MethodArgumentNotValidException`, `ConstraintViolationException`, `DataIntegrityViolationException` | ⬜ |
| 3.4 | Create V3 Flyway migration | Tables: `audit_logs`, `application_logs` | ⬜ |
| 3.5 | Create `AuditLog` JPA entity + repository | Fields: `requestId`, `userId`, `method`, `endpoint`, `requestBody`, `responseStatus`, `errorMessage`, `stackTrace`, `stage`, `durationMs`, `ipAddress`, `userAgent` | ⬜ |
| 3.6 | Create `ApplicationLog` JPA entity + repository | Fields: `level`, `logger`, `message`, `stackTrace`, `requestId`, `userId`, `context` (JSONB) | ⬜ |
| 3.7 | Build `AuditInterceptor` (HandlerInterceptor) | Intercept all requests, measure duration, log to `audit_logs` | ⬜ |
| 3.8 | Build custom Logback `DatabaseAppender` | Write ERROR/WARN logs to `application_logs` table | ⬜ |
| 3.9 | Add `X-Request-Id` header + MDC propagation | Generate UUID per request, include in all logs and responses | ⬜ |
| 3.10 | Add `stage` tracking in generation pipeline | Stages: VALIDATION, STRUCTURE, POM_GENERATION, APP_GENERATION, ENTITY_GENERATION, SECURITY_GENERATION, BUILD_VERIFICATION, ZIP | ⬜ |
| 3.11 | Replace all `throw new RuntimeException` | Use specific custom exceptions with stage context | ⬜ |
| 3.12 | Configure Logback properly | `logback-spring.xml` with console + file + DB appenders, JSON format for prod | ⬜ |
| 3.13 | Add log retention/cleanup | Scheduled job to delete logs older than 30 days | ⬜ |

**Deliverable:** Every API call tracked, every error traceable with stage info, structured logs in DB.

---

## Phase 4: Security + Authentication

**Goal:** Lock down the host API and implement all 4 security generators for output projects.

### 4A: Host API Security

| # | Task | Details | Status |
|---|------|---------|--------|
| 4A.1 | Create V2 Flyway migration | Tables: `app_users`, `refresh_tokens` | ⬜ |
| 4A.2 | Create `AppUser` entity | Fields: `email`, `passwordHash`, `fullName`, `role` (USER/ADMIN), `enabled`, `locked`, `apiKey`, `lastLoginAt` | ⬜ |
| 4A.3 | Create `RefreshToken` entity | Fields: `token`, `userId`, `expiresAt`, `revoked` | ⬜ |
| 4A.4 | Create `UserRepository`, `RefreshTokenRepository` | Standard JPA repos | ⬜ |
| 4A.5 | Create `AuthService` | Register, login, refresh token, logout, validate API key | ⬜ |
| 4A.6 | Enable `spring-boot-starter-security` | Uncomment in pom.xml, add proper dependency | ⬜ |
| 4A.7 | Implement `JwtTokenProvider` | Generate/validate JWT tokens, extract claims | ⬜ |
| 4A.8 | Implement `JwtAuthenticationFilter` | OncePerRequestFilter, extract token from Authorization header | ⬜ |
| 4A.9 | Implement `ApiKeyAuthenticationFilter` | For programmatic/CI access via `X-API-Key` header | ⬜ |
| 4A.10 | Configure `SecurityFilterChain` | Public: `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**`, `/actuator/health`. Protected: everything else | ⬜ |
| 4A.11 | Implement role-based access | ADMIN: all operations. USER: own projects only | ⬜ |
| 4A.12 | Add `user_id` FK to `project_definitions` | V2 migration includes this | ⬜ |
| 4A.13 | Create `AuthController` | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` | ⬜ |
| 4A.14 | Add rate limiting | Bucket4j or Resilience4j on `/api/v1/generator/generate` (e.g., 10 requests/minute per user) | ⬜ |
| 4A.15 | Password policy enforcement | Min 8 chars, uppercase, lowercase, digit, special char | ⬜ |

### 4B: Fix Security Generators (for output projects)

| # | Task | Details | Status |
|---|------|---------|--------|
| 4B.1 | Refactor `SecurityGeneratorFactory` | Register all generators via `@Component` + type annotation, handle missing type with clear error | ⬜ |
| 4B.2 | Fix `JwtSecurityGenerator` | Verify it generates: SecurityConfig, JwtFilter, JwtUtils, AuthController, User entity, UserDetailsService, DTOs | ⬜ |
| 4B.3 | Implement `BasicAuthSecurityGenerator` | Generate: SecurityConfig with httpBasic(), in-memory or DB user store, password encoder | ⬜ |
| 4B.4 | Implement `OAuth2SecurityGenerator` | Generate: SecurityConfig with oauth2Login/oauth2ResourceServer, application.properties with provider config | ⬜ |
| 4B.5 | Implement `SessionSecurityGenerator` | Generate: SecurityConfig with formLogin/session, login page template, session config | ⬜ |
| 4B.6 | Organize templates | Move loose templates in `/templates/` root into `/templates/security/jwt/`, `/templates/security/basic/`, etc. | ⬜ |
| 4B.7 | Test each security type end-to-end | Generate project with each type, verify it compiles and starts | ⬜ |

**Deliverable:** Host API secured with JWT + API key. All 4 security generators working for output projects.

---

## Phase 5: API Improvements

**Goal:** Make the API production-grade and frontend-friendly.

| # | Task | Details | Status |
|---|------|---------|--------|
| 5.1 | API versioning | Move all endpoints to `/api/v1/...`. Keep old paths as deprecated redirects temporarily. | ⬜ |
| 5.2 | CORS configuration | `WebMvcConfigurer` with configurable `allowed-origins`, `allowed-methods`, `allowed-headers` from properties | ⬜ |
| 5.3 | Pagination on all list endpoints | Accept `Pageable` (page, size, sort). Return `Page<T>` wrapped in response envelope. | ⬜ |
| 5.4 | Better validation error responses | Handle `MethodArgumentNotValidException` → return `{ fieldErrors: [{field, message, rejectedValue}] }` | ⬜ |
| 5.5 | Consistent response envelope | All responses: `{ success: boolean, data: T, errors: [], pagination: {page, size, totalElements, totalPages}, timestamp, requestId }` | ⬜ |
| 5.6 | Request ID in response headers | `X-Request-Id` header in every response | ⬜ |
| 5.7 | Search/filter on project list | `GET /api/v1/projects?name=foo&databaseType=POSTGRESQL&sort=createdAt,desc` | ⬜ |
| 5.8 | Swagger annotations | `@Operation`, `@ApiResponse`, `@Schema`, `@Tag` on all controllers | ⬜ |
| 5.9 | Request size limits | Validate max entities per project (e.g., 50), max fields per entity (e.g., 100) | ⬜ |

**Deliverable:** Versioned, paginated, well-documented, CORS-enabled API.

---

## Phase 6A: Relationship Model Refactor

**Goal:** Extract relationship config from `FieldDefinition` into its own dedicated model, supporting all JPA relationship options.

| # | Task | Details | Status |
|---|------|---------|--------|
| 6A.1 | Create `FetchTypeEnum` | Values: `LAZY`, `EAGER` (in `com.user.driven.operations.enums`) | ⬜ |
| 6A.2 | Create `CascadeTypeEnum` | Values: `ALL`, `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH` | ⬜ |
| 6A.3 | Create `DtoStrategy` enum | Values: `ID_ONLY`, `SUMMARY`, `NESTED`, `IGNORE` | ⬜ |
| 6A.4 | Create `RelationshipDefinition` entity | Fields: see [Database Schema](#relationship_definitions-table) | ⬜ |
| 6A.5 | Create V6 Flyway migration | `relationship_definitions` table | ⬜ |
| 6A.6 | Update `EntityDefinition` | Add `@OneToMany List<RelationshipDefinition> relationships` | ⬜ |
| 6A.7 | Create `RelationshipDefinitionDto` | For API input: `relationshipType`, `targetEntity`, `fieldName`, `mappedBy`, `fetchType`, `cascadeTypes[]`, `orphanRemoval`, `joinTableName`, `joinColumn`, `inverseJoinColumn`, `nullable`, `dtoStrategy` | ⬜ |
| 6A.8 | Update `EntityDefinitionDto` | Add `List<RelationshipDefinitionDto> relationships` field | ⬜ |
| 6A.9 | Update `DtoMapper` | Map `RelationshipDefinitionDto` ↔ `RelationshipDefinition` | ⬜ |
| 6A.10 | Update API: create/update entity | Accept relationships in request body, persist them | ⬜ |
| 6A.11 | Deprecate relationship fields in `FieldDefinition` | Keep `relationshipType`, `relationshipTarget` for backward compat but mark `@Deprecated`. New code uses `RelationshipDefinition`. | ⬜ |
| 6A.12 | Migration support | If existing fields have relationship data, migrate to `relationship_definitions` table | ⬜ |

**Deliverable:** Clean separation — fields store data columns, relationships store JPA mappings.

---

## Phase 6B: Relationship Template Generation

**Goal:** Generate production-ready JPA relationship annotations with full configuration.

| # | Task | Details | Status |
|---|------|---------|--------|
| 6B.1 | Update `Entity.java.ftl` — ManyToOne | Generate: `@ManyToOne(fetch = FetchType.${rel.fetchType})` + `@JoinColumn(name="${rel.joinColumn}")` + optional `@NotNull` | ⬜ |
| 6B.2 | Update `Entity.java.ftl` — OneToMany | Generate: `@OneToMany(mappedBy = "${rel.mappedBy}", cascade = CascadeType.${rel.cascadeType}, orphanRemoval = ${rel.orphanRemoval}, fetch = FetchType.${rel.fetchType})` | ⬜ |
| 6B.3 | Update `Entity.java.ftl` — OneToOne | Generate: `@OneToOne(fetch = FetchType.${rel.fetchType})` + `@JoinColumn` | ⬜ |
| 6B.4 | Update `Entity.java.ftl` — ManyToMany with JoinTable | Generate: `@ManyToMany` + `@JoinTable(name="${rel.joinTableName}", joinColumns = @JoinColumn(name="${rel.joinColumn}"), inverseJoinColumns = @JoinColumn(name="${rel.inverseJoinColumn}"))` | ⬜ |
| 6B.5 | Handle multiple cascade types | Support comma-separated: `cascade = {CascadeType.PERSIST, CascadeType.MERGE}` | ⬜ |
| 6B.6 | Generate Jackson annotations | `@JsonManagedReference` on parent side, `@JsonBackReference` on child side for bidirectional | ⬜ |
| 6B.7 | Generate `@JsonIgnore` option | When `dtoStrategy = IGNORE`, add `@JsonIgnore` on the field | ⬜ |
| 6B.8 | Generate relationship validation | `@NotNull` when `nullable = false` on ManyToOne/OneToOne | ⬜ |
| 6B.9 | Handle imports dynamically | Only import `JsonManagedReference`, `JsonBackReference`, `JsonIgnore`, `FetchType`, `CascadeType` when needed | ⬜ |
| 6B.10 | Handle `List` vs single entity | OneToMany/ManyToMany → `List<Target>`, ManyToOne/OneToOne → `Target` | ⬜ |

**Deliverable:** Generated entities have full, production-ready JPA relationships with proper serialization handling.

---

## Phase 6C: DTO Relationship Handling

**Goal:** Prevent circular JSON references, generate smart DTOs based on configurable strategy.

| # | Task | Details | Status |
|---|------|---------|--------|
| 6C.1 | Update `Dto.java.ftl` — ID_ONLY strategy | ManyToOne `Department department` → `Long departmentId` in DTO | ⬜ |
| 6C.2 | Update `Dto.java.ftl` — SUMMARY strategy | ManyToOne `Department department` → `DepartmentSummaryDto` (id + name only) | ⬜ |
| 6C.3 | Update `Dto.java.ftl` — NESTED strategy | Include full nested DTO (one level deep only, nested DTOs use ID_ONLY) | ⬜ |
| 6C.4 | Update `Dto.java.ftl` — IGNORE strategy | Skip relationship entirely in DTO | ⬜ |
| 6C.5 | Generate `SummaryDto` template | `{EntityName}SummaryDto.java` with only `id` + `name` fields | ⬜ |
| 6C.6 | Generate separate List vs Detail DTOs | `EmployeeListDto` (flat, for lists) vs `EmployeeDetailDto` (with nested relationships) | ⬜ |
| 6C.7 | Update `Controller.java.ftl` | Use `ListDto` for GET all, `DetailDto` for GET by ID | ⬜ |
| 6C.8 | Generate CreateRequest/UpdateRequest DTOs | Request DTOs accept `departmentId: Long` not full entity | ⬜ |
| 6C.9 | Update `ServiceImpl.java.ftl` | Add relationship resolution: `departmentRepository.findById(dto.getDepartmentId())` | ⬜ |
| 6C.10 | Generate mapper methods | Entity → ListDto, Entity → DetailDto, CreateRequest → Entity (with FK resolution) | ⬜ |
| 6C.11 | Handle collection relationships in DTOs | OneToMany → `List<Long> employeeIds` or `List<EmployeeSummaryDto>` based on strategy | ⬜ |
| 6C.12 | Prevent infinite recursion guaranteed | Template logic ensures no DTO ever contains a circular reference path | ⬜ |

**Deliverable:** Smart, recursion-safe DTOs with configurable relationship representation.

---

## Phase 6D: Generator Completeness — Other Features

**Goal:** Generated projects are fully functional with all declared operation types.

| # | Task | Details | Status |
|---|------|---------|--------|
| 6D.1 | Wire `application.properties.ftl` | Generate DB config, server port, JPA settings based on `ProjectDefinition` | ⬜ |
| 6D.2 | Generate Flyway migration for output projects | Create `V1__init.sql` with CREATE TABLE statements matching entities | ⬜ |
| 6D.3 | Generate Swagger/OpenAPI config | `SwaggerConfig.java` + annotations on generated controllers | ⬜ |
| 6D.4 | Generate auditing support | `@CreatedDate`, `@LastModifiedDate`, `@EntityListeners(AuditingEntityListener.class)`, `@EnableJpaAuditing` | ⬜ |
| 6D.5 | Implement PAGINATION template | `Pageable` parameter in controller, `Page<T>` return, `PagingAndSortingRepository` | ⬜ |
| 6D.6 | Implement SEARCH template | Spring Data JPA Specifications, dynamic query building from filter params | ⬜ |
| 6D.7 | Implement SOFT_DELETE template | Add `deleted` boolean + `deletedAt` field, override `findAll` to exclude deleted, `@SQLRestriction` | ⬜ |
| 6D.8 | Implement RESTORE template | Endpoint: `PATCH /{id}/restore`, set `deleted = false` | ⬜ |
| 6D.9 | Implement BULK_INSERT template | `POST /bulk` accepting `List<CreateRequest>`, batch persist | ⬜ |
| 6D.10 | Implement BULK_UPDATE template | `PUT /bulk` accepting list with IDs | ⬜ |
| 6D.11 | Implement BULK_DELETE template | `DELETE /bulk` accepting list of IDs | ⬜ |
| 6D.12 | Implement EXPORT_CSV template | `GET /export/csv` using OpenCSV or manual StringBuilder | ⬜ |
| 6D.13 | Implement EXPORT_EXCEL template | `GET /export/excel` using Apache POI | ⬜ |
| 6D.14 | Implement EXPORT_PDF template | `GET /export/pdf` using iText or OpenPDF | ⬜ |
| 6D.15 | Implement IMPORT_CSV template | `POST /import/csv` multipart file upload + parsing | ⬜ |
| 6D.16 | Implement IMPORT_EXCEL template | `POST /import/excel` multipart file upload | ⬜ |
| 6D.17 | Implement FILE_UPLOAD template | Generic file upload endpoint with storage service | ⬜ |
| 6D.18 | Implement FILE_DOWNLOAD template | Generic file download endpoint | ⬜ |
| 6D.19 | Implement AUDIT_LOG template | Entity change tracking table, event listener | ⬜ |
| 6D.20 | Implement VERSIONING template | `@Version` field, optimistic locking, version history table | ⬜ |
| 6D.21 | Implement STATUS_TRANSITION template | State machine pattern, allowed transitions config | ⬜ |
| 6D.22 | Implement WEBHOOK_INTEGRATION template | Event publisher + async webhook caller on entity changes | ⬜ |
| 6D.23 | Generate Maven Wrapper | Copy `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` into generated project | ⬜ |
| 6D.24 | Generate README.md | Template-based README with project name, setup instructions, API docs link | ⬜ |
| 6D.25 | Generate Dockerfile | Multi-stage build Dockerfile for the generated project | ⬜ |
| 6D.26 | Generate `.gitignore` | Standard Java/Maven gitignore | ⬜ |

**Deliverable:** Every declared `OperationType` actually generates working code. Output projects are complete and deployment-ready.

---

## Phase 7: Async Generation + File Cleanup

**Goal:** Handle large projects gracefully, don't fill disk.

| # | Task | Details | Status |
|---|------|---------|--------|
| 7.1 | Create V4 Flyway migration | Table: `generation_jobs` | ⬜ |
| 7.2 | Create `GenerationJob` entity | Fields: `jobId` (UUID), `userId`, `projectName`, `status` (QUEUED/PROCESSING/COMPLETED/FAILED), `stage`, `progress` (0-100), `zipPath`, `errorMessage`, `startedAt`, `completedAt` | ⬜ |
| 7.3 | Create `GenerationJobRepository` | Standard JPA repo + custom queries for status | ⬜ |
| 7.4 | Configure `@EnableAsync` + thread pool | `TaskExecutor` bean with configurable pool size | ⬜ |
| 7.5 | Refactor `ProjectGenerationService` | Make `generate()` async, return `jobId` immediately | ⬜ |
| 7.6 | Update `GeneratorController` | `POST /api/v1/generator/generate` → returns `{ jobId, status: "QUEUED" }` (HTTP 202 Accepted) | ⬜ |
| 7.7 | Add status endpoint | `GET /api/v1/generator/status/{jobId}` → returns `{ jobId, status, stage, progress, errorMessage }` | ⬜ |
| 7.8 | Add download endpoint | `GET /api/v1/generator/download/{jobId}` → returns ZIP (404 if not ready, 410 if expired) | ⬜ |
| 7.9 | Progress tracking | Update `GenerationJob.progress` at each stage (10% validate → 30% structure → 50% entities → 70% security → 90% build → 100% zip) | ⬜ |
| 7.10 | Scheduled file cleanup | `@Scheduled(cron)` job: delete ZIPs and generated folders older than `${APP_CLEANUP_RETENTION_HOURS:24}` hours | ⬜ |
| 7.11 | Mark expired jobs | Update job status to `EXPIRED` when files are cleaned up | ⬜ |
| 7.12 | Configurable retention | Property: `app.cleanup.retention-hours=24` | ⬜ |
| 7.13 | Concurrent generation limit | Max simultaneous generations per user (e.g., 3) | ⬜ |

**Deliverable:** Non-blocking generation, real-time progress, automatic disk cleanup.

---

## Phase 8: Test Coverage (80%+)

**Goal:** Comprehensive test suite proving the system works. Target 80%+ line coverage.

| # | Task | Details | Status |
|---|------|---------|--------|
| 8.1 | Configure JaCoCo | Add to pom.xml, configure report generation, set 80% minimum threshold | ⬜ |
| 8.2 | Unit tests: `FreemarkerTemplateEngine` | Test template rendering with various model inputs | ⬜ |
| 8.3 | Unit tests: `Entity.java.ftl` template | Verify generated entity code for: simple fields, relationships (all 4 types), validations, all data types | ⬜ |
| 8.4 | Unit tests: `Dto.java.ftl` template | Verify: ID_ONLY, SUMMARY, NESTED, IGNORE strategies | ⬜ |
| 8.5 | Unit tests: `Controller.java.ftl` template | Verify: CRUD endpoints, pagination, search, bulk operations | ⬜ |
| 8.6 | Unit tests: `ServiceImpl.java.ftl` template | Verify: business logic, relationship resolution | ⬜ |
| 8.7 | Unit tests: `pom.xml.ftl` template | Verify: correct dependencies for each DB type, security type, options | ⬜ |
| 8.8 | Unit tests: `DtoMapper`, `ProjectMapper` | Edge cases, null handling, relationship mapping | ⬜ |
| 8.9 | Unit tests: `ProjectValidator` | Missing entities, missing fields, missing primary keys, invalid configs | ⬜ |
| 8.10 | Unit tests: Service layer | Mock repositories, test business logic for `ProjectDefinitionServiceImpl`, `EntityDefinitionServiceImpl`, `ProjectGenerationServiceImpl` | ⬜ |
| 8.11 | Unit tests: Security generators | Verify each type generates expected files | ⬜ |
| 8.12 | Integration tests: `ProjectDefinitionController` | `@WebMvcTest` — all CRUD endpoints, validation errors, 404s | ⬜ |
| 8.13 | Integration tests: `EntityDefinitionController` | `@WebMvcTest` — all CRUD endpoints, nested resources | ⬜ |
| 8.14 | Integration tests: `GeneratorController` | `@WebMvcTest` — generation request, async flow | ⬜ |
| 8.15 | Integration tests: `AuthController` | `@WebMvcTest` — register, login, refresh, invalid credentials | ⬜ |
| 8.16 | Integration tests: Repository layer | `@DataJpaTest` — queries, cascading, orphan removal | ⬜ |
| 8.17 | E2E test: Full generation pipeline | Generate a multi-entity project with relationships → verify file structure → verify compiles | ⬜ |
| 8.18 | E2E test: Security generation | Generate project with each security type → verify compiles + starts | ⬜ |
| 8.19 | Security tests | Auth required, roles enforced, invalid tokens rejected, API key validation | ⬜ |
| 8.20 | Error scenario tests | Duplicate names, missing required fields, invalid enum values, oversized requests | ⬜ |
| 8.21 | Async generation tests | Job creation, status polling, download after completion, timeout handling | ⬜ |
| 8.22 | Add test profile | `application-test.properties` with H2, fast config | ⬜ |

**Deliverable:** 80%+ coverage, all critical paths tested, JaCoCo report in CI.

---

## Phase 9: Documentation

**Goal:** Professional, accurate documentation that matches the actual codebase.

| # | Task | Details | Status |
|---|------|---------|--------|
| 9.1 | Rewrite `README.md` | Accurate architecture overview, correct port/DB info, setup guide, API summary, badges | ⬜ |
| 9.2 | Create `CONTRIBUTING.md` | Code style, PR process, branch naming, testing requirements | ⬜ |
| 9.3 | Create `CHANGELOG.md` | Versioned change history starting from v1.0.0 | ⬜ |
| 9.4 | Create `LICENSE` file | Choose and add license (MIT or Apache 2.0) | ⬜ |
| 9.5 | Fix pom.xml metadata | Real license, developer name/email, SCM URL, project URL | ⬜ |
| 9.6 | Swagger/OpenAPI annotations | `@Tag` on controllers, `@Operation` + `@ApiResponse` on all endpoints, `@Schema` on DTOs | ⬜ |
| 9.7 | Create Postman collection | Accurate, working collection matching current API v1 | ⬜ |
| 9.8 | Create architecture diagram | Mermaid or draw.io diagram showing system components | ⬜ |
| 9.9 | Document environment variables | Table of all env vars with descriptions, defaults, required/optional | ⬜ |
| 9.10 | Document API request/response examples | For every endpoint, show request body + response body | ⬜ |
| 9.11 | Document supported OperationTypes | What each one generates, with code examples | ⬜ |
| 9.12 | Document relationship configuration | How to define relationships, what gets generated for each strategy | ⬜ |

**Deliverable:** Complete, accurate docs for users, contributors, and API consumers.

---

## Phase 10: Dockerization

**Goal:** One command to run anywhere.

| # | Task | Details | Status |
|---|------|---------|--------|
| 10.1 | Create multi-stage `Dockerfile` | Stage 1: Maven build. Stage 2: JRE runtime. Final image ~200MB. | ⬜ |
| 10.2 | Create `docker-compose.yml` | Services: `app` (Spring Boot), `db` (PostgreSQL), optional `pgadmin` | ⬜ |
| 10.3 | Create `.dockerignore` | Exclude `target/`, `.git/`, `.idea/`, `*.iml`, `generated-projects/` | ⬜ |
| 10.4 | Configure health check | `/actuator/health` endpoint, Docker HEALTHCHECK instruction | ⬜ |
| 10.5 | Configure graceful shutdown | `server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase=30s` | ⬜ |
| 10.6 | Production Logback config | JSON format logging when `PROFILE=docker` or `PROFILE=prod` | ⬜ |
| 10.7 | Volume mounts | `generated-projects/` directory mapped to host for persistence | ⬜ |
| 10.8 | Maven available in container | Install Maven in the runtime container so `BuildVerifier` works | ⬜ |
| 10.9 | Docker Compose profiles | `docker compose --profile dev up` vs `docker compose --profile prod up` | ⬜ |
| 10.10 | Add Kubernetes manifests (optional) | Deployment, Service, ConfigMap, Secret, PVC for generated-projects | ⬜ |

**Deliverable:** `docker compose up` starts everything. Production-ready containerization.

---

## Phase 11: Frontend Generation

**Goal:** Generate not just backend but also frontend code for the defined entities.

| # | Task | Details | Status |
|---|------|---------|--------|
| 11.1 | React CRUD page template | Generate: list page (table with pagination), create form, edit form, detail view per entity | ⬜ |
| 11.2 | React API client generation | Axios/fetch service with typed methods matching generated endpoints | ⬜ |
| 11.3 | Form generation with relationships | Dropdown selects for ManyToOne (populated via API), multi-select for ManyToMany | ⬜ |
| 11.4 | OpenAPI client generation | Generate TypeScript client from OpenAPI spec of generated project | ⬜ |
| 11.5 | Admin dashboard template | Layout with sidebar navigation, entity CRUD pages, overview dashboard | ⬜ |
| 11.6 | Add `frontendEnabled` to `ProjectDefinition` | Allow user to opt-in to frontend generation | ⬜ |
| 11.7 | Frontend build integration | Generate `package.json`, include frontend in the ZIP output | ⬜ |

**Deliverable:** Full-stack project generation — backend + frontend from a single definition.

---

## Database Schema

### Existing Tables (Phase 1 Migration)

```sql
-- V1__create_core_tables.sql

CREATE TABLE project_definitions (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL UNIQUE,
    description           TEXT,
    package_name          VARCHAR(255) NOT NULL,
    database_type         VARCHAR(50) DEFAULT 'H2',
    security_enabled      BOOLEAN DEFAULT FALSE,
    security_type         VARCHAR(50),
    caching_enabled       BOOLEAN DEFAULT FALSE,
    swagger_enabled       BOOLEAN DEFAULT TRUE,
    custom_configuration  TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE entity_definitions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    project_id  BIGINT NOT NULL REFERENCES project_definitions(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(name, project_id)
);

CREATE TABLE field_definitions (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    data_type           VARCHAR(50) NOT NULL,
    field_type          VARCHAR(50) NOT NULL,
    validation_rules    VARCHAR(500),
    relationship_type   VARCHAR(50),
    relationship_target VARCHAR(255),
    nullable            BOOLEAN DEFAULT TRUE,
    default_value       VARCHAR(255),
    reference_entity    VARCHAR(255),
    reference_field     VARCHAR(255),
    entity_id           BIGINT NOT NULL REFERENCES entity_definitions(id) ON DELETE CASCADE
);

CREATE TABLE operation_configs (
    id             BIGSERIAL PRIMARY KEY,
    operation_type VARCHAR(50) NOT NULL,
    enabled        BOOLEAN DEFAULT TRUE,
    custom_logic   TEXT,
    parameters     TEXT,
    entity_id      BIGINT NOT NULL REFERENCES entity_definitions(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_entity_definitions_project_id ON entity_definitions(project_id);
CREATE INDEX idx_field_definitions_entity_id ON field_definitions(entity_id);
CREATE INDEX idx_operation_configs_entity_id ON operation_configs(entity_id);
```

### User & Auth Tables (Phase 4 Migration)

```sql
-- V2__create_user_tables.sql

CREATE TABLE app_users (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(255),
    role           VARCHAR(50) NOT NULL DEFAULT 'USER',
    enabled        BOOLEAN DEFAULT TRUE,
    locked         BOOLEAN DEFAULT FALSE,
    api_key        VARCHAR(255) UNIQUE,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at  TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(500) NOT NULL UNIQUE,
    user_id     BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Add user ownership to projects
ALTER TABLE project_definitions ADD COLUMN user_id BIGINT REFERENCES app_users(id);

-- Indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_project_definitions_user_id ON project_definitions(user_id);
CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_app_users_api_key ON app_users(api_key);
```

### Audit & Logging Tables (Phase 3 Migration)

```sql
-- V3__create_logging_tables.sql

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    request_id      VARCHAR(36) NOT NULL,
    user_id         BIGINT,
    method          VARCHAR(10) NOT NULL,
    endpoint        VARCHAR(500) NOT NULL,
    request_body    TEXT,
    response_status INTEGER,
    error_message   TEXT,
    stack_trace     TEXT,
    stage           VARCHAR(50),
    duration_ms     BIGINT,
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE application_logs (
    id          BIGSERIAL PRIMARY KEY,
    level       VARCHAR(10) NOT NULL,
    logger      VARCHAR(255) NOT NULL,
    message     TEXT NOT NULL,
    stack_trace TEXT,
    request_id  VARCHAR(36),
    user_id     BIGINT,
    context     JSONB,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_endpoint ON audit_logs(endpoint);
CREATE INDEX idx_application_logs_level ON application_logs(level);
CREATE INDEX idx_application_logs_created_at ON application_logs(created_at);
CREATE INDEX idx_application_logs_request_id ON application_logs(request_id);
```

### Generation Jobs Table (Phase 7 Migration)

```sql
-- V4__create_generation_jobs.sql

CREATE TABLE generation_jobs (
    id              BIGSERIAL PRIMARY KEY,
    job_id          VARCHAR(36) NOT NULL UNIQUE,
    user_id         BIGINT REFERENCES app_users(id),
    project_name    VARCHAR(255) NOT NULL,
    request_payload TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'QUEUED',
    stage           VARCHAR(50),
    progress        INTEGER DEFAULT 0,
    zip_path        VARCHAR(500),
    error_message   TEXT,
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_generation_jobs_job_id ON generation_jobs(job_id);
CREATE INDEX idx_generation_jobs_user_id ON generation_jobs(user_id);
CREATE INDEX idx_generation_jobs_status ON generation_jobs(status);
CREATE INDEX idx_generation_jobs_expires_at ON generation_jobs(expires_at);
```

### Relationship Definitions Table (Phase 6A Migration)

```sql
-- V5__create_relationship_definitions.sql

CREATE TABLE relationship_definitions (
    id                   BIGSERIAL PRIMARY KEY,
    entity_id            BIGINT NOT NULL REFERENCES entity_definitions(id) ON DELETE CASCADE,
    relationship_type    VARCHAR(50) NOT NULL,
    target_entity        VARCHAR(255) NOT NULL,
    field_name           VARCHAR(255) NOT NULL,
    mapped_by            VARCHAR(255),
    fetch_type           VARCHAR(10) DEFAULT 'LAZY',
    cascade_types        VARCHAR(255),
    orphan_removal       BOOLEAN DEFAULT FALSE,
    join_column          VARCHAR(255),
    join_table_name      VARCHAR(255),
    inverse_join_column  VARCHAR(255),
    nullable             BOOLEAN DEFAULT TRUE,
    dto_strategy         VARCHAR(20) DEFAULT 'ID_ONLY',
    json_handling        VARCHAR(30) DEFAULT 'BACK_REFERENCE',
    created_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_relationship_defs_entity_id ON relationship_definitions(entity_id);
```

---

## Flyway Migrations Order

| File | Phase | Tables |
|------|-------|--------|
| `V1__create_core_tables.sql` | Phase 1 | project_definitions, entity_definitions, field_definitions, operation_configs |
| `V2__create_user_tables.sql` | Phase 4 | app_users, refresh_tokens, alter project_definitions |
| `V3__create_logging_tables.sql` | Phase 3 | audit_logs, application_logs |
| `V4__create_generation_jobs.sql` | Phase 7 | generation_jobs |
| `V5__create_relationship_definitions.sql` | Phase 6A | relationship_definitions |

> **Note:** Flyway executes migrations in version order (V1, V2, V3...) regardless of which phase you build first. We number them by dependency order, not phase order. In practice, you'll create migration files as you reach each phase.

---

## Estimated Timeline

| Phase | Effort | Depends On |
|-------|--------|------------|
| Phase 1: Clean Foundation | 2-3 days | — |
| Phase 2: Multi-Environment | 1-2 days | Phase 1 |
| Phase 3: Error Handling + Logging | 3-4 days | Phase 1 |
| Phase 4: Security | 5-6 days | Phase 1, 2 |
| Phase 5: API Improvements | 3-4 days | Phase 4 |
| Phase 6A: Relationship Model | 3-4 days | Phase 5 |
| Phase 6B: Relationship Templates | 4-5 days | Phase 6A |
| Phase 6C: DTO Handling | 4-5 days | Phase 6B |
| Phase 6D: Generator Completeness | 8-10 days | Phase 6C |
| Phase 7: Async + Cleanup | 3-4 days | Phase 6D |
| Phase 8: Test Coverage | 6-8 days | All above |
| Phase 9: Documentation | 2-3 days | All above |
| Phase 10: Dockerization | 2-3 days | Phase 8 |
| Phase 11: Frontend Generation | 7-10 days | Phase 6D |
| **TOTAL** | **~50-65 days** | |

---

## ER Diagram (Final State)

```
┌─────────────────────┐       ┌─────────────────────┐
│     app_users       │       │   refresh_tokens    │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │──┐    │ id (PK)             │
│ email (UNIQUE)      │  │    │ token (UNIQUE)      │
│ password_hash       │  └───▶│ user_id (FK)        │
│ full_name           │       │ expires_at          │
│ role                │       │ revoked             │
│ api_key (UNIQUE)    │       └─────────────────────┘
│ enabled, locked     │
│ created_at          │
└────────┬────────────┘
         │ 1:N
         ▼
┌─────────────────────┐       ┌─────────────────────┐
│ project_definitions │       │  generation_jobs    │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │
│ user_id (FK)        │◀──────│ user_id (FK)        │
│ name (UNIQUE)       │       │ job_id (UUID)       │
│ package_name        │       │ project_name        │
│ database_type       │       │ status, stage       │
│ security_enabled    │       │ progress (0-100)    │
│ security_type       │       │ zip_path            │
│ caching_enabled     │       │ error_message       │
│ swagger_enabled     │       │ started_at          │
│ created_at          │       │ completed_at        │
└────────┬────────────┘       └─────────────────────┘
         │ 1:N
         ▼
┌─────────────────────────────────────────────────┐
│              entity_definitions                  │
├─────────────────────────────────────────────────┤
│ id (PK)                                         │
│ project_id (FK)                                 │
│ name (UNIQUE per project)                       │
│ description                                     │
│ created_at, updated_at                          │
└────────┬──────────────────┬─────────────────────┘
         │ 1:N              │ 1:N              │ 1:N
         ▼                  ▼                  ▼
┌──────────────────┐ ┌──────────────────┐ ┌────────────────────────┐
│ field_definitions│ │ operation_configs│ │ relationship_definitions│
├──────────────────┤ ├──────────────────┤ ├────────────────────────┤
│ id (PK)          │ │ id (PK)          │ │ id (PK)                │
│ entity_id (FK)   │ │ entity_id (FK)   │ │ entity_id (FK)         │
│ name             │ │ operation_type   │ │ relationship_type      │
│ data_type        │ │ enabled          │ │ target_entity          │
│ field_type       │ │ custom_logic     │ │ field_name             │
│ validation_rules │ │ parameters       │ │ mapped_by              │
│ nullable         │ └──────────────────┘ │ fetch_type             │
│ default_value    │                      │ cascade_types          │
└──────────────────┘                      │ orphan_removal         │
                                          │ join_column            │
                                          │ join_table_name        │
                                          │ inverse_join_column    │
                                          │ nullable               │
                                          │ dto_strategy           │
                                          │ json_handling          │
                                          └────────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│     audit_logs      │       │  application_logs   │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │
│ request_id (UUID)   │       │ level               │
│ user_id             │       │ logger              │
│ method, endpoint    │       │ message             │
│ request_body        │       │ stack_trace         │
│ response_status     │       │ request_id          │
│ error_message       │       │ user_id             │
│ stack_trace         │       │ context (JSONB)     │
│ stage               │       │ created_at          │
│ duration_ms         │       └─────────────────────┘
│ ip_address          │
│ created_at          │
└─────────────────────┘
```

---

## Quick Start (Database Setup)

```sql
-- 1. Connect to PostgreSQL as superuser
psql -U postgres

-- 2. Create database
CREATE DATABASE user_driven_operation_mng_sys;

-- 3. That's it! Flyway handles all table creation on app startup.
```

---

## Summary

| Total Tables | 9 |
|---|---|
| Total Phases | 11 (with 6 split into 6A/6B/6C/6D) |
| Estimated Total Effort | 50-65 working days |
| Test Coverage Target | 80%+ |
| Database | PostgreSQL (H2 for dev/test) |
| Schema Strategy | Flyway migrations (no ddl-auto in prod) |
| Separate DB Schema? | No — use default `public` schema |

---

**Ready to begin? Say "Start Phase 1" and we'll go.**
