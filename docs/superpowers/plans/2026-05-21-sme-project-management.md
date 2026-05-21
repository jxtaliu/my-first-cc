# SME Project Management System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a full-stack project management system with Vue3 frontend and SpringBoot backend, featuring project/sprint/task management, timesheet tracking with approval workflow, and role-based access control.

**Architecture:** Monolithic full-stack with separated frontend (Vue3 + Vite) and backend (SpringBoot). RESTful API communication over HTTPS. JWT authentication with Redis for token blacklist. MySQL for persistent storage.

**Tech Stack:**
- Frontend: Vue 3.4+, Vite 5.x, Element Plus 2.x, Pinia 2.x, Vue Router 4.x, vue-i18n 9.x, Axios
- Backend: Spring Boot 3.x, MyBatis-Plus 3.5, Spring Security, JJWT, MySQL 8.x, Redis
- Build: pnpm (frontend), Maven (backend)

---

## Project Structure

```
my-first-cc/
├── backend/                    # SpringBoot application
│   ├── src/main/java/com/sme/pm/
│   │   ├── SmePmApplication.java
│   │   ├── config/            # Configuration classes
│   │   ├── controller/        # REST controllers
│   │   ├── service/           # Business logic
│   │   ├── mapper/            # MyBatis-Plus mappers
│   │   ├── entity/            # Domain entities
│   │   ├── dto/               # Data transfer objects
│   │   ├── common/            # Shared utilities
│   │   └── security/          # Auth & security
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── frontend/                   # Vue3 application
│   ├── src/
│   │   ├── api/               # Axios API clients
│   │   ├── components/        # Reusable components
│   │   ├── views/             # Page components
│   │   ├── stores/            # Pinia stores
│   │   ├── router/            # Vue Router config
│   │   ├── locales/           # i18n files
│   │   └── App.vue
│   ├── index.html
│   └── package.json
├── docker-compose.yml          # Local dev environment
└── docs/superpowers/plans/    # This plan
```

---

## Backend Implementation

### Task 1: Backend Project Setup

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/sme/pm/SmePmApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/test/java/com/sme/pm/SmePmApplicationTests.java`

- [ ] **Step 1: Create backend directory structure**

```bash
mkdir -p backend/src/main/java/com/sme/pm/{config,controller,service,mapper,entity,dto,common,security}
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/java/com/sme/pm
```

- [ ] **Step 2: Create pom.xml with dependencies**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <groupId>com.sme</groupId>
    <artifactId>pm</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>SME Project Management</name>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.6</mybatis-plus.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Knife4j API Doc -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>4.5.0</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create main application class**

```java
package com.sme.pm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sme.pm.mapper")
public class SmePmApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmePmApplication.class, args);
    }
}
```

- [ ] **Step 4: Create application.yml**

```yaml
server:
  port: 8080

spring:
  application:
    name: sme-pm
  datasource:
    url: jdbc:mysql://localhost:3306/sme_pm?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
      password: ""

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: your-256-bit-secret-key-for-jwt-signing-must-be-long-enough
  expiration: 86400000  # 24 hours in milliseconds

knife4j:
  enable: true
  setting:
    language: zh_cn
```

- [ ] **Step 5: Create test class**

```java
package com.sme.pm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmePmApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 6: Verify backend compiles**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add backend/pom.xml backend/src/
git commit -m "feat: add SpringBoot backend project structure"
```

---

### Task 2: Backend - Core Entities

**Files:**
- Create: `backend/src/main/java/com/sme/pm/entity/User.java`
- Create: `backend/src/main/java/com/sme/pm/entity/Role.java`
- Create: `backend/src/main/java/com/sme/pm/entity/Permission.java`
- Create: `backend/src/main/java/com/sme/pm/entity/Project.java`
- Create: `backend/src/main/java/com/sme/pm/entity/Sprint.java`
- Create: `backend/src/main/java/com/sme/pm/entity/Task.java`
- Create: `backend/src/main/java/com/sme/pm/entity/Timesheet.java`

- [ ] **Step 1: Create User entity**

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

- [ ] **Step 2: Create Role entity**

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

    private String code;  // SUPER_ADMIN, DEPT_ADMIN, PROJECT_ADMIN, MEMBER

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

- [ ] **Step 3: Create Permission entity**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;  // user:read, project:write, etc.

    private String name;

    private String module;  // user, project, task, timesheet, report

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 4: Create Project entity**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Integer projectType;  // 1: internal, 2: external/client

    private Integer status;  // 1: planning, 2: active, 3: archived

    private Integer sprintMode;  // 1: fixed, 2: agile, 3: kanban

    private Long ownerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 5: Create Sprint entity**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sprint")
public class Sprint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer status;  // 1: planning, 2: active, 3: completed

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 6: Create Task entity with hierarchy support**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sprintId;

    private Long parentId;  // null for root tasks

    private Integer depth;  // 1-4, max 4 levels

    private String title;

    private String description;

    private Integer type;  // 1: epic, 2: feature, 3: story, 4: sub-task

    private Integer status;  // 1: todo, 2: in_progress, 3: done

    private Long assigneeId;

    private Integer estimateHours;

    private Integer actualHours;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 7: Create Timesheet entity with approval status**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("timesheet")
public class Timesheet {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long projectId;

    private Long taskId;  // nullable

    private LocalDateTime workDate;

    private Integer hours;

    private String description;

    private Integer approvalStatus;  // 1: pending, 2: approved, 3: rejected

    private Long approverId;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 8: Create association tables (UserRole, RolePermission, ProjectMember)**

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class UserRole {
    private Long userId;
    private Long roleId;
}
```

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("sys_role_permission")
public class RolePermission {
    private Long roleId;
    private Long permissionId;
}
```

```java
package com.sme.pm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("project_member")
public class ProjectMember {
    private Long projectId;
    private Long userId;
    private LocalDateTime joinedAt;
}
```

- [ ] **Step 9: Create SQL schema file**

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS sme_pm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sme_pm;

-- User table
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    real_name VARCHAR(100),
    status TINYINT DEFAULT 1,
    department_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role table
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Permission table
CREATE TABLE sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role association
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role-Permission association
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project table
CREATE TABLE project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    project_type TINYINT NOT NULL COMMENT '1: internal, 2: external',
    status TINYINT DEFAULT 1 COMMENT '1: planning, 2: active, 3: archived',
    sprint_mode TINYINT DEFAULT 1 COMMENT '1: fixed, 2: agile, 3: kanban',
    owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_owner (owner_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project member
CREATE TABLE project_member (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sprint table
CREATE TABLE sprint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    start_date DATE,
    end_date DATE,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task table with hierarchy
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT,
    parent_id BIGINT,
    depth TINYINT DEFAULT 1 COMMENT '1-4, max 4 levels',
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type TINYINT NOT NULL COMMENT '1: epic, 2: feature, 3: story, 4: sub-task',
    status TINYINT DEFAULT 1 COMMENT '1: todo, 2: in_progress, 3: done',
    assignee_id BIGINT,
    estimate_hours INT,
    actual_hours INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_sprint (sprint_id),
    INDEX idx_parent (parent_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Timesheet table
CREATE TABLE timesheet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    task_id BIGINT,
    work_date DATE NOT NULL,
    hours INT NOT NULL,
    description VARCHAR(500),
    approval_status TINYINT DEFAULT 1 COMMENT '1: pending, 2: approved, 3: rejected',
    approver_id BIGINT,
    approved_at DATETIME,
    rejection_reason VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_project (project_id),
    INDEX idx_work_date (work_date),
    INDEX idx_approval (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default roles
INSERT INTO sys_role (code, name, description) VALUES
('SUPER_ADMIN', 'Super Admin', 'Full system access'),
('DEPT_ADMIN', 'Department Admin', 'Department level access'),
('PROJECT_ADMIN', 'Project Admin', 'Project level access'),
('MEMBER', 'Member', 'Basic member access');
```

