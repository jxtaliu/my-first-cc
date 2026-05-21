# SME PM - Project & Time Management System

中小企业工时与项目管理工具

## Tech Stack

**Frontend:**
- Vue 3 + Vite
- Element Plus
- Pinia (State Management)
- Vue Router
- vue-i18n (Multi-language)
- 4 switchable themes

**Backend:**
- Spring Boot 3.2.5 / JDK 17
- MyBatis-Plus 3.5
- MySQL 8.0
- Redis 7
- Spring Security + JWT

## Quick Start

### Prerequisites
- Docker & Docker Compose
- JDK 17+ (for local backend development)
- Node.js 20+ (for local frontend development)

### Docker Compose (Local Development)

```bash
# Copy environment file
cp .env.example .env

# Start all services (MySQL, Redis, Backend, Frontend)
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

**访问地址:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Debug Port: localhost:5005 (attach debugger)

### Local Development (without Docker)

```bash
# Backend
cd backend
mvn spring-boot:run          # dev profile with debug enabled

# Frontend (new terminal)
cd frontend
npm install
npm run dev                   # http://localhost:3000
```

## Features

- Project Management (Scrum/Kanban)
- Task Management with hierarchy (4 levels max)
- Time Tracking with approval workflow
- 4-layer Permission System
- 4 Switchable UI Themes
- Multi-language (English/Chinese)
- Project Archive/Restore

## Project Structure

```
├── backend/
│   ├── src/main/java/com/sme/pm/
│   │   ├── entity/       # JPA Entities
│   │   ├── mapper/       # MyBatis Mappers
│   │   ├── service/      # Business Logic
│   │   ├── controller/   # REST APIs
│   │   ├── config/       # Configuration
│   │   ├── security/     # JWT & Security
│   │   └── common/       # Shared utilities
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── schema.sql
├── frontend/
│   ├── src/
│   │   ├── api/          # API modules
│   │   ├── components/    # Vue components
│   │   ├── views/        # Page views
│   │   ├── stores/       # Pinia stores
│   │   ├── router/       # Vue Router
│   │   ├── locales/      # i18n translations
│   │   └── utils/        # Utilities
│   └── Dockerfile
├── docker-compose.yml
└── .env.example
```

## CI/CD

GitHub Actions workflows:
- `ci.yml` - Lint, test, build on PR
- `deploy.yml` - Deploy to staging/production

## License

MIT
