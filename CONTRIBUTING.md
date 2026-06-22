# Contributing to Dynamic Backend Generator

Thank you for considering contributing! This guide covers code style, branch naming, PR process, and testing requirements.

## Development Setup

1. Clone the repository
2. Ensure Java 17+ and Maven 3.9+ are installed
3. Run `mvn compile` to verify setup
4. Run `mvn test` to confirm all tests pass

## Code Style

- **Java 17** features are encouraged (records, switch expressions, text blocks)
- **Lombok** for boilerplate reduction (`@Getter`, `@Setter`, `@Slf4j`, `@RequiredArgsConstructor`)
- **Constructor injection** over field injection (no `@Autowired` on fields)
- **SLF4J logging** with `{}` placeholders (no string concatenation)
- Javadoc on all public classes and methods
- 4-space indentation, no tabs

## Branch Naming

```
phase-<description>-<phase-number>
feature/<short-description>
bugfix/<issue-description>
```

Examples:
- `phase-relationship-code-generation-11`
- `feature/add-graphql-support`
- `bugfix/fix-cascade-deduplication`

## Pull Request Process

1. Create a branch from the latest `main`
2. Implement your changes
3. Run `mvn test` — all tests must pass
4. Run `mvn compile` on a generated project to verify templates
5. Write tests for new functionality
6. Create PR with:
   - Summary of changes
   - What was tested
   - Any breaking changes or migration notes

## Testing Requirements

- All new service methods need unit tests with mocked repositories
- Template changes need `FreemarkerTemplateEngineTest` coverage
- New generators need at least one integration test verifying output compiles
- Target: 80%+ line coverage (excluding config/DTO/enum/model classes)

## Commit Messages

Follow conventional commits:

```
feat: add OAuth2 security generator
fix: resolve swagger @Tag import collision
test: add relationship code generator tests
docs: update README with API endpoint table
```

## Architecture Decisions

- **FreeMarker templates** for code generation (over string builders)
- **Orchestrator pattern** for pipeline coordination (`ProjectGenerator`)
- **Strategy pattern** for DTO strategies and security generators
- **Async execution** for generation jobs (Spring `@Async` + thread pool)
- **Flyway** for production migrations, Hibernate `create-drop` for dev

## Questions?

Open a GitHub issue or discussion for questions about the architecture or implementation.
