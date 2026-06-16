<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/FreeMarker-Templates-0066CC?style=for-the-badge" />
</p>

<h1 align="center">⚡ Dynamic Backend Generator</h1>

<p align="center">
  <strong>A Spring Boot Low-Code Engine that generates complete, production-ready backend projects from simple entity definitions via REST API.</strong>
</p>

<p align="center">
  Define your entities, fields, relationships, and operations → Get a fully functional Spring Boot project as a downloadable ZIP.
</p>

---

## 🎯 What It Does

```
┌──────────────────┐         ┌──────────────────────┐         ┌─────────────────┐
│   Define your    │         │   Dynamic Backend    │         │  Download your  │
│   entities via   │ ──────► │   Generator Engine   │ ──────► │  complete app   │
│   REST API       │         │   (this project)     │         │  as ZIP         │
└──────────────────┘         └──────────────────────┘         └─────────────────┘

  POST your schema            Generates: Entity,              Ready to run with
  (name, fields,              Controller, Service,            `mvn spring-boot:run`
   operations)                Repository, DTO, Config
```

**In one API call**, you get a complete Spring Boot project with:
- ✅ JPA Entities with relationships
- ✅ REST Controllers (CRUD + advanced operations)
- ✅ Service layer with business logic
- ✅ Spring Data repositories
- ✅ DTOs and mappers
- ✅ Security configuration (JWT, Basic Auth, OAuth2, Session)
- ✅ Swagger/OpenAPI documentation
- ✅ Maven build with all dependencies
- ✅ Build-verified (compiles before delivery)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       REST API Layer                             │
│   ProjectDefinitionController │ EntityDefinitionController       │
│                    GeneratorController                           │
├─────────────────────────────────────────────────────────────────┤
│                      Service Layer                               │
│   ProjectDefinitionService │ EntityDefinitionService             │
│               ProjectGenerationService                          │
├─────────────────────────────────────────────────────────────────┤
│                    Generator Engine                              │
│   ProjectGenerator → EntityModuleGenerator                      │
│   FreemarkerTemplateEngine → SecurityGeneratorFactory            │
│   BuildVerifier (compiles output before ZIP)                    │
├─────────────────────────────────────────────────────────────────┤
│                      Data Layer                                  │
│   JPA Repositories (PostgreSQL / H2) │ Flyway Migrations        │
└─────────────────────────────────────────────────────────────────┘
```

### 📁 Package Structure

```
com.user.driven.operations/
├── app/
│   ├── api/
│   │   ├── controller/       → REST endpoints
│   │   ├── dto/              → Request/Response objects
│   │   └── mapper/           → DTO ↔ Entity mapping
│   ├── common/
│   │   ├── exception/        → Global error handling
│   │   └── util/             → Constants, utilities
│   ├── config/               → Spring configuration
│   └── core/
│       ├── model/            → JPA entities
│       ├── repository/       → Spring Data repos
│       └── service/          → Business logic
├── enums/                    → OperationType, SecurityType, etc.
└── generator/
    ├── core/                 → Template engine, build verifier
    ├── module/               → Entity code generators
    ├── orchestrator/         → Generation pipeline
    ├── project/              → POM, config, validator
    ├── security/             → JWT/OAuth2/Basic/Session generators
    └── utils/                → Naming utilities
```

---

## 🚀 Quick Start

### Development Mode (zero setup — uses H2 in-memory)

```bash
git clone https://github.com/your-repo/Dynamic-Backend-Generator.git
cd Dynamic-Backend-Generator-Spring-Boot-Low-Code-Engine

# Build and run (dev profile is default)
mvn spring-boot:run
```

🟢 **App starts at:** http://localhost:8083  
📘 **Swagger UI:** http://localhost:8083/swagger-ui.html  
🗄️ **H2 Console:** http://localhost:8083/h2-console

### Production Mode (PostgreSQL)

```bash
# Create database
psql -U postgres -c "CREATE DATABASE user_driven_operation_mng_sys;"

