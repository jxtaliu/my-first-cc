-- Create database
CREATE DATABASE IF NOT EXISTS sme_pm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sme_pm;

-- ============================================
-- Base System Tables (依赖顺序)
-- ============================================

-- Role table (must be created before sys_user due to FK)
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Department table (FK target for sys_user.department_id)
CREATE TABLE IF NOT EXISTS sys_department (
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

-- User table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,
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
    INDEX idx_email (email),
    INDEX idx_user_id (user_id),
    INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Permission table
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role association
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role-Permission association
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dictionary type table
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dictionary code table
CREATE TABLE IF NOT EXISTS sys_dict_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100),
    name_en VARCHAR(100),
    name_zh VARCHAR(100),
    sort_order INT DEFAULT 0,
    extra VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Project Management Tables
-- ============================================

-- Project table
CREATE TABLE IF NOT EXISTS project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'Business key, e.g., PRJ_001',
    name VARCHAR(200) NOT NULL,
    description TEXT,
    project_type VARCHAR(50) NOT NULL COMMENT 'DEVELOPE/CUSTOM - from sys_dict_code',
    status VARCHAR(50) DEFAULT 'PLANNING' COMMENT 'PLANNING/ACTIVE/COMPLETED/ARCHIVED - from sys_dict_code',
    sprint_mode VARCHAR(50) DEFAULT 'SCRUM' COMMENT 'SCRUM/KANBAN - from sys_dict_code',
    owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project_id (project_id),
    INDEX idx_owner (owner_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project member
CREATE TABLE IF NOT EXISTS project_member (
    project_id VARCHAR(50) NOT NULL COMMENT 'References project.project_id',
    user_id BIGINT NOT NULL,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sprint table (enhanced with PM fields)
CREATE TABLE IF NOT EXISTS sprint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL COMMENT 'References project.project_id',
    name VARCHAR(200) NOT NULL,
    goal VARCHAR(500) COMMENT 'Sprint goal',
    start_date DATE,
    end_date DATE,
    status TINYINT DEFAULT 1,
    capacity_hours INT COMMENT 'Total team capacity in hours',
    velocity INT COMMENT 'Story points completed last sprint',
    start_reminder_sent TINYINT DEFAULT 0,
    end_reminder_sent TINYINT DEFAULT 0,
    milestone_id BIGINT COMMENT 'Associated milestone',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_status (status),
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task table with hierarchy (enhanced with PM fields)
CREATE TABLE IF NOT EXISTS task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'Business key, e.g., TSK001',
    project_id VARCHAR(50) NOT NULL COMMENT 'References project.project_id, for fast query',
    sprint_id BIGINT,
    parent_id BIGINT,
    depth TINYINT DEFAULT 1 COMMENT '1-4, max 4 levels',
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'STORY' COMMENT 'EPIC/FEATURE/STORY/TASK/BUG/SUBTASK - from sys_dict_code',
    status TINYINT DEFAULT 1 COMMENT '1: todo, 2: in_progress, 3: done',
    priority VARCHAR(20) DEFAULT 'P2' COMMENT 'P0/P1/P2/P3',
    assignee_id BIGINT,
    estimate_hours INT,
    actual_hours INT,
    remaining_hours INT COMMENT 'Remaining work hours',
    progress INT DEFAULT 0 COMMENT 'Progress percentage 0-100',
    milestone_id BIGINT COMMENT 'Auto-linked from sprint',
    start_date DATE COMMENT 'Actual start date',
    due_date DATE COMMENT 'Due date',
    in_progress_since DATETIME COMMENT 'When task entered in_progress status',
    completion_date DATETIME COMMENT 'When task was completed',
    version INT DEFAULT 1 COMMENT 'Optimistic lock',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_sprint (sprint_id),
    INDEX idx_parent (parent_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_milestone (milestone_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Timesheet table
CREATE TABLE IF NOT EXISTS timesheet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    project_id VARCHAR(50) NOT NULL COMMENT 'References project.project_id',
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

-- ============================================
-- New PM Tables
-- ============================================

-- Project template
CREATE TABLE IF NOT EXISTS project_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Template name',
    description VARCHAR(500),
    sprint_duration INT DEFAULT 14 COMMENT 'Sprint duration in days',
    enable_priority TINYINT DEFAULT 1 COMMENT 'Enable P0-P3 priority',
    task_types VARCHAR(255) DEFAULT 'EPIC,FEATURE,STORY,TASK,BUG' COMMENT 'Enabled task types',
    default_status_flow VARCHAR(500) COMMENT 'Default status transition flow',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project role
CREATE TABLE IF NOT EXISTS project_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) NOT NULL COMMENT 'References project.project_id',
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL COMMENT 'PROJECT_OWNER/PROJECT_MANAGER/DEV_LEAD/DEVELOPER/GUEST',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_project_user (project_id, user_id),
    INDEX idx_project (project_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task status
CREATE TABLE IF NOT EXISTS task_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) COMMENT 'NULL means system default, references project.project_id',
    code VARCHAR(50) NOT NULL COMMENT 'TODO, IN_PROGRESS, etc.',
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    name_zh VARCHAR(100),
    category VARCHAR(20) NOT NULL COMMENT 'todo/doing/done/alert',
    color VARCHAR(20) DEFAULT '#909399',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Status transition
CREATE TABLE IF NOT EXISTS status_transition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) COMMENT 'NULL means system default, references project.project_id',
    from_status_id BIGINT NOT NULL COMMENT 'Source status',
    to_status_id BIGINT NOT NULL COMMENT 'Target status',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_from (from_status_id),
    INDEX idx_to (to_status_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task dependency
CREATE TABLE IF NOT EXISTS task_dependency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT 'Current task',
    depends_on_task_id BIGINT NOT NULL COMMENT 'Dependency task',
    dependency_type VARCHAR(20) DEFAULT 'FS' COMMENT 'FS: Finish-Start, SS: Start-Start, FF: Finish-Finish, SF: Start-Finish',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_task_dependency (task_id, depends_on_task_id),
    INDEX idx_task (task_id),
    INDEX idx_depends_on (depends_on_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task attachment
CREATE TABLE IF NOT EXISTS task_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT COMMENT 'File size in bytes',
    mime_type VARCHAR(100),
    uploaded_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_task (task_id),
    INDEX idx_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Task comment
CREATE TABLE IF NOT EXISTS task_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_comment_id BIGINT COMMENT 'For replies',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_task (task_id),
    INDEX idx_user (user_id),
    INDEX idx_parent (parent_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Milestone
CREATE TABLE IF NOT EXISTS milestone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(50) COMMENT 'NULL for cross-project milestones, references project.project_id',
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    target_date DATE NOT NULL,
    is_cross_project TINYINT DEFAULT 0 COMMENT 'Cross-project milestone',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ACHIEVED/FAILED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_target_date (target_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Project milestone association
CREATE TABLE IF NOT EXISTS project_milestone (
    project_id VARCHAR(50) NOT NULL COMMENT 'References project.project_id',
    milestone_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, milestone_id),
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Notification
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Recipient',
    type VARCHAR(50) NOT NULL COMMENT 'TASK_ASSIGNED/TASK_STATUS_CHANGED/MILESTONE_DUE/etc.',
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_task_id BIGINT COMMENT 'Related task if applicable',
    related_project_id VARCHAR(50) COMMENT 'Related project if applicable, references project.project_id',
    is_read TINYINT DEFAULT 0,
    read_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sprint reminder log
CREATE TABLE IF NOT EXISTS sprint_reminder_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    reminder_type VARCHAR(20) NOT NULL COMMENT 'START_REMINDER/END_REMINDER',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sprint (sprint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
