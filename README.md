# Dynamic Backend Generator

A Spring Boot Low-Code Engine that generates complete, production-ready Spring Boot backend projects from entity definitions provided via REST API. Define your entities, fields, relationships, and operations — get a fully functional Spring Boot project as a downloadable ZIP.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                        │
│  ProjectDefinitionController  EntityDefinitionController│
│                GeneratorController                      │
├─────────────────────────────────────────────────────────┤
│                   Service Layer                         │
│  ProjectDefinitionService  EntityDefinitionService      │
│              ProjectGenerationService                   │
├─────────────────────────────────────────────────────────┤
│                  Generator Engine                       │
│  ProjectGenerator → EntityModuleGenerator               │
│  FreemarkerTemplateEngine → BuildVerifier               │
├─────────────────────────────────────────────────────────┤
│                   Data Layer                            │
│  JPA Repositories (PostgreSQL / H2)                    │
│  Flyway Migrations                                     │
└─────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.user.driven.operations/
├── app/
│   ├── api/
│   │   ├── controller/    # REST controllers
│   │   ├── dto/           # Request/Response DTOs
│   │   └── mapper/        # DTO ↔ Entity mappers
│   ├── common/
│   │   ├── exception/     # Global exception handler
│   │   └── util/          # Constants, file utilities
│   ├── config/            # Spring configuration
│   └── core/
│       ├── model/         # JPA entities
│       ├── repository/    # Spring Data repositories
│       └── service/       # Business logic
├── enums/                 # OperationType, RelationshipType, etc.
└── generator/
    ├── core/              # Template engine, build verifier
    ├── module/            # Entity/module generators
    ├── orchestrator/      # Project generation orchestrator
    ├── project/           # POM, application, validator generators
    └── utils/             # Naming utilities
```

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17 LTS |
| Framework | Spring Boot | 3.5.0 |
| Build Tool | Maven | 3.6+ |
| Database (prod) | PostgreSQL | 15+ |
| Database (dev) | H2 | In-memory |
| Schema Migration | Flyway | (managed by Spring Boot) |
| Template Engine | FreeMarker | (managed by Spring Boot) |
| API Docs | SpringDoc OpenAPI | 2.2.0 |
| Utility | Lombok | (managed by Spring Boot) |

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **PostgreSQL 15+** (for production profile only)

```bash
java -version   # Java 17+
mvn -version    # Maven 3.6+
```

## Quick Start

### Development Mode (H2 — no database setup required)

```bash
# Clone and build
git clone <repository-url>
cd Dynamic-Backend-Generator-Spring-Boot-Low-Code-Engine
mvn clean compile

# Run with dev profile (default)
mvn spring-boot:run
```

The app starts on **http://localhost:8083** with H2 in-memory database.

- Swagger UI: http://localhost:8083/swagger-ui.html
- H2 Console: http://localhost:8083/h2-console (URL: `jdbc:h2:mem:testdb`, user: `sa`, no password)

### Production Mode (PostgreSQL)

```bash
# 1. Create the database
psql -U postgres -c "CREATE DATABASE user_driven_operation_mng_sys;"

# 2. Set environment variables (or use defaults)
export DB_URL=jdbc:postgresql://localhost:5432/user_driven_operation_mng_sys
export DB_USERNAME=postgres
export DB_PASSWORD=your_password

# 3. Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The app starts on **http://localhost:8080**. Flyway creates all tables automatically on first startup.

## Environment Profiles

| Profile | Database | DDL Mode | Flyway | Port | Use Case |
|---------|----------|----------|--------|------|----------|
| `dev` (default) | H2 in-memory | create-drop | Disabled | 8083 | Local development |
| `prod` | PostgreSQL | validate | Enabled | 8080 | Production deployment |
| `docker` | PostgreSQL (host: `db`) | validate | Enabled | 8080 | Docker Compose |

## Configuration

Environment variables (with defaults):

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database JDBC URL | `jdbc:h2:mem:testdb` |
| `DB_USERNAME` | Database username | `sa` |
| `DB_PASSWORD` | Database password | _(empty)_ |
| `APP_GENERATED_PROJECTS_DIR` | Output directory for generated projects | `./generated-projects` |
| `APP_MAVEN_PATH` | Maven executable path for build verification | `mvn` |

## API Endpoints

### Project Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects` | Create a new project definition |
| GET | `/api/projects` | List all projects |
| GET | `/api/projects/{id}` | Get project by ID |
| GET | `/api/projects/{id}/details` | Get project with all entities, fields, and operations |
| PUT | `/api/projects/{id}` | Update a project |

