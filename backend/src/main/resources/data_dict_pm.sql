-- SME PM Data Dictionary Initialization
-- Project Management Module Data

USE sme_pm;

-- ============================================
-- Add Dict Types for PM
-- ============================================
INSERT INTO sys_dict_type (code, name, description) VALUES
('TASK_PRIORITY', 'Task Priority', 'Priority levels for tasks - P0/P1/P2/P3'),
('TASK_TYPE_PM', 'Task Type', 'Task types for PM - EPIC/FEATURE/STORY/TASK/BUG'),
('PROJECT_STATUS_PM', 'Project Status', 'Project lifecycle status'),
('DEPENDENCY_TYPE', 'Dependency Type', 'Task dependency types - FS/SS/FF/SF'),
('NOTIFICATION_TYPE', 'Notification Type', 'Types of notifications in the system'),
('PROJECT_ROLE_PM', 'Project Role', 'Project-level roles'),
('MILESTONE_STATUS', 'Milestone Status', 'Milestone achievement status'),
('SPRINT_STATUS_PM', 'Sprint Status', 'Sprint lifecycle status');

-- ============================================
-- Task Priority (dict_type_id for TASK_PRIORITY)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P0', 'P0 - Urgent', 'P0 Urgent', 'P0 紧急', 1, '{"color": "#EF4444", "level": 0}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P1', 'P1 - High', 'P1 High', 'P1 高', 2, '{"color": "#F97316", "level": 1}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P2', 'P2 - Medium', 'P2 Medium', 'P2 中', 3, '{"color": "#FBBF24", "level": 2}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_PRIORITY'), 'P3', 'P3 - Low', 'P3 Low', 'P3 低', 4, '{"color": "#6B7280", "level": 3}');

-- ============================================
-- Task Types for PM (dict_type_id for TASK_TYPE_PM)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#8B5CF6", "icon": "📦", "level": 1}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#3B82F6", "icon": "🧩", "level": 2}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#10B981", "icon": "📋", "level": 3}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'TASK', 'Task', 'Task', '任务', 4, '{"color": "#94A3B8", "icon": "✅", "level": 4}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'BUG', 'Bug', 'Bug', '缺陷', 5, '{"color": "#EF4444", "icon": "🐛", "level": 4}'),
((SELECT id FROM sys_dict_type WHERE code = 'TASK_TYPE_PM'), 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 6, '{"color": "#64748B", "icon": "➗", "level": 4}');

-- ============================================
-- Project Status (dict_type_id for PROJECT_STATUS_PM)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'STARTING', 'Starting', 'Starting', '启动中', 2, '{"color": "#3B82F6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'ACTIVE', 'Active', 'Active', '进行中', 3, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'COMPLETED', 'Completed', 'Completed', '已完成', 4, '{"color": "#8B5CF6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'PAUSED', 'Paused', 'Paused', '已暂停', 5, '{"color": "#F59E0B"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_STATUS_PM'), 'ARCHIVED', 'Archived', 'Archived', '已归档', 6, '{"color": "#64748B"}');

-- ============================================
-- Dependency Types (dict_type_id for DEPENDENCY_TYPE)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'FS', 'Finish to Start', 'Finish-Start', '完成-开始', 1, '{"description": "Task B cannot start until Task A finishes"}'),
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'SS', 'Start to Start', 'Start-Start', '开始-开始', 2, '{"description": "Task B cannot start until Task A starts"}'),
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'FF', 'Finish to Finish', 'Finish-Finish', '完成-完成', 3, '{"description": "Task B cannot finish until Task A finishes"}'),
((SELECT id FROM sys_dict_type WHERE code = 'DEPENDENCY_TYPE'), 'SF', 'Start to Finish', 'Start-Finish', '开始-完成', 4, '{"description": "Task B cannot finish until Task A starts"}');

-- ============================================
-- Notification Types (dict_type_id for NOTIFICATION_TYPE)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
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

-- ============================================
-- Project Roles (dict_type_id for PROJECT_ROLE_PM)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'PROJECT_OWNER', 'Project Owner', 'Project Owner', '项目所有者', 1, '{"description": "Full project control, can delete project"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'PROJECT_MANAGER', 'Project Manager', 'Project Manager', '项目经理', 2, '{"description": "Manage sprint, assign tasks, approve timesheets"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'DEV_LEAD', 'Development Lead', 'Development Lead', '开发负责人', 3, '{"description": "Lead development work, technical decisions"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'DEVELOPER', 'Developer', 'Developer', '开发者', 4, '{"description": "Normal team member, work on tasks"}'),
((SELECT id FROM sys_dict_type WHERE code = 'PROJECT_ROLE_PM'), 'GUEST', 'Guest', 'Guest', '访客', 5, '{"description": "Read-only access to project"}');

-- ============================================
-- Milestone Status (dict_type_id for MILESTONE_STATUS)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'ACTIVE', 'Active', 'Active', '进行中', 1, '{"color": "#3B82F6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'ACHIEVED', 'Achieved', 'Achieved', '已达成', 2, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'FAILED', 'Failed', 'Failed', '已失败', 3, '{"color": "#EF4444"}'),
((SELECT id FROM sys_dict_type WHERE code = 'MILESTONE_STATUS'), 'CANCELLED', 'Cancelled', 'Cancelled', '已取消', 4, '{"color": "#64748B"}');

-- ============================================
-- Sprint Status (dict_type_id for SPRINT_STATUS_PM)
-- ============================================
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_STATUS_PM'), 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}'),
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_STATUS_PM'), 'ACTIVE', 'Active', 'Active', '进行中', 2, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'SPRINT_STATUS_PM'), 'COMPLETED', 'Completed', 'Completed', '已完成', 3, '{"color": "#8B5CF6"}');
