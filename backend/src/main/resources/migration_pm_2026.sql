-- =============================================================================
-- SME PM Enhanced Migration Script
-- 执行顺序: 1. schema.sql  2. migration_pm_2026.sql
-- 注意: 此脚本支持重复执行 (idempotent)
-- =============================================================================

USE sme_pm;

-- =============================================================================
-- 第一部分: 修改已有表 (依赖 schema.sql 中创建的表)
-- 所有列添加使用 idempotent 模式
-- =============================================================================

-- ----------------------------------------
-- 1.1 修改 task 表 - 添加 PM 字段
-- ----------------------------------------
SET @dbname = DATABASE();
SET @tablename = 'task';

-- priority
SET @columnname = 'priority';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN priority VARCHAR(20) DEFAULT ''P2'' COMMENT ''P0/P1/P2/P3'' AFTER status'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- remaining_hours
SET @columnname = 'remaining_hours';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN remaining_hours INT COMMENT ''Remaining work hours'' AFTER actual_hours'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- progress
SET @columnname = 'progress';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN progress INT DEFAULT 0 COMMENT ''Progress percentage 0-100'' AFTER remaining_hours'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- start_date
SET @columnname = 'start_date';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN start_date DATE COMMENT ''Actual start date'' AFTER progress'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- due_date
SET @columnname = 'due_date';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN due_date DATE COMMENT ''Due date'' AFTER start_date'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- in_progress_since
SET @columnname = 'in_progress_since';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN in_progress_since DATETIME COMMENT ''When task entered in_progress status'' AFTER due_date'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- completion_date
SET @columnname = 'completion_date';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN completion_date DATETIME COMMENT ''When task was completed'' AFTER in_progress_since'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- version
SET @columnname = 'version';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN version INT DEFAULT 1 COMMENT ''Optimistic lock'' AFTER completion_date'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- milestone_id
SET @columnname = 'milestone_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE task ADD COLUMN milestone_id BIGINT COMMENT ''Auto-linked from sprint'' AFTER progress'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------
-- 1.2 修改 sprint 表 - 添加 PM 字段
-- ----------------------------------------
SET @tablename = 'sprint';

-- goal
SET @columnname = 'goal';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE sprint ADD COLUMN goal VARCHAR(500) COMMENT ''Sprint goal'' AFTER status'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- capacity_hours
SET @columnname = 'capacity_hours';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE sprint ADD COLUMN capacity_hours INT COMMENT ''Total team capacity in hours'' AFTER goal'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- velocity
SET @columnname = 'velocity';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE sprint ADD COLUMN velocity INT COMMENT ''Story points completed last sprint'' AFTER capacity_hours'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- start_reminder_sent
SET @columnname = 'start_reminder_sent';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE sprint ADD COLUMN start_reminder_sent TINYINT DEFAULT 0 AFTER velocity'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- end_reminder_sent
SET @columnname = 'end_reminder_sent';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE sprint ADD COLUMN end_reminder_sent TINYINT DEFAULT 0 AFTER start_reminder_sent'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- milestone_id
SET @columnname = 'milestone_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE sprint ADD COLUMN milestone_id BIGINT COMMENT ''Associated milestone'' AFTER status'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- 第二部分: 创建新的 PM 表
-- =============================================================================

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

CREATE TABLE IF NOT EXISTS project_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
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