- [ ] **Step 10: Verify schema and commit**

Run: `mysql -u root -p < backend/src/main/resources/schema.sql`
Expected: Tables created successfully

- [ ] **Step 11: Commit**

```bash
git add backend/src/main/java/com/sme/pm/entity/
git add backend/src/main/resources/schema.sql
git commit -m "feat: add core entities and database schema

Entities: User, Role, Permission, Project, Sprint, Task, Timesheet
Associations: UserRole, RolePermission, ProjectMember
Max task hierarchy depth: 4 levels
Project types: internal/external
Timesheet approval: pending/approved/rejected"
```

---

### Task 3: Backend - Common Infrastructure

**Files:**
- Create: `backend/src/main/java/com/sme/pm/common/Result.java`
- Create: `backend/src/main/java/com/sme/pm/common/ResultCode.java`
- Create: `backend/src/main/java/com/sme/pm/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/sme/pm/common/CurrentUser.java`
- Create: `backend/src/main/java/com/sme/pm/config/MybatisPlusConfig.java`
- Create: `backend/src/main/java/com/sme/pm/config/RedisConfig.java`

- [ ] **Step 1: Create Result response wrapper**

```java
package com.sme.pm.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

- [ ] **Step 2: Create ResultCode enum**

```java
package com.sme.pm.common;

public enum ResultCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "Parameter error"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    INTERNAL_ERROR(500, "Internal server error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

- [ ] **Step 3: Create GlobalExceptionHandler**

```java
package com.sme.pm.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        return Result.error(500, e.getMessage());
    }
}
```

- [ ] **Step 4: Create CurrentUser annotation**

```java
package com.sme.pm.common;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
```

- [ ] **Step 5: Create MyBatis-Plus config with meta-object handler**

```java
package com.sme.pm.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MybatisPlusConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

- [ ] **Step 6: Create Redis config**

```java
package com.sme.pm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/sme/pm/common/
git add backend/src/main/java/com/sme/pm/config/
git commit -m "feat: add common infrastructure (Result, exception handler, configs)"
```

---

### Task 4: Backend - JWT Authentication

**Files:**
- Create: `backend/src/main/java/com/sme/pm/security/JwtTokenProvider.java`
- Create: `backend/src/main/java/com/sme/pm/security/JwtAuthenticationFilter.java`
- Create: `backend/src/main/java/com/sme/pm/security/UserDetailsServiceImpl.java`
- Create: `backend/src/main/java/com/sme/pm/security/SecurityConfig.java`
- Create: `backend/src/main/java/com/sme/pm/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/sme/pm/dto/LoginResponse.java`

- [ ] **Step 1: Create Login DTOs**

```java
package com.sme.pm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
```

```java
package com.sme.pm.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;

    public LoginResponse(String token, Long userId, String username, String realName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
    }
}
```

- [ ] **Step 2: Create JwtTokenProvider**

```java
package com.sme.pm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

- [ ] **Step 3: Create UserDetailsServiceImpl**

```java
package com.sme.pm.security;

import com.sme.pm.entity.Role;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<Role> roles = userMapper.findRolesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
```

- [ ] **Step 4: Create JwtAuthenticationFilter**