# Run with prod profile
export DB_URL=jdbc:postgresql://localhost:5432/user_driven_operation_mng_sys
export DB_USERNAME=postgres
export DB_PASSWORD=your_password

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

🟢 **App starts at:** http://localhost:8080 (Flyway auto-creates tables)

---

## 🔧 Environment Profiles

| Profile | Database | DDL Mode | Flyway | Port | Use Case |
|:--------|:---------|:---------|:-------|:-----|:---------|
| `dev` _(default)_ | H2 in-memory | create-drop | ❌ | 8083 | Local development |
| `prod` | PostgreSQL | validate | ✅ | 8080 | Production |
| `docker` | PostgreSQL (`db` host) | validate | ✅ | 8080 | Docker Compose |

---

## 📡 API Endpoints

### Project Management

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/api/projects` | Create a new project |
| `GET` | `/api/projects` | List all projects |
| `GET` | `/api/projects/{id}` | Get project by ID |
| `GET` | `/api/projects/{id}/details` | Get project with entities, fields & operations |
| `PUT` | `/api/projects/{id}` | Update a project |

### Entity Management

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/api/projects/{projectId}/entities` | Create entity with fields & operations |
| `GET` | `/api/projects/{projectId}/entities` | List all entities |
| `GET` | `/api/projects/{projectId}/entities/{id}` | Get entity by ID |
| `GET` | `/api/projects/{projectId}/entities/{id}/details` | Get entity with full details |
| `PUT` | `/api/projects/{projectId}/entities/{id}` | Update entity |
| `DELETE` | `/api/projects/{projectId}/entities/{id}` | Delete entity |

### Code Generation

| Method | Endpoint | Description |
|:-------|:---------|:------------|
| `POST` | `/api/generator/generate` | Generate & download complete Spring Boot project |

---

## 💡 Example: Generate a Project in One Call

```bash
curl -X POST http://localhost:8083/api/generator/generate \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ECommerceApp",
    "packageName": "com.example.ecommerce",
    "securityEnabled": true,
    "securityType": "JWT",
    "entities": [
      {
        "name": "Product",
        "fields": [
          {"name": "id", "type": "Long", "fieldType": "PRIMARY_KEY"},
          {"name": "name", "type": "String", "fieldType": "NORMAL"},
          {"name": "price", "type": "Double", "fieldType": "NORMAL"},
          {"name": "active", "type": "Boolean", "fieldType": "NORMAL"}
        ]
      },
      {
        "name": "Order",
        "fields": [
          {"name": "id", "type": "Long", "fieldType": "PRIMARY_KEY"},
          {"name": "orderDate", "type": "LocalDateTime", "fieldType": "NORMAL"},
          {"name": "total", "type": "BigDecimal", "fieldType": "NORMAL"}
        ]
      }
    ]
  }' --output ecommerce.zip
```

**Result:** A complete, compilable Spring Boot project delivered as a ZIP.

---

## 📋 Supported Operations

Every entity can have any combination of these operations:

| Category | Operations | What Gets Generated |
|:---------|:-----------|:-------------------|
| **CRUD** | `CREATE`, `READ`, `UPDATE`, `DELETE` | Standard REST endpoints |
| **Query** | `SEARCH`, `PAGINATION` | JPA Specifications, Pageable |
| **Bulk** | `BULK_INSERT`, `BULK_UPDATE`, `BULK_DELETE` | Batch endpoints |
| **Soft Delete** | `SOFT_DELETE`, `RESTORE` | Deleted flag + restore endpoint |
| **Export** | `EXPORT_CSV`, `EXPORT_EXCEL`, `EXPORT_PDF` | File download endpoints |
| **Import** | `IMPORT_CSV`, `IMPORT_EXCEL` | File upload + parsing |
| **Files** | `FILE_UPLOAD`, `FILE_DOWNLOAD` | Storage service + endpoints |
| **Audit** | `AUDIT_LOG`, `VERSIONING` | Change tracking, optimistic locking |
| **Advanced** | `STATUS_TRANSITION`, `WEBHOOK_INTEGRATION` | State machine, event hooks |

