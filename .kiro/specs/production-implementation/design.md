# Design Document: Production Implementation

## Overview

This design transforms the Dynamic Backend Generator from a prototype into a production-ready Spring Boot Low-Code Engine. The Generator accepts project and entity definitions via REST API and produces complete, compilable Spring Boot applications as downloadable ZIP files.

The production implementation spans 11 phases addressing: codebase hygiene, database migration management, multi-environment configuration, structured error handling, audit logging, host API security, security code generation for output projects, API standards, relationship modeling and code generation, DTO strategies, operation completeness, asynchronous generation, test coverage, documentation, containerization, and frontend generation.

**Key Design Decisions:**
- **Flyway for schema management** — versioned migrations replace Hibernate auto-DDL in production, ensuring repeatable deployments
- **Async generation with job tracking** — long-running generation moves to a thread pool with polling-based status updates
- **Dedicated relationship model** — relationships decouple from field definitions into their own table with rich configuration
- **Strategy-based DTO generation** — four strategies (ID_ONLY, SUMMARY, NESTED, IGNORE) prevent circular references while giving users control
- **Layered security** — JWT + API key authentication on the host API; pluggable security generators for output projects
- **Profile-driven configuration** — dev/prod/docker profiles with externalized environment variables

## Architecture

```mermaid
graph TB
    subgraph "Host API Layer"
        AUTH[Auth Controller]
        PC[Project Controller]
        EC[Entity Controller]
        GC[Generator Controller]
    end

    subgraph "Security Layer"
        JF[JWT Filter]
        AKF[API Key Filter]
        RL[Rate Limiter]
    end

    subgraph "Service Layer"
        AS[Auth Service]
        PS[Project Service]
        ES[Entity Service]
        PGS[Project Generation Service]
    end

    subgraph "Async Generation Pipeline"
        JM[Job Manager]
        TP[Thread Pool Executor]
        JP[Job Progress Tracker]
    end

    subgraph "Generator Core"
        PG[Project Generator Orchestrator]
        PSG[Project Structure Generator]
        PMG[Pom Generator]
        AG[Application Generator]
        EMG[Entity Module Generator]
        SGF[Security Generator Factory]
        FG[Frontend Generator]
    end

    subgraph "Template Engine"
        FTE[FreeMarker Template Engine]
        ET[Entity Templates]
        RT[Relationship Templates]
        ST[Security Templates]
        OT[Operation Templates]
        FRT[Frontend Templates]
    end

    subgraph "Data Layer"
        PR[Project Repository]
        ER[Entity Repository]
        RR[Relationship Repository]
        JR[Job Repository]
        UR[User Repository]
        ALR[Audit Log Repository]
    end

    subgraph "Infrastructure"
        FM[Flyway Migrations]
        LB[Logback + DB Appender]
        AI[Audit Interceptor]
        SC[Scheduled Cleanup]
    end

    AUTH --> AS
    PC --> PS
    EC --> ES
    GC --> PGS

    JF --> AUTH & PC & EC & GC
    AKF --> PC & EC & GC
    RL --> GC

    PGS --> JM
    JM --> TP
    TP --> PG
    PG --> PSG & PMG & AG & EMG & SGF & FG

    EMG --> FTE
    SGF --> FTE
    FG --> FTE

    PS --> PR
    ES --> ER & RR
    JM --> JR
    AS --> UR
    AI --> ALR
```

### Package Structure (Target)

```
com.user.driven.operations
├── app
│   ├── api
│   │   ├── controller/          # REST controllers (versioned under /api/v1/)
│   │   ├── dto/                 # Request/Response DTOs and envelope
│   │   └── mapper/              # Entity-DTO mappers
│   ├── common
│   │   ├── exception/           # Exception hierarchy + GlobalExceptionHandler
│   │   ├── logging/             # DatabaseAppender, AuditInterceptor, MDC filter
│   │   └── util/                # Constants, FileUtils, ZipUtil
│   ├── config/                  # Security, Swagger, Async, AppProperties
│   ├── core
│   │   ├── model/               # JPA entities (ProjectDefinition, EntityDefinition, etc.)
│   │   ├── repository/          # Spring Data JPA repositories
│   │   └── service/             # Service interfaces and implementations
│   └── security/                # JWT, API key filters, UserDetails, rate limiting
├── generator
│   ├── core/                    # BaseGenerator, TemplateEngine, FileWriterService
│   ├── module/                  # EntityModuleGenerator, OperationGenerators
│   ├── orchestrator/            # ProjectGenerator (orchestration)
│   ├── project/                 # PomGenerator, ApplicationGenerator, StructureGenerator
│   ├── relationship/            # RelationshipCodeGenerator, DtoStrategyGenerators
│   ├── security/                # SecurityGenerator implementations (JWT, Basic, OAuth2, Session)
│   ├── frontend/                # FrontendGenerator, React template processors
│   └── utils/                   # NamingUtils, RelationshipGeneratorUtil
└── enums/                       # All enumerations
```

