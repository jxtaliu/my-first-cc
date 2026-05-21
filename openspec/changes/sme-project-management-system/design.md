## Context

中小企业技术团队需要一套完整的项目管理和工时统计系统。目标用户为50人以上的软件团队，采用灵活的项目组队方式，同时支持内部研发和客户项目两种结算模式。

当前无统一工具，依赖 Excel 或口头同步，导致工时统计困难、项目进度不透明、资源分配不均。

## Goals / Non-Goals

**Goals:**
- 提供完整的项目管理功能，支持 Scrum Sprint 和 Kanban 两种模式
- 实现灵活的层级任务结构，团队可自定义
- 提供工时填报和统计报表功能
- 建立四层权限体系，支持角色叠加
- 预留多语言和多种部署方式扩展能力

**Non-Goals:**
- 不实现完整的财务管理（预算、成本核算）
- 不实现完整的 CRM 功能
- 不实现 DevOps 集成（如 CI/CD 触发）
- 不实现即时通讯功能

## Decisions

### 1. Monolithic Architecture (前后端分离)

**Decision:** 采用前后端分离的单体架构，而非微服务

**Rationale:**
- 团队规模50+人，尚不需要微服务的复杂度
- 单一代码库便于管理和 CI/CD
- 第一阶段 MVP 需要快速交付

**Alternatives Considered:**
- 微服务架构：过早复杂，增加部署和运维成本

### 2. Spring Boot 3.x + MyBatis-Plus

**Decision:** 后端使用 Spring Boot 3.x 和 MyBatis-Plus ORM

**Rationale:**
- Spring Boot 是 Java 企业级开发标准
- MyBatis-Plus 提供便捷的 CRUD 和分页
- 与 MySQL 配合良好

**Alternatives Considered:**
- Spring Data JPA：复杂查询不如 MyBatis-Plus 直观
- 纯 MyBatis：需要手写大量 SQL

### 3. JWT for Authentication

**Decision:** 使用 JWT 实现无状态认证

**Rationale:**
- 无状态，水平扩展方便
- 适合前后端分离架构
- Redis 可用于 token 黑名单

**Alternatives Considered:**
- Session：不适合前后端分离，需要分布式 session
- OAuth2：过度设计，本系统不需要第三方授权

### 4. Element Plus UI Framework

**Decision:** 前端使用 Element Plus 作为 UI 组件库

**Rationale:**
- Vue3 官方推荐
- 组件丰富，中文文档完善
- 学习曲线平缓

**Alternatives Considered:**
- Ant Design Vue：风格偏后台，定制化需要更多工作
- Naive UI：较新，社区生态不如 Element Plus

### 5. MySQL Database

**Decision:** 使用 MySQL 8.x 作为主数据库

**Rationale:**
- 关系型数据适合项目/任务/工时模型
- 事务支持完善
- 团队已有 MySQL 使用经验

**Alternatives Considered:**
- PostgreSQL：功能更强大，但团队更熟悉 MySQL
- MongoDB：不适合强关系数据模型

### 6. Redis for Caching

**Decision:** 使用 Redis 作为缓存层

**Rationale:**
- 存储 JWT token 黑名单
- 缓存高频访问数据（如用户权限）
- 提升系统响应速度

**Alternatives Considered:**
- 不使用缓存：数据频繁查询影响性能
- Ehcache：本地缓存，不支持分布式

## Data Model

### Entity Relationship

```
User (1)──(N) UserRole (N)──(1) Role
User (1)──(N) Timesheet
User (1)──(N) Task (as assignee)
Project (1)──(N) Sprint
Project (1)──(N) ProjectMember (N)──(1) User
Sprint (1)──(N) Task
Task (1)──(N) Task (self-referential for hierarchy)
Task (1)──(N) Timesheet
```

### Key Tables

| Table | Purpose |
|-------|---------|
| sys_user | 用户信息 |
| sys_role | 角色定义 |
| sys_permission | 权限定义 |
| sys_user_role | 用户角色关联 |
| sys_role_permission | 角色权限关联 |
| project | 项目信息（含类型：internal/external） |
| project_member | 项目成员关联 |
| sprint | Sprint/迭代信息 |
| task | 任务信息（含层级，最大4层） |
| timesheet | 工时记录（含审批状态） |

## API Design

### Authentication Flow

```
POST /api/auth/login
  Request: { username, password }
  Response: { token, user }

POST /api/auth/register (admin only)
  Request: { username, password, email }
  Response: { userId }
```

### RESTful Conventions

- `GET /api/projects` - 列表
- `GET /api/projects/{id}` - 详情
- `POST /api/projects` - 创建
- `PUT /api/projects/{id}` - 更新
- `DELETE /api/projects/{id}` - 删除

### Response Format

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### Error Response

```json
{
  "code": 400,
  "message": "Validation failed",
  "errors": [
    { "field": "username", "message": "Username already exists" }
  ]
}
```

## Frontend Structure

### Route Structure

```
/login
/register
/dashboard
/projects
/projects/:id
/projects/:id/board
/projects/:id/sprints
/projects/:id/sprints/:sid
/projects/:id/tasks/:tid
/timesheet
/timesheet/week
/timesheet/month
/admin/users
/admin/roles
```

### State Management (Pinia)

```
stores/
├── auth.js        # 用户认证状态
├── user.js        # 用户信息
├── project.js     # 项目列表
├── task.js        # 任务状态
└── timesheet.js   # 工时状态
```

## Security

### Permission Check Flow

```
1. User logs in → receives JWT
2. Frontend sends JWT in Authorization header
3. Backend validates JWT → extracts userId
4. Backend queries user roles and permissions
5. Permission interceptor checks if allowed
6. If allowed → proceed, else → 403
```

### Password Storage

- 使用 BCrypt 加密存储
- 永不明文存储或传输密码

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 权限粒度不够细致 | 第一阶段聚焦四层角色，后续可扩展 |
| 大团队性能问题 | Redis 缓存 + 数据库索引优化 |
| 多语言实现复杂度 | 预留 i18n 框架，先做中英双语 |
| 任务层级灵活导致查询复杂 | MyBatis-Plus 提供递归查询支持 |

## Open Questions

1. 任务层级最大深度是否需要限制？
   - **Answer:** 最多 4 层（Epic → Feature → Story → Sub-task）

2. 工时填报是否需要审批流程？
   - **Answer:** 部分审批 - 客户项目需要审批，内部项目直接生效

3. 项目是否需要归档功能？
   - **Answer:** 需要，支持归档和恢复

4. 报表导出格式（Excel/PDF）？
   - **Answer:** 需要同时支持 Excel 和 PDF