---

## 🔐 Security Types

| Type | Generated Code |
|:-----|:---------------|
| `JWT` | SecurityConfig, JwtFilter, JwtUtils, AuthController, User/Role entities, DTOs |
| `BASIC_AUTH` | SecurityConfig with HTTP Basic + BCryptPasswordEncoder |
| `OAUTH2` | SecurityConfig with OAuth2 login + resource server config |
| `SESSION_BASED` | SecurityConfig with form login + session management |

---

## 🗄️ Database Schema (Flyway-managed)

```sql
project_definitions          -- Project config (name, package, DB, security)
  └── entity_definitions     -- Entities within a project
        ├── field_definitions    -- Fields/columns per entity
        └── operation_configs    -- Operations per entity (CRUD, export, etc.)
```

---

## ⚙️ Configuration

| Variable | Description | Default |
|:---------|:------------|:--------|
| `DB_URL` | JDBC connection URL | `jdbc:h2:mem:testdb` |
| `DB_USERNAME` | Database username | `sa` |
| `DB_PASSWORD` | Database password | _(empty)_ |
| `APP_GENERATED_PROJECTS_DIR` | Output directory | `./generated-projects` |
| `APP_MAVEN_PATH` | Maven executable | `mvn` |

---

## 🛠️ Tech Stack

| Layer | Technology |
|:------|:-----------|
| Language | Java 17 LTS |
| Framework | Spring Boot 3.5.0 |
| Build | Maven 3.6+ |
| ORM | Spring Data JPA + Hibernate 6.6 |
| Database | PostgreSQL 15+ / H2 (dev) |
| Migrations | Flyway |
| Templates | FreeMarker 2.3.34 |
| API Docs | SpringDoc OpenAPI 2.8.8 |
| Utilities | Lombok, Jackson, Commons Compress |

---

## 📂 Generated Project Structure

When you generate a project, you receive:

```
your-app/
├── src/main/java/com/your/package/
│   ├── config/          → SecurityConfig, SwaggerConfig
│   ├── controller/      → REST controllers per entity + AuthController
│   ├── dto/             → Request/Response DTOs
│   ├── model/           → JPA entities (User, Role, your entities)
│   ├── repository/      → Spring Data repositories
│   ├── security/        → JWT filter, utils, entry point
│   ├── service/         → Service implementations
│   └── Application.java
├── src/main/resources/
│   └── application.properties
├── pom.xml              → All dependencies configured
└── Ready to: mvn spring-boot:run
```

---

## 🗺️ Roadmap

- [x] ~~Phase 1: Codebase cleanup~~
- [x] ~~Phase 2: Flyway migrations + multi-environment profiles~~
- [ ] Phase 3: @ConfigurationProperties + env validation
- [ ] Phase 4: Custom exception hierarchy
- [ ] Phase 5: Audit logging (DB + Logback)
- [ ] Phase 6: JWT authentication + API keys
- [ ] Phase 7: API versioning + response envelopes
- [ ] Phase 8: Relationship model (dedicated entity)
- [ ] Phase 9: Relationship code generation
- [ ] Phase 10: DTO strategies (ID_ONLY, SUMMARY, NESTED, IGNORE)
- [ ] Phase 11: All 22 operation types fully implemented
- [ ] Phase 12: Security generators (JWT, Basic, OAuth2, Session)
- [ ] Phase 13: Async generation + progress tracking
- [ ] Phase 14: React frontend generation
- [ ] Phase 15: 80%+ test coverage (JaCoCo)
- [ ] Phase 16: Postman collection + docs
- [ ] Phase 17: Docker + Kubernetes

---

## 👨‍💻 Author

**Jatin Raheja**

---

<p align="center">
  <sub>Built with ☕ Java 17 • Spring Boot 3.5 • FreeMarker Templates</sub>
</p>