### Entity Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects/{projectId}/entities` | Create an entity in a project |
| GET | `/api/projects/{projectId}/entities` | List entities for a project |
| GET | `/api/projects/{projectId}/entities/{id}` | Get entity by ID |
| GET | `/api/projects/{projectId}/entities/{id}/details` | Get entity with fields and operations |
| PUT | `/api/projects/{projectId}/entities/{id}` | Update an entity |
| DELETE | `/api/projects/{projectId}/entities/{id}` | Delete an entity |

### Code Generation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/generator/generate` | Generate a Spring Boot project from a project definition |

## Example Usage

### 1. Create a Project

```bash
curl -X POST http://localhost:8083/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ECommerce",
    "packageName": "com.example.ecommerce",
    "description": "Online shopping platform",
    "databaseType": "POSTGRESQL",
    "securityEnabled": false,
    "swaggerEnabled": true
  }'
```

### 2. Create an Entity

```bash
curl -X POST http://localhost:8083/api/projects/1/entities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Product",
    "description": "Product catalog entity"
  }'
```

### 3. Generate the Project

```bash
curl -X POST http://localhost:8083/api/generator/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1
  }' \
  --output ecommerce.zip
```

## Database Schema

Managed by Flyway. The V1 migration creates:

- **project_definitions** — Project configuration (name, package, DB type, security settings)
- **entity_definitions** — Entities within a project
- **field_definitions** — Fields/columns for each entity
- **operation_configs** — CRUD and advanced operations per entity

## Supported Operation Types

| Operation | Generated Code |
|-----------|---------------|
| CREATE | POST endpoint + service + repository save |
| READ | GET endpoints (by ID, list all) |
| UPDATE | PUT endpoint + service + repository update |
| DELETE | DELETE endpoint + service + repository delete |
| PAGINATION | Pageable controller + PagingAndSortingRepository |
| SEARCH | JPA Specification-based dynamic queries |
| SOFT_DELETE | Deleted flag + deletedAt + filtered queries |
| BULK_INSERT | POST /bulk endpoint |
| EXPORT_CSV | GET /export/csv |
| EXPORT_EXCEL | GET /export/excel (Apache POI) |
| EXPORT_PDF | GET /export/pdf (OpenPDF) |
| IMPORT_CSV | POST /import/csv (multipart) |
| AUDIT_LOG | Change tracking table + JPA listener |
| VERSIONING | @Version + history table |
| STATUS_TRANSITION | State machine with configurable transitions |
| WEBHOOK_INTEGRATION | Event publisher + async webhook caller |

## Project Structure (Generated Output)

When you generate a project, you get a complete Spring Boot application:

```
generated-project/
├── src/main/java/com/example/app/
│   ├── controller/      # REST controllers per entity
│   ├── dto/             # Request/Response DTOs
│   ├── model/           # JPA entity classes
│   ├── repository/      # Spring Data repositories
│   ├── service/         # Service interfaces + implementations
│   └── Application.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
└── (ready to run with `mvn spring-boot:run`)
```

## Development

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run                                    # dev (default)
mvn spring-boot:run -Dspring-boot.run.profiles=prod    # production
```

## Roadmap

- [x] Phase 1: Codebase cleanup (dead code removal, SLF4J logging, constructor injection)
- [x] Phase 2: Flyway migrations + multi-environment profiles
- [ ] Phase 3: Multi-environment configuration (@ConfigurationProperties, .env.example)
- [ ] Phase 4: Custom exception hierarchy + error handling
- [ ] Phase 5: Audit logging infrastructure
- [ ] Phase 6: JWT authentication + API key support
- [ ] Phase 7: API versioning + response envelopes + pagination
- [ ] Phase 8: Relationship model (dedicated entity + full JPA config)
- [ ] Phase 9: Relationship code generation (templates)
- [ ] Phase 10: DTO strategy generation (ID_ONLY, SUMMARY, NESTED, IGNORE)
- [ ] Phase 11: Operation completeness (all 22 operation types)
- [ ] Phase 12: Security generators for output projects (JWT, Basic, OAuth2, Session)
- [ ] Phase 13: Async generation pipeline + file cleanup
- [ ] Phase 14: Frontend generation (React)
- [ ] Phase 15: Test coverage (80%+ with JaCoCo)
- [ ] Phase 16: Documentation (Postman collection, operation docs)
- [ ] Phase 17: Dockerization

---

**Author:** Jatin Raheja  
**Java Version:** 17 LTS  
**Spring Boot:** 3.5.0  
**Last Updated:** June 2026