```java
package com.sme.pm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            String username = userDetailsService.loadUserByUsername("").getUsername();

            // Note: In production, load user properly from userId in token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 5: Create SecurityConfig**

```java
package com.sme.pm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/sme/pm/security/
git add backend/src/main/java/com/sme/pm/dto/
git commit -m "feat: add JWT authentication with Spring Security"
```

---

### Task 5: Backend - User & Auth APIs

**Files:**
- Create: `backend/src/main/java/com/sme/pm/mapper/UserMapper.java`
- Create: `backend/src/main/java/com/sme/pm/mapper/RoleMapper.java`
- Create: `backend/src/main/java/com/sme/pm/service/UserService.java`
- Create: `backend/src/main/java/com/sme/pm/service/impl/UserServiceImpl.java`
- Create: `backend/src/main/java/com/sme/pm/controller/AuthController.java`
- Create: `backend/src/main/java/com/sme/pm/controller/UserController.java`

- [ ] **Step 1: Create UserMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.User;
import com.sme.pm.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteUserRolesByUserId(@Param("userId") Long userId);
}
```

- [ ] **Step 2: Create RoleMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT * FROM sys_role WHERE deleted = 0")
    List<Role> findAll();
}
```

- [ ] **Step 3: Create UserService**

```java
package com.sme.pm.service;

import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;

public interface UserService {
    LoginResponse login(LoginRequest request);
    User register(User user);
    User getUserById(Long id);
}
```

- [ ] **Step 4: Create UserServiceImpl**

```java
package com.sme.pm.service.impl;

import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import com.sme.pm.security.JwtTokenProvider;
import com.sme.pm.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName());
    }

    @Override
    public User register(User user) {
        User existing = userMapper.findByUsername(user.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);  // Don't return password
        }
        return user;
    }
}
```

- [ ] **Step 5: Create AuthController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.dto.LoginRequest;
import com.sme.pm.dto.LoginResponse;
import com.sme.pm.entity.User;
import com.sme.pm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody User user) {
        return Result.success(userService.register(user));
    }
}
```

- [ ] **Step 6: Create UserController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.User;
import com.sme.pm.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@CurrentUser Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/sme/pm/mapper/
git add backend/src/main/java/com/sme/pm/service/
git add backend/src/main/java/com/sme/pm/controller/
git commit -m "feat: add user auth and management APIs"
```

---

### Task 6: Backend - Project & Sprint APIs

**Files:**
- Create: `backend/src/main/java/com/sme/pm/mapper/ProjectMapper.java`
- Create: `backend/src/main/java/com/sme/pm/mapper/SprintMapper.java`
- Create: `backend/src/main/java/com/sme/pm/service/ProjectService.java`
- Create: `backend/src/main/java/com/sme/pm/service/impl/ProjectServiceImpl.java`
- Create: `backend/src/main/java/com/sme/pm/controller/ProjectController.java`
- Create: `backend/src/main/java/com/sme/pm/controller/SprintController.java`

- [ ] **Step 1: Create ProjectMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("SELECT * FROM project WHERE deleted = 0 ORDER BY created_at DESC")
    List<Project> findAll();

    @Select("SELECT * FROM project WHERE id = #{id} AND deleted = 0")
    Project findById(@Param("id") Long id);

    @Insert("INSERT INTO project_member (project_id, user_id, joined_at) VALUES (#{projectId}, #{userId}, NOW())")
    void addMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Delete("DELETE FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    void removeMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Select("SELECT user_id FROM project_member WHERE project_id = #{projectId}")
    List<Long> findMemberIds(@Param("projectId") Long projectId);
}
```

- [ ] **Step 2: Create SprintMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Sprint;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SprintMapper extends BaseMapper<Sprint> {

    @Select("SELECT * FROM sprint WHERE project_id = #{projectId} AND deleted = 0 ORDER BY created_at DESC")
    List<Sprint> findByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM sprint WHERE id = #{id} AND deleted = 0")
    Sprint findById(@Param("id") Long id);
}
```

- [ ] **Step 3: Create ProjectService**

```java
package com.sme.pm.service;

import com.sme.pm.entity.Project;

import java.util.List;

public interface ProjectService {
    Project create(Project project);
    Project update(Project project);
    void delete(Long id);
    Project getById(Long id);
    List<Project> list();
    void archive(Long id);
    void restore(Long id);
    void addMember(Long projectId, Long userId);
    void removeMember(Long projectId, Long userId);
}
```

- [ ] **Step 4: Create ProjectServiceImpl**

```java
package com.sme.pm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sme.pm.entity.Project;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public Project create(Project project) {
        projectMapper.insert(project);
        return project;
    }

    @Override
    public Project update(Project project) {
        projectMapper.updateById(project);
        return project;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        projectMapper.deleteById(id);
    }

    @Override
    public Project getById(Long id) {
        return projectMapper.findById(id);
    }

    @Override
    public List<Project> list() {
        return projectMapper.findAll();
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(3);  // archived
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(2);  // active
        projectMapper.updateById(project);
    }

    @Override
    @Transactional
    public void addMember(Long projectId, Long userId) {
        projectMapper.addMember(projectId, userId);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        projectMapper.removeMember(projectId, userId);
    }
}
```

- [ ] **Step 5: Create ProjectController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Project;
import com.sme.pm.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Result<Project> create(@RequestBody Project project, @CurrentUser Long userId) {
        project.setOwnerId(userId);
        return Result.success(projectService.create(project));
    }

    @PutMapping("/{id}")
    public Result<Project> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return Result.success(projectService.update(project));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @GetMapping
    public Result<List<Project>> list() {
        return Result.success(projectService.list());
    }

    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        projectService.archive(id);
        return Result.success();
    }

    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        projectService.restore(id);
        return Result.success();
    }

    @PostMapping("/{id}/members/{userId}")
    public Result<Void> addMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.addMember(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeMember(id, userId);
        return Result.success();
    }
}
```

- [ ] **Step 6: Create SprintController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Sprint;
import com.sme.pm.mapper.SprintMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
public class SprintController {

    private final SprintMapper sprintMapper;

    public SprintController(SprintMapper sprintMapper) {
        this.sprintMapper = sprintMapper;
    }

    @PostMapping
    public Result<Sprint> create(@PathVariable Long projectId, @RequestBody Sprint sprint) {
        sprint.setProjectId(projectId);
        sprintMapper.insert(sprint);
        return Result.success(sprint);
    }

