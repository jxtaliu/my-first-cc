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