## Components and Interfaces

### 1. Configuration Components

```java
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String generatedProjectsDirectory = "./generated-projects";
    private String mavenExecutable = "mvn";
    private int asyncPoolSize = 5;
    private int jobRetentionHours = 24;
    private int auditRetentionDays = 30;
}
```

**Profile Configuration:**

| Property | dev | prod | docker |
|----------|-----|------|--------|
| Database | H2 in-memory | PostgreSQL | PostgreSQL (service: db) |
| ddl-auto | create-drop | validate | validate |
| Flyway | disabled | enabled | enabled |
| Port | 8083 | 8080 | 8080 |
| Logging | console (DEBUG) | JSON (INFO) + rolling-file + DB | JSON (INFO) + rolling-file + DB |

### 2. Exception Hierarchy

```java
public abstract class BaseApplicationException extends RuntimeException {
    private final String resourceType;
    private final Object resourceIdentifier;
    private final String operationStage;
}

public class ProjectNotFoundException extends BaseApplicationException { }
public class DuplicateNameException extends BaseApplicationException { }
public class GenerationFailedException extends BaseApplicationException { }
public class ValidationException extends BaseApplicationException { }
public class BuildVerificationException extends BaseApplicationException { }
public class UnsupportedSecurityTypeException extends BaseApplicationException { }
```

**ErrorResponse:**
```java
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String errorType,
    String message,
    String path,
    String requestId,
    List<FieldError> fieldErrors  // only for validation exceptions
) {}
```

### 3. Security Components

```java
public interface JwtService {
    String generateAccessToken(AppUser user);     // 15 min expiry
    String generateRefreshToken(AppUser user);    // 7 day expiry
    Claims validateToken(String token);
}

public class JwtAuthenticationFilter extends OncePerRequestFilter { }
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter { }
public class RateLimitFilter extends OncePerRequestFilter { }  // 10 req/min on generation
```

**SecurityFilterChain order:** RateLimit → ApiKey → JWT → Authorization

**Public endpoints:** `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**`, `/actuator/health`

### 4. Audit Components

```java
public class RequestIdFilter extends OncePerRequestFilter {
    // Generates X-Request-Id UUID, sets MDC, adds to response header
}

@Component
public class AuditInterceptor implements HandlerInterceptor {
    // preHandle: create Audit_Log with request metadata
    // afterCompletion: update duration, response status
}

public class DatabaseAppender extends AppenderBase<ILoggingEvent> {
    // Persists ERROR/WARN logs to application_logs table
}
```

### 5. Async Generation Pipeline

```java
public interface GenerationJobService {
    GenerationJob submit(Long projectId, Long userId);
    GenerationJob getStatus(UUID jobId);
    Resource download(UUID jobId);
}

public class AsyncGenerationExecutor {
    @Async("generationTaskExecutor")
    public void executeGeneration(GenerationJob job, ProjectDefinition project) {
        // Pipeline: VALIDATION(10%) → STRUCTURE(30%) → ENTITIES(50%)
        //           → SECURITY(70%) → BUILD(90%) → ZIP(100%)
    }
}
```

### 6. Generator Components

**Relationship Code Generator:**
```java
public interface RelationshipCodeGenerator {
    String generateAnnotations(RelationshipDefinition rel, boolean isBidirectional);
    String generateImports(List<RelationshipDefinition> rels);
    String generateField(RelationshipDefinition rel);
}
```

**DTO Strategy Generator:**
```java
public interface DtoStrategyGenerator {
    String generateDtoField(RelationshipDefinition rel, EntityDefinition target);
    Optional<String> generateSummaryDto(EntityDefinition target);
}

// Implementations: IdOnlyStrategy, SummaryStrategy, NestedStrategy, IgnoreStrategy
```

**Operation Template Registry:**
```java
@Component
public class OperationTemplateRegistry {
    // Maps OperationType → template path + model builder
    // Handles: PAGINATION, SEARCH, SOFT_DELETE, BULK_*, EXPORT_*, IMPORT_*,
    //          AUDIT_LOG, VERSIONING, STATUS_TRANSITION, WEBHOOK_INTEGRATION,
    //          FILE_UPLOAD, FILE_DOWNLOAD
}
```