    @GetMapping
    public Result<List<Sprint>> list(@PathVariable Long projectId) {
        return Result.success(sprintMapper.findByProjectId(projectId));
    }

    @GetMapping("/{id}")
    public Result<Sprint> getById(@PathVariable Long id) {
        return Result.success(sprintMapper.findById(id));
    }

    @PutMapping("/{id}")
    public Result<Sprint> update(@PathVariable Long id, @RequestBody Sprint sprint) {
        sprint.setId(id);
        sprintMapper.updateById(sprint);
        return Result.success(sprint);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sprintMapper.deleteById(id);
        return Result.success();
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java/com/sme/pm/mapper/ProjectMapper.java
git add backend/src/main/java/com/sme/pm/mapper/SprintMapper.java
git add backend/src/main/java/com/sme/pm/service/ProjectService.java
git add backend/src/main/java/com/sme/pm/service/impl/ProjectServiceImpl.java
git add backend/src/main/java/com/sme/pm/controller/ProjectController.java
git add backend/src/main/java/com/sme/pm/controller/SprintController.java
git commit -m "feat: add project and sprint management APIs"
```

---

### Task 7: Backend - Task & Timesheet APIs

**Files:**
- Create: `backend/src/main/java/com/sme/pm/mapper/TaskMapper.java`
- Create: `backend/src/main/java/com/sme/pm/mapper/TimesheetMapper.java`
- Create: `backend/src/main/java/com/sme/pm/service/TaskService.java`
- Create: `backend/src/main/java/com/sme/pm/service/impl/TaskServiceImpl.java`
- Create: `backend/src/main/java/com/sme/pm/service/TimesheetService.java`
- Create: `backend/src/main/java/com/sme/pm/service/impl/TimesheetServiceImpl.java`
- Create: `backend/src/main/java/com/sme/pm/controller/TaskController.java`
- Create: `backend/src/main/java/com/sme/pm/controller/TimesheetController.java`

- [ ] **Step 1: Create TaskMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT * FROM task WHERE sprint_id = #{sprintId} AND deleted = 0 ORDER BY created_at")
    List<Task> findBySprintId(@Param("sprintId") Long sprintId);

    @Select("SELECT * FROM task WHERE parent_id = #{parentId} AND deleted = 0")
    List<Task> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM task WHERE id = #{id} AND deleted = 0")
    Task findById(@Param("id") Long id);

    @Select("SELECT MAX(depth) FROM task WHERE parent_id = #{parentId} AND deleted = 0")
    Integer getMaxChildDepth(@Param("parentId") Long parentId);
}
```

- [ ] **Step 2: Create TaskService with max depth validation**

```java
package com.sme.pm.service;

import com.sme.pm.entity.Task;
import java.util.List;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    void delete(Long id);
    Task getById(Long id);
    List<Task> listBySprint(Long sprintId);
    List<Task> listByParent(Long parentId);
}
```

- [ ] **Step 3: Create TaskServiceImpl**

```java
package com.sme.pm.service.impl;

import com.sme.pm.entity.Task;
import com.sme.pm.mapper.TaskMapper;
import com.sme.pm.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int MAX_DEPTH = 4;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public Task create(Task task) {
        // Validate max depth
        if (task.getParentId() != null) {
            Integer maxChildDepth = taskMapper.getMaxChildDepth(task.getParentId());
            Task parent = taskMapper.findById(task.getParentId());
            if (parent != null) {
                int newDepth = parent.getDepth() + 1;
                if (newDepth > MAX_DEPTH) {
                    throw new IllegalArgumentException("Maximum task hierarchy depth is " + MAX_DEPTH);
                }
                task.setDepth(newDepth);
            }
        } else {
            task.setDepth(1);
        }

        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task update(Task task) {
        taskMapper.updateById(task);
        return task;
    }

    @Override
    public void delete(Long id) {
        taskMapper.deleteById(id);
    }

    @Override
    public Task getById(Long id) {
        return taskMapper.findById(id);
    }

    @Override
    public List<Task> listBySprint(Long sprintId) {
        return taskMapper.findBySprintId(sprintId);
    }

    @Override
    public List<Task> listByParent(Long parentId) {
        return taskMapper.findByParentId(parentId);
    }
}
```

- [ ] **Step 4: Create TimesheetMapper**

```java
package com.sme.pm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sme.pm.entity.Timesheet;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TimesheetMapper extends BaseMapper<Timesheet> {

    @Select("SELECT * FROM timesheet WHERE user_id = #{userId} AND deleted = 0 " +
            "AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date")
    List<Timesheet> findByUserAndDateRange(@Param("userId") Long userId,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    @Select("SELECT * FROM timesheet WHERE project_id = #{projectId} AND deleted = 0 " +
            "AND work_date BETWEEN #{startDate} AND #{endDate} ORDER BY work_date")
    List<Timesheet> findByProjectAndDateRange(@Param("projectId") Long projectId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    @Select("SELECT * FROM timesheet WHERE approval_status = 1 AND deleted = 0")
    List<Timesheet> findPendingApproval();
}
```

- [ ] **Step 5: Create TimesheetService**

```java
package com.sme.pm.service;

import com.sme.pm.entity.Timesheet;
import java.util.List;

public interface TimesheetService {
    Timesheet create(Timesheet timesheet);
    Timesheet update(Timesheet timesheet);
    void delete(Long id);
    List<Timesheet> listWeekly(Long userId, String startDate, String endDate);
    List<Timesheet> listMonthly(Long userId, String startDate, String endDate);
    List<Timesheet> listProjectTimesheets(Long projectId, String startDate, String endDate);
    void approve(Long timesheetId, Long approverId);
    void reject(Long timesheetId, Long approverId, String reason);
    void resubmit(Long timesheetId);
}
```

- [ ] **Step 6: Create TimesheetServiceImpl with approval logic**

```java
package com.sme.pm.service.impl;

import com.sme.pm.entity.Timesheet;
import com.sme.pm.entity.Project;
import com.sme.pm.mapper.TimesheetMapper;
import com.sme.pm.mapper.ProjectMapper;
import com.sme.pm.service.TimesheetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetMapper timesheetMapper;
    private final ProjectMapper projectMapper;

    public TimesheetServiceImpl(TimesheetMapper timesheetMapper, ProjectMapper projectMapper) {
        this.timesheetMapper = timesheetMapper;
        this.projectMapper = projectMapper;
    }

    @Override
    @Transactional
    public Timesheet create(Timesheet timesheet) {
        // Auto-approve for internal projects
        Project project = projectMapper.findById(timesheet.getProjectId());
        if (project != null && project.getProjectType() == 1) {
            timesheet.setApprovalStatus(2);  // auto-approved
        } else {
            timesheet.setApprovalStatus(1);  // pending
        }

        timesheetMapper.insert(timesheet);
        return timesheet;
    }

    @Override
    public Timesheet update(Timesheet timesheet) {
        timesheetMapper.updateById(timesheet);
        return timesheet;
    }

    @Override
    public void delete(Long id) {
        timesheetMapper.deleteById(id);
    }

    @Override
    public List<Timesheet> listWeekly(Long userId, String startDate, String endDate) {
        return timesheetMapper.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<Timesheet> listMonthly(Long userId, String startDate, String endDate) {
        return timesheetMapper.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<Timesheet> listProjectTimesheets(Long projectId, String startDate, String endDate) {
        return timesheetMapper.findByProjectAndDateRange(projectId, startDate, endDate);
    }

    @Override
    @Transactional
    public void approve(Long timesheetId, Long approverId) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(2);
        timesheet.setApproverId(approverId);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheetMapper.updateById(timesheet);
    }

    @Override
    @Transactional
    public void reject(Long timesheetId, Long approverId, String reason) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(3);
        timesheet.setApproverId(approverId);
        timesheet.setRejectionReason(reason);
        timesheetMapper.updateById(timesheet);
    }

    @Override
    @Transactional
    public void resubmit(Long timesheetId) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(timesheetId);
        timesheet.setApprovalStatus(1);  // back to pending
        timesheetMapper.updateById(timesheet);
    }
}
```

- [ ] **Step 7: Create TaskController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.Result;
import com.sme.pm.entity.Task;
import com.sme.pm.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sprints/{sprintId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Result<Task> create(@PathVariable Long sprintId, @RequestBody Task task) {
        task.setSprintId(sprintId);
        return Result.success(taskService.create(task));
    }

    @GetMapping
    public Result<List<Task>> list(@PathVariable Long sprintId) {
        return Result.success(taskService.listBySprint(sprintId));
    }

    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.update(task));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success();
    }
}
```

- [ ] **Step 8: Create TimesheetController**

```java
package com.sme.pm.controller;