CREATE TABLE IF NOT EXISTS project_milestone (
    project_id BIGINT NOT NULL,
    milestone_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, milestone_id),
    INDEX idx_milestone (milestone_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

CREATE TABLE IF NOT EXISTS sprint_reminder_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    reminder_type VARCHAR(20) NOT NULL COMMENT 'START_REMINDER/END_REMINDER',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sprint (sprint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- 第三部分: 插入字典数据
-- =============================================================================

INSERT IGNORE INTO sys_dict_type (code, name, description) VALUES
('TASK_PRIORITY', 'Task Priority', 'Priority levels for tasks - P0/P1/P2/P3'),
('TASK_TYPE_PM', 'Task Type', 'Task types for PM - EPIC/FEATURE/STORY/TASK/BUG'),
('PROJECT_STATUS_PM', 'Project Status', 'Project lifecycle status'),
('DEPENDENCY_TYPE', 'Dependency Type', 'Task dependency types - FS/SS/FF/SF'),
('NOTIFICATION_TYPE', 'Notification Type', 'Types of notifications in the system'),
('PROJECT_ROLE_PM', 'Project Role', 'Project-level roles'),
('MILESTONE_STATUS', 'Milestone Status', 'Milestone achievement status'),
('SPRINT_STATUS_PM', 'Sprint Status', 'Sprint lifecycle status');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P0', 'P0 - Urgent', 'P0 Urgent', 'P0 紧急', 1, '{"color": "#EF4444", "level": 0}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P1', 'P1 - High', 'P1 High', 'P1 高', 2, '{"color": "#F97316", "level": 1}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P2', 'P2 - Medium', 'P2 Medium', 'P2 中', 3, '{"color": "#FBBF24", "level": 2}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P3', 'P3 - Low', 'P3 Low', 'P3 低', 4, '{"color": "#6B7280", "level": 3}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#8B5CF6", "icon": "📦", "level": 1}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#3B82F6", "icon": "🧩", "level": 2}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#10B981", "icon": "📋", "level": 3}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'TASK', 'Task', 'Task', '任务', 4, '{"color": "#94A3B8", "icon": "✅", "level": 4}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'BUG', 'Bug', 'Bug', '缺陷', 5, '{"color": "#EF4444", "icon": "🐛", "level": 4}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 6, '{"color": "#64748B", "icon": "➗", "level": 4}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'STARTING', 'Starting', 'Starting', '启动中', 2, '{"color": "#3B82F6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'ACTIVE', 'Active', 'Active', '进行中', 3, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'COMPLETED', 'Completed', 'Completed', '已完成', 4, '{"color": "#8B5CF6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'PAUSED', 'Paused', 'Paused', '已暂停', 5, '{"color": "#F59E0B"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'ARCHIVED', 'Archived', 'Archived', '已归档', 6, '{"color": "#64748B"}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'FS', 'Finish to Start', 'Finish-Start', '完成-开始', 1, '{"description": "Task B cannot start until Task A finishes"}'),
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'SS', 'Start to Start', 'Start-Start', '开始-开始', 2, '{"description": "Task B cannot start until Task A starts"}'),
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'FF', 'Finish to Finish', 'Finish-Finish', '完成-完成', 3, '{"description": "Task B cannot finish until Task A finishes"}'),
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'SF', 'Start to Finish', 'Start-Finish', '开始-完成', 4, '{"description": "Task B cannot finish until Task A starts"}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'TASK_ASSIGNED', 'Task Assigned', 'Task Assigned', '任务已分配', 1, '{"icon": "📋"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'TASK_STATUS_CHANGED', 'Task Status Changed', 'Task Status Changed', '任务状态变更', 2, '{"icon": "🔄"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'TASK_COMMENT', 'Task Comment', 'Task Comment', '任务评论', 3, '{"icon": "💬"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'TASK_MENTION', 'Task @Mention', 'Task @Mention', '任务中被@', 4, '{"icon": "👤"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'MILESTONE_DUE', 'Milestone Due', 'Milestone Due', '里程碑到期', 5, '{"icon": "🎯"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'MILESTONE_ACHIEVED', 'Milestone Achieved', 'Milestone Achieved', '里程碑达成', 6, '{"icon": "🏆"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'SPRINT_START', 'Sprint Started', 'Sprint Started', 'Sprint开始', 7, '{"icon": "🚀"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'SPRINT_END', 'Sprint Ending Soon', 'Sprint Ending Soon', 'Sprint即将结束', 8, '{"icon": "⏰"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'SPRINT_COMPLETED', 'Sprint Completed', 'Sprint Completed', 'Sprint完成', 9, '{"icon": "✅"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'TASK_OVERDUE', 'Task Overdue', 'Task Overdue', '任务逾期', 10, '{"icon": "⚠️"}'),
((SELECT id FROM sys_dict_type WHERE code = 'NOTIFICATION_TYPE'), 'TASK_DEPENDENCY_BLOCKED', 'Dependency Blocked', 'Dependency Blocked', '依赖被阻塞', 11, '{"icon": "🚧"}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'PROJECT_OWNER', 'Project Owner', 'Project Owner', '项目所有者', 1, '{"description": "Full project control, can delete project"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'PROJECT_MANAGER', 'Project Manager', 'Project Manager', '项目经理', 2, '{"description": "Manage sprint, assign tasks, approve timesheets"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'DEV_LEAD', 'Development Lead', 'Development Lead', '开发负责人', 3, '{"description": "Lead development work, technical decisions"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'DEVELOPER', 'Developer', 'Developer', '开发者', 4, '{"description": "Normal team member, work on tasks"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'GUEST', 'Guest', 'Guest', '访客', 5, '{"description": "Read-only access to project"}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'ACTIVE', 'Active', 'Active', '进行中', 1, '{"color": "#3B82F6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'ACHIEVED', 'Achieved', 'Achieved', '已达成', 2, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'FAILED', 'Failed', 'Failed', '已失败', 3, '{"color": "#EF4444"}'),
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'CANCELLED', 'Cancelled', 'Cancelled', '已取消', 4, '{"color": "#64748B"}');

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_STATUS_PM'), 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}'),
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_STATUS_PM'), 'ACTIVE', 'Active', 'Active', '进行中', 2, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_STATUS_PM'), 'COMPLETED', 'Completed', 'Completed', '已完成', 3, '{"color": "#8B5CF6"}');

-- =============================================================================
-- 第四部分: 插入 PM 表数据
-- =============================================================================

INSERT IGNORE INTO task_status (project_id, code, name, name_en, name_zh, category, color, sort_order) VALUES
(NULL, 'TODO', 'Todo', 'Todo', '待办', 'todo', '#94A3B8', 1),
(NULL, 'IN_PROGRESS', 'In Progress', 'In Progress', '进行中', 'doing', '#3B82F6', 2),
(NULL, 'DEVELOPMENT', 'Dev Done', 'Development Done', '开发完成', 'doing', '#8B5CF6', 3),
(NULL, 'TESTING', 'Testing', 'Testing', '测试中', 'doing', '#F59E0B', 4),
(NULL, 'DONE', 'Done', 'Done', '已完成', 'done', '#10B981', 5),
(NULL, 'BLOCKED', 'Blocked', 'Blocked', '已阻塞', 'alert', '#EF4444', 6);

INSERT IGNORE INTO status_transition (project_id, from_status_id, to_status_id) VALUES
(NULL, 1, 2), (NULL, 1, 6), (NULL, 2, 1), (NULL, 2, 3), (NULL, 2, 6),
(NULL, 3, 2), (NULL, 3, 4), (NULL, 3, 6), (NULL, 4, 3), (NULL, 4, 5),
(NULL, 4, 6), (NULL, 6, 1), (NULL, 6, 2);

INSERT IGNORE INTO project_template (name, description, sprint_duration, enable_priority, task_types) VALUES
('敏捷开发模板', '标准的Scrum敏捷开发流程，2周Sprint', 14, 1, 'EPIC,FEATURE,STORY,TASK,BUG,SUBTASK'),
('看板模板', '看板模式，无固定Sprint，持续交付', 0, 1, 'EPIC,FEATURE,STORY,TASK,BUG'),
('简单任务模板', '无优先级，适合简单任务管理', 14, 0, 'TASK');