**Frontend Generator:**
```java
@Component
public class FrontendGenerator {
    void generate(ProjectDefinition project, Path basePath);
    // Produces: React app with CRUD pages, API client, admin dashboard
    // Maps field types to input components
}
```

### 7. API Response Envelope

```java
public record ApiEnvelope<T>(
    boolean success,
    T data,
    PaginationMeta pagination,  // null for non-collection responses
    String timestamp,           // ISO-8601 UTC
    String requestId,
    ErrorInfo error             // null for success responses
) {}

public record PaginationMeta(int page, int size, long totalElements, int totalPages) {}
```

## Data Models

### Entity Relationship Diagram

```mermaid
erDiagram
    APP_USER {
        bigserial id PK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar role
        varchar api_key UK
        timestamp created_at
        timestamp updated_at
    }

    PROJECT_DEFINITIONS {
        bigserial id PK
        varchar name UK
        varchar description
        varchar package_name
        varchar database_type
        boolean security_enabled
        varchar security_type
        boolean caching_enabled
        boolean swagger_enabled
        boolean frontend_enabled
        text custom_configuration
        bigint owner_id FK
        timestamp created_at
        timestamp updated_at
    }

    ENTITY_DEFINITIONS {
        bigserial id PK
        varchar name
        varchar description
        bigint project_id FK
        timestamp created_at
        timestamp updated_at
    }

    FIELD_DEFINITIONS {
        bigserial id PK
        varchar name
        varchar description
        varchar data_type
        varchar field_type
        varchar validation_rules
        varchar relationship_type "DEPRECATED"
        varchar relationship_target "DEPRECATED"
        boolean nullable
        varchar default_value
        bigint entity_id FK
        varchar reference_entity
        varchar reference_field
    }

    RELATIONSHIP_DEFINITIONS {
        bigserial id PK
        bigint source_entity_id FK
        varchar field_name
        varchar relationship_type
        varchar target_entity_name
        varchar mapped_by
        varchar fetch_type
        varchar cascade_types
        boolean orphan_removal
        varchar join_column_name
        varchar join_table_name
        varchar inverse_join_column_name
        boolean nullable
        varchar dto_strategy
        timestamp created_at
    }

    OPERATION_CONFIGS {
        bigserial id PK
        varchar operation_type
        text configuration
        bigint entity_id FK
    }

    GENERATION_JOBS {
        uuid id PK
        bigint project_id FK
        bigint user_id FK
        varchar status
        varchar current_stage
        int progress
        varchar error_message
        varchar output_path
        timestamp created_at
        timestamp completed_at
        timestamp expires_at
    }

    AUDIT_LOGS {
        bigserial id PK
        uuid request_id
        varchar http_method
        varchar endpoint
        text request_body
        int response_status
        bigint duration_ms
        varchar client_ip
        varchar user_agent
        varchar generation_stage
        bigint user_id
        timestamp created_at
    }

    APPLICATION_LOGS {
        bigserial id PK
        varchar log_level
        varchar logger_name
        text message
        text stack_trace
        uuid request_id
        bigint user_id
        jsonb context
        timestamp created_at
    }

    APP_USER ||--o{ PROJECT_DEFINITIONS : owns
    PROJECT_DEFINITIONS ||--o{ ENTITY_DEFINITIONS : contains
    ENTITY_DEFINITIONS ||--o{ FIELD_DEFINITIONS : has
    ENTITY_DEFINITIONS ||--o{ RELATIONSHIP_DEFINITIONS : sources
    ENTITY_DEFINITIONS ||--o{ OPERATION_CONFIGS : configures
    PROJECT_DEFINITIONS ||--o{ GENERATION_JOBS : generates
    APP_USER ||--o{ GENERATION_JOBS : submits
```

### Generation Job State Machine

```mermaid
stateDiagram-v2
    [*] --> QUEUED: Submit request
    QUEUED --> PROCESSING: Thread picks up
    PROCESSING --> COMPLETED: Success
    PROCESSING --> FAILED: Error
    COMPLETED --> EXPIRED: Cleanup job (24h)
    FAILED --> [*]: Terminal
    EXPIRED --> [*]: Terminal
```

### Flyway Migration Strategy

| Version | Description |
|---------|-------------|
| V1 | Create project_definitions, entity_definitions, field_definitions, operation_configs |
| V2 | Create relationship_definitions, migrate legacy field relationship data |
| V3 | Create app_users, generation_jobs |
| V4 | Create audit_logs, application_logs |
| V5 | Add owner_id to project_definitions, frontend_enabled column |
| V6 | Add indexes on foreign keys and frequently queried columns |