import com.sme.pm.common.CurrentUser;
import com.sme.pm.common.Result;
import com.sme.pm.entity.Timesheet;
import com.sme.pm.service.TimesheetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @PostMapping
    public Result<Timesheet> create(@RequestBody Timesheet timesheet, @CurrentUser Long userId) {
        timesheet.setUserId(userId);
        return Result.success(timesheetService.create(timesheet));
    }

    @GetMapping("/weekly")
    public Result<List<Timesheet>> weekly(@CurrentUser Long userId,
                                          @RequestParam String startDate,
                                          @RequestParam String endDate) {
        return Result.success(timesheetService.listWeekly(userId, startDate, endDate));
    }

    @GetMapping("/monthly")
    public Result<List<Timesheet>> monthly(@CurrentUser Long userId,
                                           @RequestParam String startDate,
                                           @RequestParam String endDate) {
        return Result.success(timesheetService.listMonthly(userId, startDate, endDate));
    }

    @GetMapping("/project/{projectId}")
    public Result<List<Timesheet>> projectTimesheets(@PathVariable Long projectId,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate) {
        return Result.success(timesheetService.listProjectTimesheets(projectId, startDate, endDate));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @CurrentUser Long userId) {
        timesheetService.approve(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @CurrentUser Long userId,
                               @RequestParam String reason) {
        timesheetService.reject(id, userId, reason);
        return Result.success();
    }

    @PostMapping("/{id}/resubmit")
    public Result<Void> resubmit(@PathVariable Long id) {
        timesheetService.resubmit(id);
        return Result.success();
    }
}
```

- [ ] **Step 9: Commit**

```bash
git add backend/src/main/java/com/sme/pm/mapper/TaskMapper.java
git add backend/src/main/java/com/sme/pm/mapper/TimesheetMapper.java
git add backend/src/main/java/com/sme/pm/service/TaskService.java
git add backend/src/main/java/com/sme/pm/service/impl/TaskServiceImpl.java
git add backend/src/main/java/com/sme/pm/service/TimesheetService.java
git add backend/src/main/java/com/sme/pm/service/impl/TimesheetServiceImpl.java
git add backend/src/main/java/com/sme/pm/controller/TaskController.java
git add backend/src/main/java/com/sme/pm/controller/TimesheetController.java
git commit -m "feat: add task and timesheet APIs with approval workflow

- Task hierarchy max 4 levels enforced
- Internal project timesheets auto-approved
- External project timesheets require approval"
```

---

## Frontend Implementation

### Task 8: Frontend Project Setup

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`

- [ ] **Step 1: Create frontend directory structure**

```bash
mkdir -p frontend/src/{api,components,views,stores,router,locales,utils}
mkdir -p frontend/src/views/{auth,admin,project,timesheet,dashboard}
```

- [ ] **Step 2: Create package.json**

