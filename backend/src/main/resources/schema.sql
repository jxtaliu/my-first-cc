-- Create database
CREATE DATABASE IF NOT EXISTS sme_pm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sme_pm;

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
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role table
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

-- Department table
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

-- Project table
CREATE TABLE IF NOT EXISTS project (
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
CREATE TABLE IF NOT EXISTS project_member (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sprint table
CREATE TABLE IF NOT EXISTS sprint (
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
CREATE TABLE IF NOT EXISTS task (
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
CREATE TABLE IF NOT EXISTS timesheet (
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
INSERT INTO sys_role (role_id, name, description) VALUES
('ROLE_001', 'Super Admin', 'Full system access'),
('ROLE_002', 'Department Admin', 'Department level access'),
('ROLE_003', 'Project Admin', 'Project level access'),
('ROLE_004', 'Member', 'Basic member access');

-- Insert default admin user (password: admin123)
INSERT INTO sys_user (user_id, username, password, email, real_name) VALUES
('USR_001', 'admin', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'admin@example.com', 'Admin');

-- Assign SUPER_ADMIN role to admin user
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Insert dictionary types
INSERT INTO sys_dict_type (code, name, description) VALUES
('task_status', 'Task Status', 'Task status options'),
('task_type', 'Task Type', 'Task type options'),
('priority', 'Priority', 'Priority levels'),
('project_type', 'Project Type', 'Project type options'),
('project_status', 'Project Status', 'Project status options');

-- Insert dictionary codes for task_status
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(1, 'TODO', 'Todo', 'Todo', '待办', 1, '{"color": "#909399"}'),
(1, 'IN_PROGRESS', 'In Progress', 'In Progress', '进行中', 2, '{"color": "#E6A23C"}'),
(1, 'IN_REVIEW', 'In Review', 'In Review', '审核中', 3, '{"color": "#409EFF"}'),
(1, 'DONE', 'Done', 'Done', '已完成', 4, '{"color": "#67C23A"}');

-- Insert dictionary codes for task_type
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(2, 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#909399"}'),
(2, 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#409EFF"}'),
(2, 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#67C23A"}'),
(2, 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 4, '{"color": "#E6A23C"}');

-- Insert dictionary codes for priority
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(3, 'LOW', 'Low', 'Low', '低', 1, '{"color": "#909399"}'),
(3, 'MEDIUM', 'Medium', 'Medium', '中', 2, '{"color": "#E6A23C"}'),
(3, 'HIGH', 'High', 'High', '高', 3, '{"color": "#F56C6C"}'),
(3, 'URGENT', 'Urgent', 'Urgent', '紧急', 4, '{"color": "#F56C6C"}');

-- Insert dictionary codes for project_type
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(4, 'SCRUM', 'Scrum', 'Scrum', 'Scrum敏捷', 1, '{"color": "#409EFF"}'),
(4, 'KANBAN', 'Kanban', 'Kanban', '看板', 2, '{"color": "#67C23A"}');

-- Insert dictionary codes for project_status
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(5, 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#909399"}'),
(5, 'ACTIVE', 'Active', 'Active', '进行中', 2, '{"color": "#67C23A"}'),
(5, 'COMPLETED', 'Completed', 'Completed', '已完成', 3, '{"color": "#409EFF"}'),
(5, 'ARCHIVED', 'Archived', 'Archived', '已归档', 4, '{"color": "#909399"}');
