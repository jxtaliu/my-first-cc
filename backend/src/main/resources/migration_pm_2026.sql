-- =============================================================================
-- SME PM Migration Script - Data Only
-- 执行顺序: 1. schema.sql  2. migration_pm_2026.sql
-- 注意: 此脚本支持重复执行 (idempotent)
-- =============================================================================

USE sme_pm;

-- =============================================================================
-- 第一部分: 系统基础数据
-- =============================================================================

-- Insert default roles
INSERT IGNORE INTO sys_role (role_id, name, description) VALUES
('ROLE_001', 'Super Admin', 'Full system access'),
('ROLE_002', 'Department Admin', 'Department level access'),
('ROLE_003', 'Project Admin', 'Project level access'),
('ROLE_004', 'Member', 'Basic member access');

-- Insert default admin user (password: admin123)
INSERT IGNORE INTO sys_user (user_id, username, password, email, real_name) VALUES
('USR_001', 'admin', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'admin@example.com', 'Admin');

-- Assign SUPER_ADMIN role to admin user
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Insert dictionary types (基础)
INSERT IGNORE INTO sys_dict_type (code, name, description) VALUES
('task_status', 'Task Status', 'Task status options'),
('task_type', 'Task Type', 'Task type options'),
('priority', 'Priority', 'Priority levels'),
('project_type', 'Project Type', 'Project type options'),
('project_status', 'Project Status', 'Project status options');

-- Insert dictionary codes for task_status
INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(1, 'TODO', 'Todo', 'Todo', '待办', 1, '{"color": "#909399"}'),
(1, 'IN_PROGRESS', 'In Progress', 'In Progress', '进行中', 2, '{"color": "#E6A23C"}'),
(1, 'IN_REVIEW', 'In Review', 'In Review', '审核中', 3, '{"color": "#409EFF"}'),
(1, 'DONE', 'Done', 'Done', '已完成', 4, '{"color": "#67C23A"}');

-- Insert dictionary codes for task_type
INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(2, 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#909399"}'),
(2, 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#409EFF"}'),
(2, 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#67C23A"}'),
(2, 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 4, '{"color": "#E6A23C"}');

-- Insert dictionary codes for priority
INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(3, 'LOW', 'Low', 'Low', '低', 1, '{"color": "#909399"}'),
(3, 'MEDIUM', 'Medium', 'Medium', '中', 2, '{"color": "#E6A23C"}'),
(3, 'HIGH', 'High', 'High', '高', 3, '{"color": "#F56C6C"}'),
(3, 'URGENT', 'Urgent', 'Urgent', '紧急', 4, '{"color": "#F56C6C"}');

-- Insert dictionary codes for project_type
INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(4, 'DEVELOPE', 'Develope', 'Develope Project', '研发项目', 1, '{"color": "#409EFF"}'),
(4, 'CUSTOM', 'Custom', 'Custom Project', '客户项目', 2, '{"color": "#67C23A"}');

-- Insert dictionary codes for project_status
INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
(5, 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#909399"}'),
(5, 'ACTIVE', 'Active', 'Active', '进行中', 2, '{"color": "#67C23A"}'),
(5, 'COMPLETED', 'Completed', 'Completed', '已完成', 3, '{"color": "#409EFF"}'),
(5, 'ARCHIVED', 'Archived', 'Archived', '已归档', 4, '{"color": "#909399"}');

-- =============================================================================
-- 第二部分: PM 扩展字典数据
-- =============================================================================

INSERT IGNORE INTO sys_dict_type (code, name, description) VALUES
('TASK_PRIORITY', 'Task Priority', 'Priority levels for tasks - P0/P1/P2/P3'),
('TASK_TYPE_PM', 'Task Type', 'Task types for PM - EPIC/FEATURE/STORY/TASK/BUG'),
('PROJECT_STATUS_PM', 'Project Status', 'Project lifecycle status'),
('DEPENDENCY_TYPE', 'Dependency Type', 'Task dependency types - FS/SS/FF/SF'),
('NOTIFICATION_TYPE', 'Notification Type', 'Types of notifications in the system'),
('PROJECT_ROLE_PM', 'Project Role', 'Project-level roles'),
('MILESTONE_STATUS', 'Milestone Status', 'Milestone achievement status'),
('SPRINT_STATUS_PM', 'Sprint Status', 'Sprint lifecycle status'),
('SPRINT_MODE_PM', 'Sprint Mode', 'Project sprint mode - SCRUM/KANBAN');

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

INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_MODE_PM'), 'SCRUM', 'Scrum', 'Scrum', 'Scrum敏捷', 1, '{"color": "#3B82F6", "description": "Fixed sprint with planning and reviews"}'),
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_MODE_PM'), 'KANBAN', 'Kanban', 'Kanban', '看板', 2, '{"color": "#10B981", "description": "Continuous flow, no fixed sprints"}');

-- =============================================================================
-- 第三部分: PM 表数据
-- =============================================================================

-- Insert default task statuses
INSERT IGNORE INTO task_status (project_id, code, name, name_en, name_zh, category, color, sort_order) VALUES
(NULL, 'TODO', 'Todo', 'Todo', '待办', 'todo', '#94A3B8', 1),
(NULL, 'IN_PROGRESS', 'In Progress', 'In Progress', '进行中', 'doing', '#3B82F6', 2),
(NULL, 'DEVELOPMENT', 'Dev Done', 'Development Done', '开发完成', 'doing', '#8B5CF6', 3),
(NULL, 'TESTING', 'Testing', 'Testing', '测试中', 'doing', '#F59E0B', 4),
(NULL, 'DONE', 'Done', 'Done', '已完成', 'done', '#10B981', 5),
(NULL, 'BLOCKED', 'Blocked', 'Blocked', '已阻塞', 'alert', '#EF4444', 6);

-- Insert default status transitions
INSERT IGNORE INTO status_transition (project_id, from_status_id, to_status_id) VALUES
(NULL, 1, 2), (NULL, 1, 6), (NULL, 2, 1), (NULL, 2, 3), (NULL, 2, 6),
(NULL, 3, 2), (NULL, 3, 4), (NULL, 3, 6), (NULL, 4, 3), (NULL, 4, 5),
(NULL, 4, 6), (NULL, 6, 1), (NULL, 6, 2);

-- Insert project templates
INSERT IGNORE INTO project_template (name, description, sprint_duration, enable_priority, task_types) VALUES
('敏捷开发模板', '标准的Scrum敏捷开发流程，2周Sprint', 14, 1, 'EPIC,FEATURE,STORY,TASK,BUG,SUBTASK'),
('看板模板', '看板模式，无固定Sprint，持续交付', 0, 1, 'EPIC,FEATURE,STORY,TASK,BUG'),
('简单任务模板', '无优先级，适合简单任务管理', 14, 0, 'TASK');
