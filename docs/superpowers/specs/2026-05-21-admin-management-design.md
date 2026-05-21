# Admin Management Module Design

> **Goal:** Add admin management menu with User, Role, and Department management sub-menus.

## Navigation Menu Design

**Sidebar Structure:**
```
├── Dashboard (仪表盘)
├── Projects (项目)
├── Timesheet (工时)
└── Admin (管理) [with sub-menu]
    ├── User Management (用户管理)
    ├── Role Management (角色管理)
    └── Department Management (部门管理)
```

**Implementation:**
- Use `el-sub-menu` for collapsible sub-menu
- Click "Admin" to expand sub-menu, click sub-menu item to navigate
- Active sub-menu item highlighted

## Database Design

### New Table: sys_department

```sql
CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    leader_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Relationships:**
- User → Department: Many-to-One (user belongs to one department)
- Department → Parent Department: Self-referencing Many-to-One (tree structure)
- Department → Leader: Many-to-One (one department has one leader)

**Existing Tables Used:**
- `sys_user` - has `department_id` field, no schema change needed
- `sys_role` - existing role table
- `sys_permission` - existing permission table
- `sys_user_role` - existing user-role association
- `sys_role_permission` - existing role-permission association

## User Management Module

### Features
1. User list with pagination and search
2. Create user
3. Edit user
4. Delete user (physical delete, not soft delete)
5. Assign roles (one user can have multiple roles)
6. Assign department

### List Columns
| Field | Description |
|-------|-------------|
| Username | Unique identifier |
| Email | Contact |
| Real Name | Display name |
| Department | Affiliated department |
| Roles | User's roles (displayed as tags) |
| Status | Active/Inactive |
| Created At | |

### Create/Edit Dialog Fields
- Username (required on create, immutable on edit)
- Email
- Real Name
- Password (required on create, optional on edit)
- Department (tree-select)
- Roles (multi-select)
- Status (Active/Inactive)

## Role Management Module

### Features
1. Role list
2. Create role
3. Edit role
4. Delete role (must verify no users are assigned)
5. Assign permissions (multi-select, grouped by module)

### List Columns
| Field | Description |
|-------|-------------|
| Role Code | Unique identifier, e.g. SUPER_ADMIN |
| Role Name | Display name |
| Description | Description |
| Status | Active/Inactive |
| Created At | |

### Create/Edit Dialog Fields
- Role Code (required on create, immutable on edit, unique)
- Role Name
- Description
- Status
- Permissions (multi-select, grouped by module)

### Permission UI
- Grouped by module (User, Role, Project, Timesheet, etc.)
- Each module shows its available permissions

## Department Management Module

### Features
1. Department list (tree display)
2. Create department
3. Edit department
4. Delete department (must verify no child departments or users)
5. Drag to reorder/adjust hierarchy

### List Display
- Tree structure showing parent-child relationships
- Supports expand/collapse
- Shows department name, leader, status, child count

### Create/Edit Dialog Fields
- Department Name (required)
- Parent Department (tree-select, empty for top-level)
- Leader (select from user list)
- Sort Order (number, smaller = higher priority)
- Status (Active/Inactive)

### Operation Constraints
- Cannot delete department with child departments → prompt to delete children first
- Cannot delete department with users → prompt to transfer users first

## API Endpoints

### User API
- `GET /api/users` - List users (pagination, search)
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user (physical)
- `PUT /api/users/{id}/roles` - Assign roles to user
- `PUT /api/users/{id}/department` - Assign department to user

### Role API
- `GET /api/roles` - List roles
- `GET /api/roles/{id}` - Get role by ID
- `POST /api/roles` - Create role
- `PUT /api/roles/{id}` - Update role
- `DELETE /api/roles/{id}` - Delete role
- `GET /api/roles/{id}/permissions` - Get role's permissions
- `PUT /api/roles/{id}/permissions` - Assign permissions to role
- `GET /api/permissions` - List all permissions (grouped by module)

### Department API
- `GET /api/departments` - List departments (tree structure)
- `GET /api/departments/{id}` - Get department by ID
- `POST /api/departments` - Create department
- `PUT /api/departments/{id}` - Update department
- `DELETE /api/departments/{id}` - Delete department

## Tech Stack
- Backend: Spring Boot, MyBatis-Plus
- Frontend: Vue 3, Element Plus (el-tree, el-table, el-dialog)
- Database: MySQL 8

## Implementation Order
1. Backend: Department entity, mapper, service, controller
2. Backend: Role entity, mapper, service, controller (with permission assignment)
3. Backend: User entity, mapper, service, controller (with role/department assignment)
4. Frontend: Layout with sub-menu
5. Frontend: Department management page
6. Frontend: Role management page
7. Frontend: User management page
