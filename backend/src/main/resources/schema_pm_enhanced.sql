-- SME PM Enhanced Schema
-- Project Management Module

USE sme_pm;

-- ============================================
-- 1. Project Template
-- ============================================
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

-- ============================================
-- 2. Project Role (within project)
-- ============================================
CREATE TABLE IF NOT EXISTS project_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL COMMENT 'PROJECT_OWNER/PROJECT_MANAGER/DEV_LEAD/DEVELOPER/GUEST',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_project_user (project_id, user_id),
    INDEX idx_project (project_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 3. Task Status (Customizable)
-- ============================================
CREATE TABLE IF NOT EXISTS task_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT COMMENT 'NULL means system default',
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

-- ============================================
-- 4. Status Transition Rules
-- ============================================
CREATE TABLE IF NOT EXISTS status_transition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT COMMENT 'NULL means system default',
    from_status_id BIGINT NOT NULL COMMENT 'Source status',
    to_status_id BIGINT NOT NULL COMMENT 'Target status',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_from (from_status_id),
    INDEX idx_to (to_status_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 5. Task Dependency
-- ============================================
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

-- ============================================
-- 6. Task Enhancement
-- ============================================
ALTER TABLE task
ADD COLUMN priority VARCHAR(20) DEFAULT 'P2' COMMENT 'P0/P1/P2/P3' AFTER status,
ADD COLUMN remaining_hours INT COMMENT 'Remaining work hours' AFTER actual_hours,
ADD COLUMN progress INT DEFAULT 0 COMMENT 'Progress percentage 0-100' AFTER remaining_hours,
ADD COLUMN start_date DATE COMMENT 'Actual start date' AFTER progress,
ADD COLUMN due_date DATE COMMENT 'Due date' AFTER start_date,
ADD COLUMN in_progress_since DATETIME COMMENT 'When task entered in_progress status' AFTER due_date,
ADD COLUMN completion_date DATETIME COMMENT 'When task was completed' AFTER in_progress_since,
ADD COLUMN version INT DEFAULT 1 COMMENT 'Optimistic lock' AFTER completion_date;

-- ============================================
-- 7. Task Attachment
-- ============================================
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

-- ============================================
-- 8. Task Comment
-- ============================================
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

-- ============================================
-- 9. Milestone
-- ============================================
CREATE TABLE IF NOT EXISTS milestone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    target_date DATE NOT NULL,
    is_cross_project TINYINT DEFAULT 0 COMMENT 'Cross-project milestone',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/ACHIEVED/FAILED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_target_date (target_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 10. Project Milestone Relation
-- ============================================
CREATE TABLE IF NOT EXISTS project_milestone (
    project_id BIGINT NOT NULL,
    milestone_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, milestone_id),
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 11. Sprint Enhancement
-- ============================================
ALTER TABLE sprint
ADD COLUMN goal VARCHAR(500) COMMENT 'Sprint goal' AFTER status,
ADD COLUMN capacity_hours INT COMMENT 'Total team capacity in hours' AFTER goal,
ADD COLUMN velocity INT COMMENT 'Story points completed last sprint' AFTER capacity_hours,
ADD COLUMN start_reminder_sent TINYINT DEFAULT 0 AFTER velocity,
ADD COLUMN end_reminder_sent TINYINT DEFAULT 0 AFTER start_reminder_sent;

-- ============================================
-- 12. Notification
-- ============================================
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Recipient',
    type VARCHAR(50) NOT NULL COMMENT 'TASK_ASSIGNED/TASK_STATUS_CHANGED/MILESTONE_DUE/etc.',
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_task_id BIGINT COMMENT 'Related task if applicable',
    related_project_id BIGINT COMMENT 'Related project if applicable',
    is_read TINYINT DEFAULT 0,
    read_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 13. Sprint Reminder Log
-- ============================================
CREATE TABLE IF NOT EXISTS sprint_reminder_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    reminder_type VARCHAR(20) NOT NULL COMMENT 'START_REMINDER/END_REMINDER',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sprint (sprint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Insert Default Data: System Task Statuses
-- ============================================
INSERT INTO task_status (id, project_id, code, name, name_en, name_zh, category, color, sort_order) VALUES
(NULL, NULL, 'TODO', 'Todo', 'Todo', '待办', 'todo', '#94A3B8', 1),
(NULL, NULL, 'IN_PROGRESS', 'In Progress', 'In Progress', '进行中', 'doing', '#3B82F6', 2),
(NULL, NULL, 'DEVELOPMENT', 'Dev Done', 'Development Done', '开发完成', 'doing', '#8B5CF6', 3),
(NULL, NULL, 'TESTING', 'Testing', 'Testing', '测试中', 'doing', '#F59E0B', 4),
(NULL, NULL, 'DONE', 'Done', 'Done', '已完成', 'done', '#10B981', 5),
(NULL, NULL, 'BLOCKED', 'Blocked', 'Blocked', '已阻塞', 'alert', '#EF4444', 6);

-- ============================================
-- Insert Default Data: Status Transitions
-- ============================================
INSERT INTO status_transition (id, project_id, from_status_id, to_status_id) VALUES
-- TODO transitions
(1, NULL, 1, 2),  -- TODO -> IN_PROGRESS
(2, NULL, 1, 6),   -- TODO -> BLOCKED
-- IN_PROGRESS transitions
(3, NULL, 2, 1),   -- IN_PROGRESS -> TODO
(4, NULL, 2, 3),   -- IN_PROGRESS -> DEVELOPMENT
(5, NULL, 2, 6),   -- IN_PROGRESS -> BLOCKED
-- DEVELOPMENT transitions
(6, NULL, 3, 2),   -- DEVELOPMENT -> IN_PROGRESS
(7, NULL, 3, 4),   -- DEVELOPMENT -> TESTING
(8, NULL, 3, 6),   -- DEVELOPMENT -> BLOCKED
-- TESTING transitions
(9, NULL, 4, 3),   -- TESTING -> DEVELOPMENT
(10, NULL, 4, 5),  -- TESTING -> DONE
(11, NULL, 4, 6),  -- TESTING -> BLOCKED
-- BLOCKED transitions
(12, NULL, 6, 1),  -- BLOCKED -> TODO
(13, NULL, 6, 2);   -- BLOCKED -> IN_PROGRESS

-- ============================================
-- Insert Default Data: Priority
-- ============================================
DELETE FROM sys_dict_code WHERE dict_type_id = 3;
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(3, 'P0', 'P0 - Urgent', 'P0 Urgent', 'P0 紧急', 1, '{"color": "#EF4444", "level": 0}'),
(3, 'P1', 'P1 - High', 'P1 High', 'P1 高', 2, '{"color": "#F97316", "level": 1}'),
(3, 'P2', 'P2 - Medium', 'P2 Medium', 'P2 中', 3, '{"color": "#FBBF24", "level": 2}'),
(3, 'P3', 'P3 - Low', 'P3 Low', 'P3 低', 4, '{"color": "#6B7280", "level": 3}');

-- ============================================
-- Insert Default Data: Task Types
-- ============================================
DELETE FROM sys_dict_code WHERE dict_type_id = 2;
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(2, 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#8B5CF6", "icon": "📦", "level": 1}'),
(2, 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#3B82F6", "icon": "🧩", "level": 2}'),
(2, 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#10B981", "icon": "📋", "level": 3}'),
(2, 'TASK', 'Task', 'Task', '任务', 4, '{"color": "#94A3B8", "icon": "✅", "level": 4}'),
(2, 'BUG', 'Bug', 'Bug', '缺陷', 5, '{"color": "#EF4444", "icon": "🐛", "level": 4}'),
(2, 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 6, '{"color": "#64748B", "icon": "➗", "level": 4}');

-- ============================================
-- Insert Default Data: Project Status
-- ============================================
DELETE FROM sys_dict_code WHERE dict_type_id = 5;
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(5, 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}'),
(5, 'STARTING', 'Starting', 'Starting', '启动中', 2, '{"color": "#3B82F6"}'),
(5, 'ACTIVE', 'Active', 'Active', '进行中', 3, '{"color": "#10B981"}'),
(5, 'COMPLETED', 'Completed', 'Completed', '已完成', 4, '{"color": "#8B5CF6"}'),
(5, 'PAUSED', 'Paused', 'Paused', '已暂停', 5, '{"color": "#F59E0B"}'),
(5, 'ARCHIVED', 'Archived', 'Archived', '已归档', 6, '{"color": "#64748B"}');

-- ============================================
-- Insert Default Data: Project Templates
-- ============================================
INSERT INTO project_template (name, description, sprint_duration, enable_priority, task_types) VALUES
('敏捷开发模板', '标准的Scrum敏捷开发流程，2周Sprint', 14, 1, 'EPIC,FEATURE,STORY,TASK,BUG,SUBTASK'),
('看板模板', '看板模式，无固定Sprint，持续交付', 0, 1, 'EPIC,FEATURE,STORY,TASK,BUG'),
('简单任务模板', '无优先级，适合简单任务管理', 14, 0, 'TASK');

-- ============================================
-- Insert Default Data: Project Roles
-- ============================================
-- Note: PROJECT_OWNER, PROJECT_MANAGER, DEV_LEAD, DEVELOPER, GUEST
