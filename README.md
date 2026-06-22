# Dynamic Backend Generator — Spring Boot Low-Code Engine

A production-ready low-code engine that generates complete Spring Boot applications (with optional React frontends) from a JSON schema definition. Define your entities, relationships, operations, and security — get a fully functional, compilable project in seconds.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    API Layer                         │
│  AuthController │ ProjectController │ GeneratorCtrl  │
├─────────────────────────────────────────────────────┤
│                  Service Layer                       │
│  EntityService │ ProjectGenerationService │ JwtSvc   │
├─────────────────────────────────────────────────────┤
│               Generator Pipeline                     │
│  ProjectGenerator → EntityModule → Security → Frontend│
│  TemplateEngine (FreeMarker) → FileWriter → BuildVerifier│
├─────────────────────────────────────────────────────┤
│                 Data Layer                           │
│  JPA Repositories │ Flyway Migrations │ H2/PostgreSQL│
└─────────────────────────────────────────────────────┘
```

### Package Structure

```
com.user.driven.operations
├── app
│   ├── api          # REST controllers, DTOs, mappers
│   ├── common       # Exceptions, utilities, constants
│   ├── config       # Security, async, CORS, properties
│   └── core         # Models, repositories, services
├── enums            # OperationType, RelationshipType, SecurityType, etc.
└── generator
    ├── core         # TemplateEngine, FileWriter, BuildVerifier
    ├── frontend     # React frontend generator
    ├── module       # Entity + relationship + DTO generators
    ├── orchestrator # ProjectGenerator (pipeline coordinator)
    ├── project      # Pom, Application, Structure generators
    ├── security     # JWT, Basic, OAuth2, Session generators
    └── utils        # NamingUtils, RelationshipGeneratorUtil
```

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.5.0 |
| Language | Java | 17 |
| Build | Maven | 3.9+ |
| Database (prod) | PostgreSQL | 15+ |
| Database (dev) | H2 | In-memory |
| Migrations | Flyway | 10+ |
| Templates | FreeMarker | 2.3.32 |
| Auth | JWT (jjwt) | 0.12.6 |
| Docs | SpringDoc OpenAPI | 2.8.8 |
| Testing | JUnit 5 + Mockito | via Spring Boot |
| Coverage | JaCoCo | 0.8.11 |

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15+ (production) or H2 (dev, auto-configured)

## Quick Start

### Development (H2 in-memory)

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8083` with H2 console at `/h2-console`.

### Production (PostgreSQL)

```bash
export DB_URL=jdbc:postgresql://localhost:5432/generator_db
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-base64-encoded-256-bit-key

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Docker

```bash
docker-compose up
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login, get JWT tokens |

### Projects

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/projects` | List projects (paginated, filterable) |
| POST | `/api/v1/projects` | Create project |
| GET | `/api/v1/projects/{id}` | Get project by ID |
| PUT | `/api/v1/projects/{id}` | Update project |
| DELETE | `/api/v1/projects/{id}` | Delete project |

### Entities

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/projects/{id}/entities` | List entities |
| POST | `/api/v1/projects/{id}/entities` | Create entity |
| PUT | `/api/v1/entities/{id}` | Update entity |
| DELETE | `/api/v1/entities/{id}` | Delete entity |

### Generation

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/generator/generate` | Synchronous generation (returns ZIP) |
| POST | `/api/v1/generator/async/generate` | Async generation (returns job ID) |
| GET | `/api/v1/generator/status/{jobId}` | Poll job status |
| GET | `/api/v1/generator/download/{jobId}` | Download completed ZIP |

### Documentation

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Interactive API docs |
| `/api-docs` | OpenAPI 3.0 JSON spec |

## Database Setup

### PostgreSQL

```sql
CREATE DATABASE generator_db;
CREATE USER generator_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE generator_db TO generator_user;
```

### H2 (Development)

No setup needed — auto-configured with `spring.profiles.active=dev`.

## Environment Variables

See `.env.example` for the complete list. Key variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database JDBC URL | H2 in-memory (dev) |
| `DB_USERNAME` | Database username | sa (dev) |
| `DB_PASSWORD` | Database password | empty (dev) |
| `JWT_SECRET` | Base64 JWT signing key | dev key |
| `APP_GENERATED_PROJECTS_DIR` | Output directory | ./generated-projects |
| `APP_MAVEN_PATH` | Maven executable path | mvn |

## Running Tests

```bash
mvn test
```

Coverage report generated at `target/site/jacoco/index.html`.

## License

MIT