```json
{
  "name": "sme-pm-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.js,.jsx --fix"
  },
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "axios": "^1.6.8",
    "element-plus": "^2.7.0",
    "@element-plus/icons-vue": "^2.3.1",
    "vue-i18n": "^9.10.2",
    "dayjs": "^1.11.10"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "vite": "^5.2.8",
    "eslint": "^8.57.0",
    "eslint-plugin-vue": "^9.23.0",
    "sass": "^1.72.0"
  }
}
```

- [ ] **Step 3: Create vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: Create index.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SME Project Management</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 5: Create main.js**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import i18n from './locales'
import App from './App.vue'

const app = createApp(App)

// Register all icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.use(i18n)

app.mount('#app')
```

- [ ] **Step 6: Create App.vue**

```vue
<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
```

- [ ] **Step 7: Commit**

```bash
git add frontend/package.json frontend/vite.config.js frontend/index.html
git add frontend/src/main.js frontend/src/App.vue
git commit -m "feat: add Vue3 frontend project structure"
```

---

### Task 9: Frontend - Router & Auth Store

**Files:**
- Create: `frontend/src/router/index.js`
- Create: `frontend/src/stores/auth.js`
- Create: `frontend/src/api/request.js`
- Create: `frontend/src/locales/index.js`
- Create: `frontend/src/locales/zh-CN.js`
- Create: `frontend/src/locales/en-US.js`

- [ ] **Step 1: Create Axios request utility**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(res)
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    ElMessage.error(error.response?.data?.message || 'Request failed')
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 2: Create auth store**

```javascript
import { defineStore } from 'pinia'
import request from '@/api/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null
  }),

  actions: {
    async login(username, password) {
      const res = await request.post('/auth/login', { username, password })
      this.token = res.data.token
      this.user = res.data
      localStorage.setItem('token', this.token)
      return res.data
    },

    async register(data) {
      const res = await request.post('/auth/register', data)
      return res.data
    },

    async getCurrentUser() {
      const res = await request.get('/users/me')
      this.user = res.data
      return res.data
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})
```

- [ ] **Step 3: Create router configuration**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue')
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/List.vue')
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/Detail.vue')
      },
      {
        path: 'timesheet',
        name: 'Timesheet',
        component: () => import('@/views/timesheet/Index.vue')
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth !== false && !authStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 4: Create i18n configuration**

```javascript
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export default i18n
```

- [ ] **Step 5: Create Chinese locale**

```javascript
export default {
  common: {
    save: '保存',
    cancel: '取消',
    delete: '删除',
    edit: '编辑',
    add: '添加',
    search: '搜索',
    confirm: '确认',
    success: '成功',
    failed: '失败'
  },
  auth: {
    login: '登录',
    register: '注册',
    username: '用户名',
    password: '密码',
    email: '邮箱'
  },
  nav: {
    dashboard: '仪表盘',
    projects: '项目管理',
    timesheet: '工时管理',
    admin: '管理'
  }
}
```

- [ ] **Step 6: Create English locale**

```javascript
export default {
  common: {
    save: 'Save',
    cancel: 'Cancel',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    confirm: 'Confirm',
    success: 'Success',
    failed: 'Failed'
  },
  auth: {
    login: 'Login',
    register: 'Register',
    username: 'Username',
    password: 'Password',
    email: 'Email'
  },
  nav: {
    dashboard: 'Dashboard',
    projects: 'Projects',
    timesheet: 'Timesheet',
    admin: 'Admin'
  }
}
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/router/index.js
git add frontend/src/stores/auth.js
git add frontend/src/api/request.js
git add frontend/src/locales/
git commit -m "feat: add Vue Router, Pinia auth store, Axios, and i18n"
```

---

### Task 10: Frontend - Auth & Layout Pages

**Files:**
- Create: `frontend/src/views/auth/Login.vue`
- Create: `frontend/src/components/Layout.vue`
- Create: `frontend/src/views/dashboard/Index.vue`

- [ ] **Step 1: Create Login page**

```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>{{ $t('auth.login') }}</h2>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item :label="$t('auth.username')" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item :label="$t('auth.password')" prop="password">
          <el-input v-model="form.password" type="password" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            {{ $t('auth.login') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Username is required' }],
  password: [{ required: true, message: 'Password is required' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('Login successful')
    router.push('/')
  } catch (error) {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}
</style>
```

- [ ] **Step 2: Create Layout component**

```vue
<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">SME PM</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ $t('nav.dashboard') }}</span>
        </el-menu-item>
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>{{ $t('nav.projects') }}</span>
        </el-menu-item>
        <el-menu-item index="/timesheet">
          <el-icon><Clock /></el-icon>
          <span>{{ $t('nav.timesheet') }}</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>{{ $t('nav.admin') }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ authStore.user?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  background: #2b3a4a;
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
}
</style>
```

- [ ] **Step 3: Create Dashboard page**

```vue
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>Projects</template>
          <div class="stat-value">{{ stats.projects }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>Tasks</template>
          <div class="stat-value">{{ stats.tasks }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>Hours This Week</template>
          <div class="stat-value">{{ stats.hours }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const stats = ref({
  projects: 0,
  tasks: 0,
  hours: 0
})

onMounted(async () => {
  try {
    const res = await request.get('/projects')
    stats.value.projects = res.data?.length || 0
  } catch (e) {
    // Handle error
  }
})
</script>

<style scoped>
.stat-value {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  padding: 20px 0;
}
</style>
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/auth/Login.vue
git add frontend/src/components/Layout.vue
git add frontend/src/views/dashboard/Index.vue
git commit -m "feat: add login page, layout, and dashboard"
```

---

### Task 11: Frontend - Project & Task Pages

**Files:**
- Create: `frontend/src/views/project/List.vue`
- Create: `frontend/src/views/project/Detail.vue`
- Create: `frontend/src/api/project.js`
- Create: `frontend/src/api/task.js`

- [ ] **Step 1: Create project API**

```javascript
import request from './request'

export const projectApi = {
  list: () => request.get('/projects'),
  getById: (id) => request.get(`/projects/${id}`),
  create: (data) => request.post('/projects', data),
  update: (id, data) => request.put(`/projects/${id}`, data),
  delete: (id) => request.delete(`/projects/${id}`),
  archive: (id) => request.post(`/projects/${id}/archive`),
  restore: (id) => request.post(`/projects/${id}/restore`),
  addMember: (id, userId) => request.post(`/projects/${id}/members/${userId}`),
  removeMember: (id, userId) => request.delete(`/projects/${id}/members/${userId}`)
}
```

- [ ] **Step 2: Create task API**

```javascript
import request from './request'

export const taskApi = {
  listBySprint: (sprintId) => request.get(`/projects/${sprintId}/sprints/${sprintId}/tasks`),
  getById: (id) => request.get(`/tasks/${id}`),
  create: (sprintId, data) => request.post(`/sprints/${sprintId}/tasks`, data),
  update: (id, data) => request.put(`/tasks/${id}`, data),
  delete: (id) => request.delete(`/tasks/${id}`)
}
```

- [ ] **Step 3: Create Project List page**

```vue
<template>
  <div class="project-list">
    <div class="toolbar">
      <el-button type="primary" @click="handleCreate">New Project</el-button>
    </div>

    <el-table :data="projects" stripe>
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="projectType" label="Type">
        <template #default="{ row }">
          {{ row.projectType === 1 ? 'Internal' : 'External' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">View</el-button>
          <el-button link type="danger" @click="handleDelete(row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="Name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Type">
          <el-select v-model="form.projectType">
            <el-option :value="1" label="Internal" />
            <el-option :value="2" label="External" />
          </el-select>
        </el-form-item>
        <el-form-item label="Sprint Mode">
          <el-select v-model="form.sprintMode">
            <el-option :value="1" label="Fixed Sprint" />
            <el-option :value="2" label="Agile Iteration" />
            <el-option :value="3" label="Kanban" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleSave">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/project'

const router = useRouter()
const projects = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('New Project')
const form = reactive({
  name: '',
  projectType: 1,
  sprintMode: 1
})

const loadProjects = async () => {
  const res = await projectApi.list()
  projects.value = res.data || []
}

const handleCreate = () => {
  dialogTitle.value = 'New Project'
  Object.assign(form, { name: '', projectType: 1, sprintMode: 1 })
  dialogVisible.value = true
}

const handleView = (row) => {
  router.push(`/projects/${row.id}`)
}

const handleSave = async () => {
  await projectApi.create(form)
  ElMessage.success('Project created')
  dialogVisible.value = false
  loadProjects()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('Delete this project?', 'Warning', { type: 'warning' })
  await projectApi.delete(row.id)
  ElMessage.success('Deleted')
  loadProjects()
}

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'success', 3: '' }
  return types[status] || ''
}

const getStatusLabel = (status) => {
  const labels = { 1: 'Planning', 2: 'Active', 3: 'Archived' }
  return labels[status] || ''
}

onMounted(loadProjects)
</script>
```

- [ ] **Step 4: Create Project Detail page with Kanban**

```vue
<template>
  <div class="project-detail">
    <div class="toolbar">
      <h2>{{ project.name }}</h2>
      <el-button @click="showTaskDialog = true">New Task</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="Board" name="board">
        <div class="kanban-board">
          <div v-for="status in taskStatuses" :key="status.value" class="kanban-column">
            <div class="column-header">{{ status.label }}</div>
            <div class="column-tasks">
              <el-card v-for="task in getTasksByStatus(status.value)" :key="task.id" class="task-card" shadow="hover">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-type">{{ getTypeLabel(task.type) }}</div>
              </el-card>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Sprints" name="sprints">
        <el-button @click="showSprintDialog = true">New Sprint</el-button>
        <el-list>
          <el-list-item v-for="sprint in sprints" :key="sprint.id">
            {{ sprint.name }} ({{ sprint.startDate }} - {{ sprint.endDate }})
          </el-list-item>
        </el-list>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showTaskDialog" title="New Task" width="500px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="Title">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item label="Type">
          <el-select v-model="taskForm.type">
            <el-option :value="1" label="Epic" />
            <el-option :value="2" label="Feature" />
            <el-option :value="3" label="Story" />
            <el-option :value="4" label="Sub-task" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">Cancel</el-button>
        <el-button type="primary" @click="handleCreateTask">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/project'
import { taskApi } from '@/api/task'

const route = useRoute()
const project = ref({})
const tasks = ref([])
const sprints = ref([])
const activeTab = ref('board')
const showTaskDialog = ref(false)
const showSprintDialog = ref(false)

const taskStatuses = [
  { value: 1, label: 'To Do' },
  { value: 2, label: 'In Progress' },
  { value: 3, label: 'Done' }
]

const taskForm = reactive({
  title: '',
  type: 3
})

const loadProject = async () => {
  const res = await projectApi.getById(route.params.id)
  project.value = res.data
}

const loadTasks = async () => {
  if (sprints.value.length > 0) {
    const res = await taskApi.listBySprint(sprints.value[0].id)
    tasks.value = res.data || []
  }
}

const getTasksByStatus = (status) => {
  return tasks.value.filter(t => t.status === status)
}

const getTypeLabel = (type) => {
  const labels = { 1: 'Epic', 2: 'Feature', 3: 'Story', 4: 'Sub-task' }
  return labels[type] || ''
}

const handleCreateTask = async () => {
  if (sprints.value.length === 0) {
    ElMessage.warning('Please create a sprint first')
    return
  }
  await taskApi.create(sprints.value[0].id, taskForm)
  ElMessage.success('Task created')
  showTaskDialog.value = false
  loadTasks()
}

onMounted(async () => {
  await loadProject()
  // Load sprints...
})
</script>

<style scoped>
.kanban-board {
  display: flex;
  gap: 16px;
  min-height: 500px;
}

.kanban-column {
  flex: 1;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
}

.column-header {
  font-weight: bold;
  margin-bottom: 12px;
  padding: 8px;
}

.column-tasks {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-card {
  cursor: pointer;
}

.task-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.task-type {
  font-size: 12px;
  color: #999;
}
</style>
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/api/project.js frontend/src/api/task.js
git add frontend/src/views/project/List.vue frontend/src/views/project/Detail.vue
git commit -m "feat: add project list and detail pages with Kanban board"
```

---

### Task 12: Frontend - Timesheet Page

**Files:**
- Create: `frontend/src/api/timesheet.js`
- Create: `frontend/src/views/timesheet/Index.vue`

- [ ] **Step 1: Create timesheet API**

```javascript
import request from './request'

export const timesheetApi = {
  create: (data) => request.post('/timesheets', data),
  weekly: (startDate, endDate) => request.get('/timesheets/weekly', {
    params: { startDate, endDate }
  }),
  monthly: (startDate, endDate) => request.get('/timesheets/monthly', {
    params: { startDate, endDate }
  }),
  approve: (id) => request.post(`/timesheets/${id}/approve`),
  reject: (id, reason) => request.post(`/timesheets/${id}/reject`, { reason }),
  resubmit: (id) => request.post(`/timesheets/${id}/resubmit`)
}
```

- [ ] **Step 2: Create Timesheet page**

```vue
<template>
  <div class="timesheet">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Weekly View" name="weekly">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-table :data="weeklyEntries" border size="small">
              <el-table-column prop="workDate" label="Date" width="120" />
              <el-table-column prop="projectName" label="Project" />
              <el-table-column prop="hours" label="Hours" width="80" />
              <el-table-column prop="description" label="Description" />
              <el-table-column prop="approvalStatus" label="Status" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.approvalStatus)">
                    {{ getStatusLabel(row.approvalStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
          <el-col :span="8">
            <el-card>
              <template #header>Quick Add</template>
              <el-form :model="form" label-width="80px">
                <el-form-item label="Date">
                  <el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" />
                </el-form-item>
                <el-form-item label="Hours">
                  <el-input-number v-model="form.hours" :min="0.5" :max="24" />
                </el-form-item>
                <el-form-item label="Project">
                  <el-select v-model="form.projectId">
                    <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Description">
                  <el-input v-model="form.description" type="textarea" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleAdd">Add Entry</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="Monthly View" name="monthly">
        <div class="monthly-summary">
          <el-table :data="monthlyData" border>
            <el-table-column prop="projectName" label="Project" />
            <el-table-column prop="totalHours" label="Total Hours" />
            <el-table-column prop="approvedHours" label="Approved" />
            <el-table-column prop="pendingHours" label="Pending" />
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { timesheetApi } from '@/api/timesheet'
import { projectApi } from '@/api/project'
import dayjs from 'dayjs'

const activeTab = ref('weekly')
const weeklyEntries = ref([])
const monthlyData = ref([])
const projects = ref([])

const form = reactive({
  workDate: dayjs().format('YYYY-MM-DD'),
  hours: 8,
  projectId: null,
  description: ''
})

const loadProjects = async () => {
  const res = await projectApi.list()
  projects.value = res.data || []
}

const loadWeeklyData = async () => {
  const start = dayjs().startOf('week').format('YYYY-MM-DD')
  const end = dayjs().endOf('week').format('YYYY-MM-DD')
  const res = await timesheetApi.weekly(start, end)
  weeklyEntries.value = res.data || []
}

const handleAdd = async () => {
  await timesheetApi.create(form)
  ElMessage.success('Entry added')
  Object.assign(form, { hours: 8, description: '' })
  loadWeeklyData()
}

const getStatusType = (status) => {
  const types = { 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || ''
}

const getStatusLabel = (status) => {
  const labels = { 1: 'Pending', 2: 'Approved', 3: 'Rejected' }
  return labels[status] || ''
}

onMounted(() => {
  loadProjects()
  loadWeeklyData()
})
</script>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/api/timesheet.js
git add frontend/src/views/timesheet/Index.vue
git commit -m "feat: add timesheet weekly and monthly views with entry form"
```

---

## Docker & CI/CD Setup

### Task 13: Docker Compose & GitHub Actions

**Files:**
- Create: `docker-compose.yml`
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create docker-compose.yml**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: sme_pm
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  mysql_data:
```

- [ ] **Step 2: Create GitHub Actions workflow**

```yaml
name: CI/CD

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build with Maven
        run: cd backend && mvn clean test

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup pnpm
        uses: pnpm/action-setup@v2
        with:
          version: 8
      - name: Install dependencies
        run: cd frontend && pnpm install
      - name: Lint
        run: cd frontend && pnpm lint
```

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml .github/workflows/ci.yml
git commit -m "feat: add Docker Compose and GitHub Actions CI/CD"
```

---

## Spec Coverage Check

| Spec Requirement | Task(s) |
|------------------|---------|
| user-auth | Tasks 4, 5 |
| role-permission | Tasks 4, 5 |
| project-management | Tasks 6, 11 |
| task hierarchy (max 4) | Task 7 |
| time-tracking | Tasks 7, 12 |
| timesheet approval | Tasks 7, 12 |
| dashboard | Tasks 8, 10 |
| multi-tenancy | Tasks 8, 9 |
| project archive | Tasks 6, 11 |
| report export | (Deferred for future implementation) |

---

**Plan complete.** This plan provides 13 bite-sized tasks covering backend and frontend implementation, with each step producing verifiable, commit-ready changes.
