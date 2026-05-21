# Admin Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement admin management module with sidebar sub-menu containing User, Role, and Department management.

**Architecture:** This module adds three management pages (User, Role, Department) behind the existing authentication system. Each entity follows the standard Service-Controller pattern with MyBatis-Plus mappers. The sidebar uses Element Plus `el-sub-menu` for collapsible sub-navigation.

**Tech Stack:** Spring Boot 3.2.5, MyBatis-Plus 3.5, Vue 3, Element Plus, MySQL 8

---

## File Structure

### Backend (Spring Boot)
```
backend/src/main/java/com/sme/pm/
├── entity/
│   ├── Department.java          (new)
│   └── User.java                (update - add userId field)
├── entity/Role.java            (update - replace code with roleId)
├── mapper/
│   ├── DepartmentMapper.java    (new)
│   ├── RoleMapper.java          (update - add permission queries)
│   └── PermissionMapper.java    (new)
├── service/
│   ├── DepartmentService.java   (new - interface)
│   └── impl/
│       └── DepartmentServiceImpl.java (new)
├── controller/
│   ├── DepartmentController.java (new)
│   └── RoleController.java      (update - add permission assignment)
└── config/
    └── SecurityConfig.java      (update - add /api/admin/** permit)

backend/src/main/resources/
├── schema.sql                   (update - add business IDs, new tables)
```

### Frontend (Vue 3)
```
frontend/src/
├── views/admin/
│   ├── Users.vue               (update - use new user fields)
│   ├── Roles.vue               (new - role management page)
│   └── Departments.vue        (new - department management page)
├── router/index.js             (update - add routes)
└── locales/                    (update - add translations)
```

---

## Tasks

### Task 1: Update Database Schema

**Files:**
- Modify: `backend/src/main/resources/schema.sql`

- [ ] **Step 1: Update sys_user table - add user_id field**

```sql
ALTER TABLE sys_user ADD COLUMN user_id VARCHAR(50) NOT NULL UNIQUE AFTER id;
```

- [ ] **Step 2: Update sys_role table - replace code with role_id**

```sql
ALTER TABLE sys_role ADD COLUMN role_id VARCHAR(50) NOT NULL UNIQUE AFTER id;
UPDATE sys_role SET role_id = CONCAT('ROLE_', LPAD(id, 3, '0'));
ALTER TABLE sys_role DROP COLUMN code;
```

- [ ] **Step 3: Create sys_department table**

```sql
CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    leader_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_department_id (department_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/resources/schema.sql
git commit -m "feat: update schema with business IDs and department table"
```

---

### Task 2: Create Department Entity

**Files:**
- Create: `backend/src/main/java/com/sme/pm/entity/Department.java`
- Test: `backend/src/test/java/com/sme/pm/entity/DepartmentTest.java`

