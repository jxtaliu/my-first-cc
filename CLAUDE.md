# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SME PM is a中小企业工时与项目管理工具 (SME Project & Time Management System) for small/medium enterprises. It supports project management (Scrum/Kanban), task hierarchies (4 levels max), time tracking with approval workflows, and role-based permissions.

## Tech Stack

**Backend:** Spring Boot 3.2.5, JDK 17, MyBatis-Plus 3.5, MySQL 8.0, Redis 7, Spring Security + JWT
**Frontend:** Vue 3 + Vite, Element Plus, Pinia, Vue Router, vue-i18n
**Infrastructure:** Docker Compose, GitHub Actions CI/CD

## Commands

### Backend
```bash
cd backend
mvn spring-boot:run          # Run development server (port 8080)
mvn verify                    # Run tests and verify
mvn test                      # Unit tests only
mvn -B verify -DskipITs       # CI verify (skip integration tests)
```

### Frontend
```bash
cd frontend
npm install                   # Install dependencies
npm run dev                   # Development server (port 3000)
npm run build                 # Production build
npm run lint                  # ESLint check
npm run preview                # Preview production build
npm test                      # Run unit tests
npm run test:watch            # Run tests in watch mode
```

### Docker (Full Stack)
```bash
cp .env.example .env          # Configure environment
docker compose up -d          # Start all services (MySQL, Redis, backend, frontend)
docker compose down           # Stop all services
```

## Architecture

### Backend (Spring Boot)
- `src/main/java/com/sme/pm/`
  - `entity/` - JPA entities with MyBatis-Plus (User, Project, Task, Sprint, Timesheet, Role, Permission)
  - `mapper/` - MyBatis-Plus mappers
  - `service/` - Business logic layer with impl/ subdirectory
  - `controller/` - REST API endpoints (Auth, User, Project, Sprint, Task, Timesheet)
  - `security/` - JWT authentication filter, token provider, SecurityConfig, UserDetailsServiceImpl
  - `common/` - Result wrapper, global exception handler, ResultCode enum, CurrentUser annotation
  - `config/` - MyBatis-Plus config, Redis config
  - `dto/` - LoginRequest, LoginResponse

Database uses soft deletes (`deleted` column) and automatic timestamp management. MyBatis-Plus handles logic delete via `logic-delete-field`.

### Frontend (Vue 3)
- `src/views/` - Page components organized by domain: `admin/`, `auth/`, `dashboard/`, `project/`, `timesheet/`
- `src/api/` - Axios-based API modules (project, task, timesheet, request)
- `src/stores/` - Pinia stores for auth and theme state
- `src/router/` - Vue Router with auth guards
- `src/locales/` - i18n translation files (en-US, zh-CN)
- `src/components/` - Layout.vue, ThemeSwitcher.vue
- `src/utils/` - Utility functions

### API Design
- RESTful APIs with `/api/v1/` prefix
- Authentication via `Authorization: Bearer <jwt_token>` header
- Standard response wrapper: `Result<T>` with `code`, `message`, `data`
- Swagger UI available at `/swagger-ui.html` (dev profile)

### Database Schema
Key tables: `sys_user`, `sys_role`, `sys_permission`, `sys_user_role`, `sys_role_permission`, `project`, `project_member`, `sprint`, `task` (hierarchical), `timesheet`

### Permission System
4-layer role system: Super Admin > Department Admin > Project Admin > Member
- Role stacking supported (users can have multiple roles)
- Project-level role assignment via `project_member` table

### Theme System
4 switchable CSS variable-based themes stored in `src/stores/theme.js`:
- Professional Minimal, Modern Tech, Vibrant, Dark Luxe

### OpenSpec Workflow
This project uses OpenSpec for change management. Use the `/opsx:propose`, `/opsx:explore`, `/opsx:apply` commands to create and implement changes.

## Configuration

- Backend config: `backend/src/main/resources/application.yml`
- Frontend config: `frontend/vite.config.js`, `frontend/src/main.js`
- Docker: `docker-compose.yml` with MySQL, Redis, backend, frontend services
- Environment: `.env.example` template for Docker deployment

## CI/CD

GitHub Actions (`.github/workflows/`):
- `ci.yml` - Runs Maven verify + npm lint/test on PRs to main/develop
- `deploy.yml` - Builds and pushes Docker images on push to main/develop

Dockerfiles: `backend/Dockerfile` (multi-stage), `frontend/Dockerfile` (Node + Nginx)

## Testing Requirements

### Mandatory Rule
**Every new feature must include complete test cases before being considered done.** No feature is complete without tests.

### Test Structure

**Frontend Tests (Vitest)**
- Location: `frontend/src/**/*.spec.js` or `frontend/src/**/*.test.js`
- Components: `frontend/src/components/__tests__/`
- Run: `npm test` or `npm run test:watch`

**Backend Tests (JUnit 5)**
- Location: `backend/src/test/java/com/sme/pm/**/*.java`
- Run: `mvn test`

### Test Principles (FIRST)

| Principle | Description |
|-----------|-------------|
| **Fast** | Unit tests should run in milliseconds |
| **Independent** | Tests have no dependencies on each other |
| **Repeatable** | Same results every run, no random data |
| **Self-Validating** | Automated assertions, no manual checking |
| **Timely** | Write tests alongside code (TDD approach) |

### What to Test

**✅ Always test:**
- Core business logic in services
- Component rendering and user interactions
- API endpoints (controller tests)
- Edge cases: empty inputs, null values, boundary values
- Error handling paths

**❌ Don't test:**
- Third-party libraries
- Framework internals
- Trivial getters/setters (unless they contain logic)

### Test Naming Convention

```javascript
// Frontend: descriptive name explaining the scenario
describe('ProjectList', () => {
  it('should display empty state when no projects exist')
  it('should filter projects by active status')
})

// Backend: methodName_should_expectedBehavior
@Test
void createProject_should_returnCreatedProject_whenValidInput()
```

### Test Code Style

Use **AAA pattern** (Arrange-Act-Assert):

```javascript
// Frontend example
it('should create project successfully', async () => {
  // Arrange
  const projectData = { name: 'Test Project', type: 'SCRUM' }

  // Act
  const result = await createProject(projectData)

  // Assert
  expect(result.id).toBeDefined()
  expect(result.name).toBe('Test Project')
})
```

```java
// Backend example
@Test
void createProject_should_returnCreatedProject_whenValidInput() {
    // Arrange
    ProjectRequest request = new ProjectRequest();
    request.setName("Test Project");

    // Act
    Project result = projectService.createProject(request);

    // Assert
    assertNotNull(result.getId());
    assertEquals("Test Project", result.getName());
}
```

### Coverage Guidelines

- Target **70-80%** code coverage for core modules
- 100% coverage is not the goal—meaningful tests are
- Focus on **business-critical paths** and **edge cases**
- Coverage reports are indicators, not targets

### Mocking Guidelines

- Mock external dependencies (API calls, database in unit tests)
- Use real instances for integration tests
- Avoid over-mocking (test the actual behavior, not mocks)

### When to Run Tests

1. **Before commit** - All tests must pass
2. **In CI pipeline** - Automatic on every PR
3. **During code review** - Review test quality alongside code