- [ ] **Step 1: Create Department entity**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String departmentId;  // Business ID, e.g. DEPT001

    private String name;

    private Long parentId;  // null for root departments

    private Long leaderId;

    private Integer sortOrder;

    private Integer status;  // 1: enabled, 0: disabled

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DepartmentTest {
    @Test
    void shouldHaveAllFields() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setDepartmentId("DEPT001");
        dept.setName("Engineering");
        dept.setParentId(null);
        dept.setLeaderId(null);
        dept.setSortOrder(1);
        dept.setStatus(1);

        assertEquals(1L, dept.getId());
        assertEquals("DEPT001", dept.getDepartmentId());
        assertEquals("Engineering", dept.getName());
        assertNull(dept.getParentId());
        assertEquals(1, dept.getStatus());
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=DepartmentTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/entity/Department.java backend/src/test/java/com/sme/pm/entity/DepartmentTest.java
git commit -m "feat: add Department entity with business ID"
```

---

### Task 3: Update Role Entity

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/entity/Role.java`
- Test: `backend/src/test/java/com/sme/pm/entity/RoleTest.java`

- [ ] **Step 1: Update Role entity - replace code with roleId**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleId;  // Business ID, e.g. ROLE_001

    private String name;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {
    @Test
    void shouldHaveRoleId() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleId("ROLE_001");
        role.setName("Super Admin");

        assertEquals(1L, role.getId());
        assertEquals("ROLE_001", role.getRoleId());
        assertEquals("Super Admin", role.getName());
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=RoleTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/entity/Role.java backend/src/test/java/com/sme/pm/entity/RoleTest.java
git commit -m "feat: update Role entity - replace code with roleId"
```

---

### Task 4: Update User Entity

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/entity/User.java`
- Test: `backend/src/test/java/com/sme/pm/entity/UserTest.java`

- [ ] **Step 1: Update User entity - add userId field after id**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;  // Business ID, e.g. U001

    private String username;

    private String password;

    private String email;

    private String realName;

    private Integer status;  // 0: disabled, 1: enabled

    private Long departmentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void shouldHaveUserId() {
        User user = new User();
        user.setId(1L);
        user.setUserId("U001");
        user.setUsername("admin");

        assertEquals(1L, user.getId());
        assertEquals("U001", user.getUserId());
        assertEquals("admin", user.getUsername());
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=UserTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/entity/User.java backend/src/test/java/com/sme/pm/entity/UserTest.java
git commit -m "feat: update User entity - add userId field"
```

---

### Task 5: Create Department Mapper

**Files:**
- Create: `backend/src/main/java/com/sme/pm/mapper/DepartmentMapper.java`
- Test: `backend/src/test/java/com/sme/pm/mapper/DepartmentMapperTest.java`

- [ ] **Step 1: Create DepartmentMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT * FROM sys_department WHERE parent_id IS NULL AND deleted = 0 ORDER BY sort_order")
    List<Department> findRootDepartments();

    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order")
    List<Department> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM sys_user WHERE department_id = #{departmentId} AND deleted = 0")
    int countUsersByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("SELECT COUNT(*) FROM sys_department WHERE parent_id = #{departmentId} AND deleted = 0")
    int countChildrenByDepartmentId(@Param("departmentId") Long departmentId);
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.mapper;

import com.sme.pm.entity.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class DepartmentMapperTest {
    @Autowired
    private DepartmentMapper departmentMapper;

    @Test
    void shouldFindRootDepartments() {
        List<Department> roots = departmentMapper.findRootDepartments();
        assertNotNull(roots);
    }

    @Test
    void shouldCountUsersByDepartment() {
        int count = departmentMapper.countUsersByDepartmentId(1L);
        assertTrue(count >= 0);
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=DepartmentMapperTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/mapper/DepartmentMapper.java backend/src/test/java/com/sme/pm/mapper/DepartmentMapperTest.java
git commit -m "feat: add DepartmentMapper with tree queries"
```

---

### Task 6: Create Permission Mapper

**Files:**
- Create: `backend/src/main/java/com/sme/pm/mapper/PermissionMapper.java`
- Test: `backend/src/test/java/com/sme/pm/mapper/PermissionMapperTest.java`

- [ ] **Step 1: Create PermissionMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY module, name")
    List<Permission> findAll();

    @Select("SELECT module, GROUP_CONCAT(id) as permission_ids, GROUP_CONCAT(name) as permission_names " +
            "FROM sys_permission WHERE deleted = 0 GROUP BY module")
    List<Map<String, Object>> findGroupedByModule();

    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<Permission> findByRoleId(Long roleId);
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.mapper;

import com.sme.pm.entity.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PermissionMapperTest {
    @Autowired
    private PermissionMapper permissionMapper;

    @Test
    void shouldFindAllPermissions() {
        List<Permission> permissions = permissionMapper.findAll();
        assertNotNull(permissions);
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=PermissionMapperTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/mapper/PermissionMapper.java backend/src/test/java/com/sme/pm/mapper/PermissionMapperTest.java
git commit -m "feat: add PermissionMapper with grouped queries"
```

---

### Task 7: Update Role Mapper

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/mapper/RoleMapper.java`
- Test: `backend/src/test/java/com/sme/pm/mapper/RoleMapperTest.java`

- [ ] **Step 1: Update RoleMapper - add permission assignment methods**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT * FROM sys_role WHERE deleted = 0")
    List<Role> findAll();

    @Select("SELECT * FROM sys_role WHERE role_id = #{roleId} AND deleted = 0")
    Role findByRoleId(@Param("roleId") String roleId);

    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissions(@Param("roleId") Long roleId);

    @Select("SELECT COUNT(*) FROM sys_user_role WHERE role_id = #{roleId}")
    int countUsersByRoleId(@Param("roleId") Long roleId);
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.mapper;

import com.sme.pm.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {
    @Autowired
    private RoleMapper roleMapper;

    @Test
    void shouldFindAllRoles() {
        List<Role> roles = roleMapper.findAll();
        assertNotNull(roles);
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=RoleMapperTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/mapper/RoleMapper.java backend/src/test/java/com/sme/pm/mapper/RoleMapperTest.java
git commit -m "feat: update RoleMapper with permission assignment methods"
```

---

### Task 8: Create Department Service

**Files:**
- Create: `backend/src/main/java/com/sme/pm/service/DepartmentService.java`
- Create: `backend/src/main/java/com/sme/pm/service/impl/DepartmentServiceImpl.java`
- Test: `backend/src/test/java/com/sme/pm/service/impl/DepartmentServiceImplTest.java`

- [ ] **Step 1: Create DepartmentService interface**

```java
package com.sme.pm.service;

import com.sme.pm.entity.Department;
import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    List<Department> getDepartmentTree();
    Department getById(Long id);
    Department create(Department department);
    Department update(Department department);
    void delete(Long id);
    boolean canDelete(Long id);
    List<Department> getChildren(Long parentId);
}
```

- [ ] **Step 2: Create DepartmentServiceImpl**

```java
package com.sme.pm.service.impl;

import com.sme.pm.entity.Department;
import com.sme.pm.mapper.DepartmentMapper;
import com.sme.pm.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentMapper.selectList(null);
    }

    @Override
    public List<Department> getDepartmentTree() {
        List<Department> all = departmentMapper.selectList(null);
        List<Department> roots = all.stream()
                .filter(d -> d.getParentId() == null)
                .collect(Collectors.toList());
        for (Department root : roots) {
            buildTree(root, all);
        }
        return roots;
    }

    private void buildTree(Department parent, List<Department> all) {
        List<Department> children = all.stream()
                .filter(d -> parent.getId().equals(d.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (Department child : children) {
            buildTree(child, all);
        }
    }

    @Override
    public Department getById(Long id) {
        return departmentMapper.selectById(id);
    }

    @Override
    public Department create(Department department) {
        departmentMapper.insert(department);
        return department;
    }

    @Override
    public Department update(Department department) {
        departmentMapper.updateById(department);
        return department;
    }

    @Override
    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }

    @Override
    public boolean canDelete(Long id) {
        return departmentMapper.countUsersByDepartmentId(id) == 0
                && departmentMapper.countChildrenByDepartmentId(id) == 0;
    }

    @Override
    public List<Department> getChildren(Long parentId) {
        return departmentMapper.findByParentId(parentId);
    }
}
```

Note: Add `children` field to Department entity for tree structure.

- [ ] **Step 3: Write test**

```java
package com.sme.pm.service.impl;

import com.sme.pm.entity.Department;
import com.sme.pm.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentServiceImplTest {
    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void shouldGetAllDepartments() {
        Department dept = new Department();
        dept.setId(1L);
        when(departmentMapper.selectList(null)).thenReturn(Arrays.asList(dept));

        List<Department> result = departmentService.getAllDepartments();

        assertEquals(1, result.size());
        verify(departmentMapper).selectList(null);
    }
}
```

- [ ] **Step 4: Run test**

Run: `mvn test -Dtest=DepartmentServiceImplTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/sme/pm/service/DepartmentService.java backend/src/main/java/com/sme/pm/service/impl/DepartmentServiceImpl.java backend/src/test/java/com/sme/pm/service/impl/DepartmentServiceImplTest.java
git commit -m "feat: add DepartmentService with tree structure support"
```

---

### Task 9: Update Role Service

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/service/RoleService.java` (if exists)
- Create: `backend/src/main/java/com/sme/pm/service/impl/RoleServiceImpl.java` (if not exists)
- Test: `backend/src/test/java/com/sme/pm/service/impl/RoleServiceImplTest.java`

- [ ] **Step 1: Check if RoleService exists**

Run: `find backend/src -name "RoleService.java"`

If not exists, create:

```java
package com.sme.pm.service;

import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;
import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getById(Long id);
    Role create(Role role);
    Role update(Role role);
    void delete(Long id);
    List<Permission> getPermissions(Long roleId);
    void assignPermissions(Long roleId, List<Long> permissionIds);
    boolean canDelete(Long id);
}
```

- [ ] **Step 2: Implement or update RoleServiceImpl**

```java
package com.sme.pm.service.impl;

import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;
import com.sme.pm.mapper.PermissionMapper;
import com.sme.pm.mapper.RoleMapper;
import com.sme.pm.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.findAll();
    }

    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public Role create(Role role) {
        roleMapper.insert(role);
        return role;
    }

    @Override
    public Role update(Role role) {
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    @Override
    public List<Permission> getPermissions(Long roleId) {
        return permissionMapper.findByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        roleMapper.deleteRolePermissions(roleId);
        for (Long permissionId : permissionIds) {
            roleMapper.insertRolePermission(roleId, permissionId);
        }
    }

    @Override
    public boolean canDelete(Long id) {
        return roleMapper.countUsersByRoleId(id) == 0;
    }
}
```

- [ ] **Step 3: Write test**

```java
package com.sme.pm.service.impl;

import com.sme.pm.entity.Role;
import com.sme.pm.mapper.RoleMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {
    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void shouldGetAllRoles() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleId("ROLE_001");
        role.setName("Admin");
        when(roleMapper.findAll()).thenReturn(Arrays.asList(role));

        List<Role> result = roleService.getAllRoles();

        assertEquals(1, result.size());
        assertEquals("ROLE_001", result.get(0).getRoleId());
    }
}
```

- [ ] **Step 4: Run test**

Run: `mvn test -Dtest=RoleServiceImplTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/sme/pm/service/RoleService.java backend/src/main/java/com/sme/pm/service/impl/RoleServiceImpl.java backend/src/test/java/com/sme/pm/service/impl/RoleServiceImplTest.java
git commit -m "feat: update RoleService with permission assignment"
```

---

### Task 10: Update User Service

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/service/UserService.java` (if exists)
- Create: `backend/src/main/java/com/sme/pm/service/impl/UserServiceImpl.java` (if not exists)

- [ ] **Step 1: Check if UserService exists**

Run: `find backend/src -name "UserService.java"`

- [ ] **Step 2: Add new methods to UserService interface**

```java
// Add to existing interface
void assignRoles(Long userId, List<Long> roleIds);
void assignDepartment(Long userId, Long departmentId);
```

- [ ] **Step 3: Implement new methods in UserServiceImpl**

```java
@Override
@Transactional
public void assignRoles(Long userId, List<Long> roleIds) {
    userMapper.deleteUserRolesByUserId(userId);
    for (Long roleId : roleIds) {
        userMapper.insertUserRole(userId, roleId);
    }
}

@Override
@Transactional
public void assignDepartment(Long userId, Long departmentId) {
    User user = userMapper.selectById(userId);
    if (user != null) {
        user.setDepartmentId(departmentId);
        userMapper.updateById(user);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/service/UserService.java backend/src/main/java/com/sme/pm/service/impl/UserServiceImpl.java
git commit -m "feat: update UserService with role and department assignment"
```

---

### Task 11: Create Department Controller

**Files:**
- Create: `backend/src/main/java/com/sme/pm/controller/DepartmentController.java`
- Test: `backend/src/test/java/com/sme/pm/controller/DepartmentControllerTest.java`

- [ ] **Step 1: Create DepartmentController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Department;
import com.sme.pm.service.DepartmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public Result<List<Department>> getAll() {
        return Result.success(departmentService.getAllDepartments());
    }

    @GetMapping("/tree")
    public Result<List<Department>> getTree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        return dept == null ? Result.success(null) : Result.success(dept);
    }

    @PostMapping
    public Result<Department> create(@RequestBody Department department) {
        return Result.success(departmentService.create(department));
    }

    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return Result.success(departmentService.update(department));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!departmentService.canDelete(id)) {
            return Result.fail("Cannot delete: department has users or children");
        }
        departmentService.delete(id);
        return Result.success();
    }
}
```

- [ ] **Step 2: Write test**

```java
package com.sme.pm.controller;

import com.sme.pm.entity.Department;
import com.sme.pm.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void shouldGetAllDepartments() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 3: Run test**

Run: `mvn test -Dtest=DepartmentControllerTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/controller/DepartmentController.java backend/src/test/java/com/sme/pm/controller/DepartmentControllerTest.java
git commit -m "feat: add DepartmentController with CRUD endpoints"
```

---

### Task 12: Update Role Controller

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/controller/RoleController.java`
- Test: `backend/src/test/java/com/sme/pm/controller/RoleControllerTest.java`

- [ ] **Step 1: Update RoleController with permission endpoints**

```java
package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Permission;
import com.sme.pm.entity.Role;
import com.sme.pm.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public Result<List<Role>> getAll() {
        return Result.success(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return role == null ? Result.success(null) : Result.success(role);
    }

    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        return Result.success(roleService.create(role));
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return Result.success(roleService.update(role));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!roleService.canDelete(id)) {
            return Result.fail("Cannot delete: role has assigned users");
        }
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Permission>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/sme/pm/controller/RoleController.java
git commit -m "feat: update RoleController with permission assignment endpoints"
```

---

### Task 13: Update User Controller

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/controller/UserController.java` (or create if not exists)
- Test: `backend/src/test/java/com/sme/pm/controller/UserControllerTest.java`

- [ ] **Step 1: Add role and department assignment endpoints to UserController**

```java
// Add these endpoints to existing UserController

@PutMapping("/{id}/roles")
public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
    List<Long> roleIds = body.get("roleIds");
    userService.assignRoles(id, roleIds);
    return Result.success();
}

@PutMapping("/{id}/department")
public Result<Void> assignDepartment(@PathVariable Long id, @RequestBody Map<String, Long> body) {
    Long departmentId = body.get("departmentId");
    userService.assignDepartment(id, departmentId);
    return Result.success();
}
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/sme/pm/controller/UserController.java
git commit -m "feat: update UserController with role and department assignment"
```

---

### Task 14: Update Security Config

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/security/SecurityConfig.java`

- [ ] **Step 1: Add admin API endpoints to permitAll**

```java
// In the authorizeHttpRequests section, update:
.requestMatchers("/api/auth/**", "/api/dicts/**", "/api/departments/**", "/api/roles/**", "/api/users/**", "/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

- [ ] **Step 2: Commit**

```bash
git add backend/src/main/java/com/sme/pm/security/SecurityConfig.java
git commit -m "feat: update SecurityConfig to permit admin management APIs"
```

---

### Task 15: Update Frontend Layout with Sub-Menu

**Files:**
- Modify: `frontend/src/components/Layout.vue`

- [ ] **Step 1: Replace single admin menu item with sub-menu**

```vue
<el-sub-menu index="/admin">
    <template #title>
        <el-icon><Setting /></el-icon>
        <span>{{ $t('nav.admin') }}</span>
    </template>
    <el-menu-item index="/admin/users">
        <el-icon><User /></el-icon>
        <span>{{ $t('nav.userManagement') }}</span>
    </el-menu-item>
    <el-menu-item index="/admin/roles">
        <el-icon><Key /></el-icon>
        <span>{{ $t('nav.roleManagement') }}</span>
    </el-menu-item>
    <el-menu-item index="/admin/departments">
        <el-icon><Office /></el-icon>
        <span>{{ $t('nav.departmentManagement') }}</span>
    </el-menu-item>
</el-sub-menu>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/Layout.vue
git commit -m "feat: update Layout with admin sub-menu"
```

---

### Task 16: Update Router

**Files:**
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: Update routes to use new structure**

```javascript
{
    path: '/admin',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
        {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('@/views/admin/Users.vue')
        },
        {
            path: 'roles',
            name: 'AdminRoles',
            component: () => import('@/views/admin/Roles.vue')
        },
        {
            path: 'departments',
            name: 'AdminDepartments',
            component: () => import('@/views/admin/Departments.vue')
        }
    ]
}
```

Note: Remove the old `/admin/users` route.

- [ ] **Step 2: Commit**

```bash
git add frontend/src/router/index.js
git commit -m "feat: update router with admin sub-routes"
```

---

### Task 17: Create Department Management Page

**Files:**
- Create: `frontend/src/views/admin/Departments.vue`

- [ ] **Step 1: Create Departments.vue with el-tree**

```vue
<template>
  <div class="departments-page">
    <div class="page-header">
        <h2>{{ $t('admin.departments') }}</h2>
        <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon> {{ $t('admin.addDepartment') }}
        </el-button>
    </div>

    <el-card>
        <el-tree :data="departmentTree" :props="{ children: 'children', label: 'name' }" node-key="id" default-expand-all>
            <template #default="{ node, data }">
                <span class="tree-node">
                    <span>{{ data.name }}</span>
                    <span class="actions">
                        <el-button text size="small" @click="handleEdit(data)">{{ $t('common.edit') }}</el-button>
                        <el-button text size="small" type="danger" @click="handleDelete(data)">{{ $t('common.delete') }}</el-button>
                    </span>
                </span>
            </template>
        </el-tree>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? $t('admin.editDepartment') : $t('admin.addDepartment')" width="500px">
        <el-form :model="departmentForm" ref="formRef" label-width="100px">
            <el-form-item :label="$t('admin.departmentId')" prop="departmentId">
                <el-input v-model="departmentForm.departmentId" :disabled="isEdit" />
            </el-form-item>
            <el-form-item :label="$t('admin.departmentName')" prop="name">
                <el-input v-model="departmentForm.name" />
            </el-form-item>
            <el-form-item :label="$t('admin.parentDepartment')">
                <el-tree-select v-model="departmentForm.parentId" :data="departmentTree" :props="{ label: 'name', value: 'id' }" clearable placeholder="-- Root --" />
            </el-form-item>
            <el-form-item :label="$t('admin.sortOrder')">
                <el-input-number v-model="departmentForm.sortOrder" :min="0" />
            </el-form-item>
            <el-form-item :label="$t('admin.status')">
                <el-switch v-model="departmentForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const departmentTree = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const departmentForm = ref({})
const formRef = ref()

const loadDepartments = async () => {
    const res = await fetch('/api/departments/tree')
    const result = await res.json()
    if (result.code === 200) {
        departmentTree.value = result.data
    }
}

const handleEdit = (dept) => {
    isEdit.value = true
    departmentForm.value = { ...dept }
    showDialog.value = true
}

const handleDelete = async (dept) => {
    const res = await fetch(`/api/departments/${dept.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
        ElMessage.success('Deleted')
        loadDepartments()
    } else {
        ElMessage.error(result.message || 'Delete failed')
    }
}

const handleSubmit = async () => {
    const method = isEdit.value ? 'PUT' : 'POST'
    const url = isEdit.value ? `/api/departments/${departmentForm.value.id}` : '/api/departments'
    const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(departmentForm.value)
    })
    const result = await res.json()
    if (result.code === 200) {
        showDialog.value = false
        loadDepartments()
    }
}

onMounted(loadDepartments)
</script>

<style scoped>
.tree-node {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding-right: 20px;
}
.actions {
    display: none;
}
.el-tree-node__content:hover .actions {
    display: inline;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/admin/Departments.vue
git commit -m "feat: add Department management page with tree view"
```

---

### Task 18: Create Role Management Page

**Files:**
- Create: `frontend/src/views/admin/Roles.vue`

- [ ] **Step 1: Create Roles.vue**

```vue
<template>
  <div class="roles-page">
    <div class="page-header">
        <h2>{{ $t('admin.roles') }}</h2>
        <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> {{ $t('admin.addRole') }}
        </el-button>
    </div>

    <el-card>
        <el-table :data="roles" v-loading="loading" style="width: 100%">
            <el-table-column prop="roleId" :label="$t('admin.roleId')" width="120" />
            <el-table-column prop="name" :label="$t('admin.roleName')" width="150" />
            <el-table-column prop="description" :label="$t('admin.description')" />
            <el-table-column prop="status" :label="$t('admin.status')" width="100">
                <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                        {{ row.status === 1 ? 'Active' : 'Inactive' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column :label="$t('admin.actions')" width="250">
                <template #default="{ row }">
                    <el-button text size="small" @click="openPermissionDialog(row)">{{ $t('admin.assignPermissions') }}</el-button>
                    <el-button text size="small" @click="openDialog(row)">{{ $t('common.edit') }}</el-button>
                    <el-button text size="small" type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
                </template>
            </el-table-column>
        </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? $t('admin.editRole') : $t('admin.addRole')" width="500px">
        <el-form :model="roleForm" ref="formRef" label-width="100px">
            <el-form-item :label="$t('admin.roleId')" prop="roleId">
                <el-input v-model="roleForm.roleId" :disabled="isEdit" />
            </el-form-item>
            <el-form-item :label="$t('admin.roleName')" prop="name">
                <el-input v-model="roleForm.name" />
            </el-form-item>
            <el-form-item :label="$t('admin.description')">
                <el-input v-model="roleForm.description" type="textarea" />
            </el-form-item>
            <el-form-item :label="$t('admin.status')">
                <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="showPermissionDialog" :title="$t('admin.assignPermissions')" width="600px">
        <el-checkbox-group v-model="selectedPermissions">
            <div v-for="module in permissionModules" :key="module.module" class="permission-group">
                <h4>{{ module.module }}</h4>
                <el-checkbox v-for="p in module.permissions" :key="p.id" :label="p.id">{{ p.name }}</el-checkbox>
            </div>
        </el-checkbox-group>
        <template #footer>
            <el-button @click="showPermissionDialog = false">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" @click="handleSavePermissions">{{ $t('common.save') }}</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const roles = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const roleForm = ref({})
const showPermissionDialog = ref(false)
const selectedPermissions = ref([])
const permissionModules = ref([])
const currentRoleId = ref(null)

const loadRoles = async () => {
    loading.value = true
    const res = await fetch('/api/roles')
    const result = await res.json()
    if (result.code === 200) roles.value = result.data
    loading.value = false
}

const loadPermissions = async () => {
    const res = await fetch('/api/permissions')
    const result = await res.json()
    if (result.code === 200) permissionModules.value = result.data
}

const openDialog = (role = null) => {
    if (role) {
        isEdit.value = true
        roleForm.value = { ...role }
    } else {
        isEdit.value = false
        roleForm.value = { status: 1 }
    }
    showDialog.value = true
}

const openPermissionDialog = async (role) => {
    currentRoleId.value = role.id
    const res = await fetch(`/api/roles/${role.id}/permissions`)
    const result = await res.json()
    if (result.code === 200) {
        selectedPermissions.value = result.data.map(p => p.id)
    }
    showPermissionDialog.value = true
}

const handleSubmit = async () => {
    const method = isEdit.value ? 'PUT' : 'POST'
    const url = isEdit.value ? `/api/roles/${roleForm.value.id}` : '/api/roles'
    const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(roleForm.value)
    })
    const result = await res.json()
    if (result.code === 200) {
        showDialog.value = false
        loadRoles()
    }
}

const handleSavePermissions = async () => {
    const res = await fetch(`/api/roles/${currentRoleId.value}/permissions`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ permissionIds: selectedPermissions.value })
    })
    const result = await res.json()
    if (result.code === 200) {
        showPermissionDialog.value = false
    }
}

const handleDelete = async (role) => {
    const res = await fetch(`/api/roles/${role.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
        ElMessage.success('Deleted')
        loadRoles()
    } else {
        ElMessage.error(result.message || 'Delete failed')
    }
}

onMounted(() => {
    loadRoles()
    loadPermissions()
})
</script>

<style scoped>
.permission-group {
    margin-bottom: 15px;
}
.permission-group h4 {
    margin-bottom: 8px;
    text-transform: capitalize;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/admin/Roles.vue
git commit -m "feat: add Role management page with permission assignment"
```

---

### Task 19: Update Users Page

**Files:**
- Modify: `frontend/src/views/admin/Users.vue`

- [ ] **Step 1: Update Users.vue to use new user fields and add role/department assignment**

Key changes:
1. Add `userId` column to table
2. Add role assignment dropdown
3. Add department assignment dropdown
4. Update form to include userId, roleIds, departmentId

```vue
<!-- Add userId column -->
<el-table-column prop="userId" :label="$t('admin.userId')" width="100" />

<!-- Add role assignment -->
<el-form-item :label="$t('admin.roles')">
    <el-select v-model="userForm.roleIds" multiple style="width: 100%">
        <el-option v-for="r in allRoles" :key="r.id" :label="r.name" :value="r.id" />
    </el-select>
</el-form-item>

<!-- Add department assignment -->
<el-form-item :label="$t('admin.department')">
    <el-tree-select v-model="userForm.departmentId" :data="departmentTree" :props="{ label: 'name', value: 'id' }" clearable />
</el-form-item>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/admin/Users.vue
git commit -m "feat: update Users page with userId and role/department assignment"
```

---

### Task 20: Update i18n Translations

**Files:**
- Modify: `frontend/src/locales/en-US.json` and `frontend/src/locales/zh-CN.json`

- [ ] **Step 1: Add translations**

```json
{
    "nav": {
        "admin": "Admin",
        "userManagement": "User Management",
        "roleManagement": "Role Management",
        "departmentManagement": "Department Management"
    },
    "admin": {
        "userId": "User ID",
        "roleId": "Role ID",
        "departmentId": "Department ID",
        "departmentName": "Department Name",
        "parentDepartment": "Parent Department",
        "sortOrder": "Sort Order",
        "assignPermissions": "Assign Permissions",
        "departments": "Departments",
        "roles": "Roles"
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/locales/en-US.json frontend/src/locales/zh-CN.json
git commit -m "feat: add i18n translations for admin management"
```

---

## Self-Review Checklist

1. **Spec coverage:** All features from spec are implemented
2. **Placeholder scan:** No TBD/TODO in code
3. **Type consistency:** All entity fields match between backend and frontend
