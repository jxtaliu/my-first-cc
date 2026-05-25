-- =============================================================================
-- SME PM Migration Script - Data Only
-- 执行顺序: 1. schema.sql  2. migration_pm_2026.sql
-- 注意: 此脚本支持重复执行 (idempotent)
-- =============================================================================

USE sme_pm;

-- =============================================================================
-- 第一部分: 系统基础数据
-- =============================================================================

-- =============================================================================
-- 系统角色 (TRUNCATE后重新插入)
-- =============================================================================
TRUNCATE TABLE sys_role;

INSERT INTO sys_role (role_id, name, description) VALUES
('ROLE_SUPER_ADMIN', '超级管理员', '系统最高权限'),
('ROLE_DEPT_ADMIN', '部门管理员', '管理部门用户和资源'),
('ROLE_PM', '项目经理', '管理项目全生命周期'),
('ROLE_DEV_LEAD', '开发组长', '技术负责人'),
('ROLE_DEVELOPER', '开发人员', '执行任务'),
('ROLE_TESTER', '测试工程师', '测试验证'),
('ROLE_GUEST', '访客', '只读访问');

-- =============================================================================
-- 部门数据 (8个部门)
-- =============================================================================
TRUNCATE TABLE sys_department;

INSERT INTO sys_department (department_id, name, parent_id, sort_order, status) VALUES
('DEPT001', '技术部', NULL, 1, 1),
('DEPT002', '产品部', NULL, 2, 1),
('DEPT003', '设计部', NULL, 3, 1),
('DEPT004', '市场部', NULL, 4, 1),
('DEPT005', '行政部', NULL, 5, 1),
('DEPT006', '开发组', 1, 11, 1),
('DEPT007', '测试组', 1, 12, 1),
('DEPT008', '运维组', 1, 13, 1);

-- =============================================================================
-- 用户数据 (20个用户)
-- 密码统一为: PM123456 (BCrypt加密后的值)
-- 注意: department_id 使用数字ID (部门表的自增ID)
-- =============================================================================
TRUNCATE TABLE sys_user;

INSERT INTO sys_user (user_id, username, password, email, real_name, status, department_id) VALUES
-- 超级管理员
('USR001', 'admin', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'admin@company.com', '系统管理员', 1, NULL),

-- 技术部 (id=1)
('USR002', 'zhangwei', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'zhang.wei@company.com', '张伟', 1, 1),
('USR003', 'lina', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'li.na@company.com', '李娜', 1, 1),

-- 开发组 (id=6)
('USR004', 'wangfang', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'wang.fang@company.com', '王芳', 1, 6),
('USR005', 'zhaoming', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'zhao.ming@company.com', '赵明', 1, 6),
('USR006', 'sunhui', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'sun.hui@company.com', '孙辉', 1, 6),
('USR007', 'zhoujing', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'zhou.jing@company.com', '周静', 1, 6),

-- 测试组 (id=7)
('USR008', 'wuyong', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'wu.yong@company.com', '吴勇', 1, 7),
('USR009', 'zhengli', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'zheng.li@company.com', '郑丽', 1, 7),
('USR010', 'chenhao', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'chen.hao@company.com', '陈浩', 1, 7),

-- 运维组 (id=8)
('USR011', 'liuqy', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'liu.qy@company.com', '刘强', 1, 8),

-- 产品部 (id=2)
('USR012', 'xiaoli', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'xiao.li@company.com', '肖丽', 1, 2),
('USR013', 'huangbp', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'huang.bp@company.com', '黄宝平', 1, 2),
('USR014', 'guxf', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'gu.xf@company.com', '顾晓峰', 1, 2),

-- 设计部 (id=3)
('USR015', 'liuyt', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'liu.yt@company.com', '刘雨婷', 1, 3),
('USR016', 'mayan', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'ma.yan@company.com', '马艳', 1, 3),

-- 市场部 (id=4)
('USR017', 'zhangss', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'zhang.ss@company.com', '张姗姗', 1, 4),
('USR018', 'lidong', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'li.dong@company.com', '李东', 1, 4),

-- 行政部 (id=5)
('USR019', 'wangxy', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'wang.xy@company.com', '王晓燕', 1, 5),
('USR020', 'zhaoyy', '$2b$10$1QOu23c6LRlOVbyHtd6QJexktUnaUuhC8Pq2HOy1X0WSD0pNC7.DG', 'zhao.yy@company.com', '赵圆圆', 1, 5);

-- =============================================================================
-- 用户角色分配
-- =============================================================================
TRUNCATE TABLE sys_user_role;

-- 管理员 -> 超级管理员
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_id = 'ROLE_SUPER_ADMIN';

-- 技术部经理 -> 部门管理员
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'zhangwei' AND r.role_id = 'ROLE_DEPT_ADMIN';

-- 产品部经理 -> 项目经理
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'xiaoli' AND r.role_id = 'ROLE_PM';

-- 开发组长
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'zhaoming' AND r.role_id = 'ROLE_DEV_LEAD';

-- 开发人员
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username IN ('wangfang', 'sunhui', 'zhoujing') AND r.role_id = 'ROLE_DEVELOPER';

-- 测试工程师
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username IN ('wuyong', 'zhengli', 'chenhao') AND r.role_id = 'ROLE_TESTER';

-- Insert dictionary types (基础)
INSERT INTO sys_dict_type (code, name, description) VALUES
('task_status', 'Task Status', 'Task status options'),
('task_type', 'Task Type', 'Task type options'),
('priority', 'Priority', 'Priority levels'),
('project_type', 'Project Type', 'Project type options'),
('project_status', 'Project Status', 'Project status options')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- Insert dictionary codes for task_status
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TODO', 'Todo', 'Todo', '待办', 1, '{"color": "#909399"}' FROM sys_dict_type t WHERE t.code = 'task_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'IN_PROGRESS', 'In Progress', 'In Progress', '进行中', 2, '{"color": "#E6A23C"}' FROM sys_dict_type t WHERE t.code = 'task_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'IN_REVIEW', 'In Review', 'In Review', '审核中', 3, '{"color": "#409EFF"}' FROM sys_dict_type t WHERE t.code = 'task_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'DONE', 'Done', 'Done', '已完成', 4, '{"color": "#67C23A"}' FROM sys_dict_type t WHERE t.code = 'task_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- Insert dictionary codes for task_type
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#8B5CF6"}' FROM sys_dict_type t WHERE t.code = 'task_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#3B82F6"}' FROM sys_dict_type t WHERE t.code = 'task_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#10B981"}' FROM sys_dict_type t WHERE t.code = 'task_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 4, '{"color": "#64748B"}' FROM sys_dict_type t WHERE t.code = 'task_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- Insert dictionary codes for priority
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'LOW', 'Low', 'Low', '低', 1, '{"color": "#909399"}' FROM sys_dict_type t WHERE t.code = 'priority'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'MEDIUM', 'Medium', 'Medium', '中', 2, '{"color": "#E6A23C"}' FROM sys_dict_type t WHERE t.code = 'priority'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'HIGH', 'High', 'High', '高', 3, '{"color": "#F56C6C"}' FROM sys_dict_type t WHERE t.code = 'priority'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'URGENT', 'Urgent', 'Urgent', '紧急', 4, '{"color": "#F56C6C"}' FROM sys_dict_type t WHERE t.code = 'priority'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- Insert dictionary codes for project_type
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'DEVELOPE', 'Develope', 'Develope Project', '研发项目', 1, '{"color": "#409EFF"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'CUSTOM', 'Custom', 'Custom Project', '客户项目', 2, '{"color": "#67C23A"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ERP', 'ERP', 'ERP System', 'ERP系统', 3, '{"color": "#6366F1"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'MOBILE', 'Mobile', 'Mobile App', '移动应用', 4, '{"color": "#EC4899"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'DATA_ANALYTICS', 'Data Analytics', 'Data Analytics', '数据分析', 5, '{"color": "#14B8A6"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'WEB', 'Web', 'Web Application', 'Web应用', 6, '{"color": "#3B82F6"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'CRM', 'CRM', 'Customer Relationship', '客户关系管理', 7, '{"color": "#F59E0B"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'INFRASTRUCTURE', 'Infra', 'Infrastructure', '基础设施', 8, '{"color": "#8B5CF6"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'REPORTING', 'Reporting', 'Reporting System', '报表系统', 9, '{"color": "#10B981"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'DOCUMENT', 'Document', 'Document Management', '文档管理', 10, '{"color": "#64748B"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'INTEGRATION', 'Integration', 'System Integration', '系统集成', 11, '{"color": "#EF4444"}' FROM sys_dict_type t WHERE t.code = 'project_type'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- Insert dictionary codes for project_status
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}' FROM sys_dict_type t WHERE t.code = 'project_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ACTIVE', 'Active', 'Active', '进行中', 2, '{"color": "#67C23A"}' FROM sys_dict_type t WHERE t.code = 'project_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'COMPLETED', 'Completed', 'Completed', '已完成', 3, '{"color": "#409EFF"}' FROM sys_dict_type t WHERE t.code = 'project_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ARCHIVED', 'Archived', 'Archived', '已归档', 4, '{"color": "#909399"}' FROM sys_dict_type t WHERE t.code = 'project_status'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- =============================================================================
-- 第二部分: PM 扩展字典数据
-- =============================================================================

INSERT INTO sys_dict_type (code, name, description) VALUES
('TASK_PRIORITY', 'Task Priority', 'Priority levels for tasks - P0/P1/P2/P3'),
('TASK_TYPE_PM', 'Task Type', 'Task types for PM - EPIC/FEATURE/STORY/TASK/BUG'),
('PROJECT_STATUS_PM', 'Project Status', 'Project lifecycle status'),
('DEPENDENCY_TYPE', 'Dependency Type', 'Task dependency types - FS/SS/FF/SF'),
('NOTIFICATION_TYPE', 'Notification Type', 'Types of notifications in the system'),
('PROJECT_ROLE_PM', 'Project Role', 'Project-level roles'),
('MILESTONE_STATUS', 'Milestone Status', 'Milestone achievement status'),
('SPRINT_STATUS_PM', 'Sprint Status', 'Sprint lifecycle status'),
('SPRINT_MODE_PM', 'Sprint Mode', 'Project sprint mode - SCRUM/KANBAN')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- TASK_PRIORITY codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'P0', 'P0 - Urgent', 'P0 Urgent', 'P0 紧急', 1, '{"color": "#EF4444", "level": 0}' FROM sys_dict_type t WHERE t.code = 'TASK_PRIORITY'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'P1', 'P1 - High', 'P1 High', 'P1 高', 2, '{"color": "#F97316", "level": 1}' FROM sys_dict_type t WHERE t.code = 'TASK_PRIORITY'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'P2', 'P2 - Medium', 'P2 Medium', 'P2 中', 3, '{"color": "#FBBF24", "level": 2}' FROM sys_dict_type t WHERE t.code = 'TASK_PRIORITY'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'P3', 'P3 - Low', 'P3 Low', 'P3 低', 4, '{"color": "#6B7280", "level": 3}' FROM sys_dict_type t WHERE t.code = 'TASK_PRIORITY'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- TASK_TYPE_PM codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'EPIC', 'Epic', 'Epic', '史诗', 1, '{"color": "#8B5CF6", "icon": "📦", "level": 1}' FROM sys_dict_type t WHERE t.code = 'TASK_TYPE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'FEATURE', 'Feature', 'Feature', '特性', 2, '{"color": "#3B82F6", "icon": "🧩", "level": 2}' FROM sys_dict_type t WHERE t.code = 'TASK_TYPE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'STORY', 'Story', 'Story', '故事', 3, '{"color": "#10B981", "icon": "📋", "level": 3}' FROM sys_dict_type t WHERE t.code = 'TASK_TYPE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK', 'Task', 'Task', '任务', 4, '{"color": "#94A3B8", "icon": "✅", "level": 4}' FROM sys_dict_type t WHERE t.code = 'TASK_TYPE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'BUG', 'Bug', 'Bug', '缺陷', 5, '{"color": "#EF4444", "icon": "🐛", "level": 4}' FROM sys_dict_type t WHERE t.code = 'TASK_TYPE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SUBTASK', 'Sub-task', 'Sub-task', '子任务', 6, '{"color": "#64748B", "icon": "➗", "level": 4}' FROM sys_dict_type t WHERE t.code = 'TASK_TYPE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- PROJECT_STATUS_PM codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'STARTING', 'Starting', 'Starting', '启动中', 2, '{"color": "#3B82F6"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ACTIVE', 'Active', 'Active', '进行中', 3, '{"color": "#10B981"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'COMPLETED', 'Completed', 'Completed', '已完成', 4, '{"color": "#8B5CF6"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'PAUSED', 'Paused', 'Paused', '已暂停', 5, '{"color": "#F59E0B"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ARCHIVED', 'Archived', 'Archived', '已归档', 6, '{"color": "#64748B"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- DEPENDENCY_TYPE codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'FS', 'Finish to Start', 'Finish-Start', '完成-开始', 1, '{"description": "Task B cannot start until Task A finishes"}' FROM sys_dict_type t WHERE t.code = 'DEPENDENCY_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SS', 'Start to Start', 'Start-Start', '开始-开始', 2, '{"description": "Task B cannot start until Task A starts"}' FROM sys_dict_type t WHERE t.code = 'DEPENDENCY_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'FF', 'Finish to Finish', 'Finish-Finish', '完成-完成', 3, '{"description": "Task B cannot finish until Task A finishes"}' FROM sys_dict_type t WHERE t.code = 'DEPENDENCY_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SF', 'Start to Finish', 'Start-Finish', '开始-完成', 4, '{"description": "Task B cannot finish until Task A starts"}' FROM sys_dict_type t WHERE t.code = 'DEPENDENCY_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- NOTIFICATION_TYPE codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK_ASSIGNED', 'Task Assigned', 'Task Assigned', '任务已分配', 1, '{"icon": "📋"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK_STATUS_CHANGED', 'Task Status Changed', 'Task Status Changed', '任务状态变更', 2, '{"icon": "🔄"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK_COMMENT', 'Task Comment', 'Task Comment', '任务评论', 3, '{"icon": "💬"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK_MENTION', 'Task @Mention', 'Task @Mention', '任务中被@', 4, '{"icon": "👤"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'MILESTONE_DUE', 'Milestone Due', 'Milestone Due', '里程碑到期', 5, '{"icon": "🎯"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'MILESTONE_ACHIEVED', 'Milestone Achieved', 'Milestone Achieved', '里程碑达成', 6, '{"icon": "🏆"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SPRINT_START', 'Sprint Started', 'Sprint Started', 'Sprint开始', 7, '{"icon": "🚀"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SPRINT_END', 'Sprint Ending Soon', 'Sprint Ending Soon', 'Sprint即将结束', 8, '{"icon": "⏰"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SPRINT_COMPLETED', 'Sprint Completed', 'Sprint Completed', 'Sprint完成', 9, '{"icon": "✅"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK_OVERDUE', 'Task Overdue', 'Task Overdue', '任务逾期', 10, '{"icon": "⚠️"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'TASK_DEPENDENCY_BLOCKED', 'Dependency Blocked', 'Dependency Blocked', '依赖被阻塞', 11, '{"icon": "🚧"}' FROM sys_dict_type t WHERE t.code = 'NOTIFICATION_TYPE'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- PROJECT_ROLE_PM codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'PROJECT_OWNER', 'Project Owner', 'Project Owner', '项目所有者', 1, '{"description": "Full project control, can delete project"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_ROLE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'PROJECT_MANAGER', 'Project Manager', 'Project Manager', '项目经理', 2, '{"description": "Manage sprint, assign tasks, approve timesheets"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_ROLE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'DEV_LEAD', 'Development Lead', 'Development Lead', '开发负责人', 3, '{"description": "Lead development work, technical decisions"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_ROLE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'DEVELOPER', 'Developer', 'Developer', '开发者', 4, '{"description": "Normal team member, work on tasks"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_ROLE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'GUEST', 'Guest', 'Guest', '访客', 5, '{"description": "Read-only access to project"}' FROM sys_dict_type t WHERE t.code = 'PROJECT_ROLE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- MILESTONE_STATUS codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ACTIVE', 'Active', 'Active', '进行中', 1, '{"color": "#3B82F6"}' FROM sys_dict_type t WHERE t.code = 'MILESTONE_STATUS'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ACHIEVED', 'Achieved', 'Achieved', '已达成', 2, '{"color": "#10B981"}' FROM sys_dict_type t WHERE t.code = 'MILESTONE_STATUS'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'FAILED', 'Failed', 'Failed', '已失败', 3, '{"color": "#EF4444"}' FROM sys_dict_type t WHERE t.code = 'MILESTONE_STATUS'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'CANCELLED', 'Cancelled', 'Cancelled', '已取消', 4, '{"color": "#64748B"}' FROM sys_dict_type t WHERE t.code = 'MILESTONE_STATUS'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- SPRINT_STATUS_PM codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'PLANNING', 'Planning', 'Planning', '规划中', 1, '{"color": "#94A3B8"}' FROM sys_dict_type t WHERE t.code = 'SPRINT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'ACTIVE', 'Active', 'Active', '进行中', 2, '{"color": "#10B981"}' FROM sys_dict_type t WHERE t.code = 'SPRINT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'COMPLETED', 'Completed', 'Completed', '已完成', 3, '{"color": "#8B5CF6"}' FROM sys_dict_type t WHERE t.code = 'SPRINT_STATUS_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- SPRINT_MODE_PM codes
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'SCRUM', 'Scrum', 'Scrum', 'Scrum敏捷', 1, '{"color": "#3B82F6", "description": "Fixed sprint with planning and reviews"}' FROM sys_dict_type t WHERE t.code = 'SPRINT_MODE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);
INSERT INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra)
SELECT t.id, 'KANBAN', 'Kanban', 'Kanban', '看板', 2, '{"color": "#10B981", "description": "Continuous flow, no fixed sprints"}' FROM sys_dict_type t WHERE t.code = 'SPRINT_MODE_PM'
ON DUPLICATE KEY UPDATE name = VALUES(name), name_en = VALUES(name_en), name_zh = VALUES(name_zh), sort_order = VALUES(sort_order), extra = VALUES(extra);

-- =============================================================================
-- 第三部分: PM 表数据
-- =============================================================================

-- Insert project templates
INSERT IGNORE INTO project_template (name, description, sprint_duration, enable_priority, task_types) VALUES
('敏捷开发模板', '标准的Scrum敏捷开发流程，2周Sprint', 14, 1, 'EPIC,FEATURE,STORY,TASK,BUG,SUBTASK'),
('看板模板', '看板模式，无固定Sprint，持续交付', 0, 1, 'EPIC,FEATURE,STORY,TASK,BUG'),
('简单任务模板', '无优先级，适合简单任务管理', 14, 0, 'TASK');

-- =============================================================================
-- 第四部分: 初始项目数据
-- =============================================================================

-- 先添加更多项目类型
INSERT IGNORE INTO sys_dict_code (dict_type_id, code, name, name_en, name_zh, sort_order, extra) VALUES
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'ERP', 'ERP', 'ERP System', 'ERP系统', 3, '{"color": "#6366F1"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'MOBILE', 'Mobile', 'Mobile App', '移动应用', 4, '{"color": "#EC4899"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'DATA_ANALYTICS', 'Data Analytics', 'Data Analytics', '数据分析', 5, '{"color": "#14B8A6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'WEB', 'Web', 'Web Application', 'Web应用', 6, '{"color": "#3B82F6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'CRM', 'CRM', 'Customer Relationship', '客户关系管理', 7, '{"color": "#F59E0B"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'INFRASTRUCTURE', 'Infra', 'Infrastructure', '基础设施', 8, '{"color": "#8B5CF6"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'REPORTING', 'Reporting', 'Reporting System', '报表系统', 9, '{"color": "#10B981"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'DOCUMENT', 'Document', 'Document Management', '文档管理', 10, '{"color": "#64748B"}'),
((SELECT id FROM sys_dict_type WHERE code = 'project_type'), 'INTEGRATION', 'Integration', 'System Integration', '系统集成', 11, '{"color": "#EF4444"}');

-- 插入10个初始项目
INSERT IGNORE INTO project (project_id, name, description, project_type, status, sprint_mode, owner_id, created_at) VALUES
('PRJ001', '企业内部管理系统', '建设集团级ERP系统，整合财务、采购、库存等模块', 'ERP', 'PLANNING', 'SCRUM', 1, NOW()),
('PRJ002', '移动端App升级项目', 'iOS/Android双平台App全新设计开发', 'MOBILE', 'PLANNING', 'KANBAN', 1, NOW()),
('PRJ003', '数据分析平台', '构建企业级数据仓库和BI报表平台', 'DATA_ANALYTICS', 'STARTING', 'SCRUM', 1, NOW()),
('PRJ004', '电商网站重构', '对现有电商平台进行微服务架构重构', 'WEB', 'ACTIVE', 'SCRUM', 1, NOW()),
('PRJ005', '客户关系管理系统', '全新CRM系统，支持销售流程和客户跟进', 'CRM', 'ACTIVE', 'KANBAN', 1, NOW()),
('PRJ006', '自动化测试框架', '搭建CI/CD自动化测试体系', 'INFRASTRUCTURE', 'ACTIVE', 'SCRUM', 1, NOW()),
('PRJ007', '财务报表系统', '月度季度财务报表自动生成系统', 'REPORTING', 'COMPLETED', 'SCRUM', 1, NOW()),
('PRJ008', '文档管理系统', '企业级文档存储、检索、权限管理', 'DOCUMENT', 'COMPLETED', 'KANBAN', 1, NOW()),
('PRJ009', '第三方支付集成', '对接支付宝、微信支付、银联', 'INTEGRATION', 'PAUSED', 'SCRUM', 1, NOW()),
('PRJ010', '旧官网改版', '企业官网全新设计改版项目', 'WEB', 'ARCHIVED', 'KANBAN', 1, NOW());

-- 为每个初始项目初始化任务状态（从业务字典 task_status 复制）
INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ001', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ002', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ003', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ004', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ005', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ006', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ007', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ008', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ009', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

INSERT IGNORE INTO task_status (project_id, code, name_en, name_zh, color, sort_order)
SELECT 'PRJ010', d.code, d.name_en, d.name_zh,
       JSON_EXTRACT(d.extra, '$.color') as color, d.sort_order
FROM sys_dict_code d
JOIN sys_dict_type t ON d.dict_type_id = t.id
WHERE t.code = 'task_status';

-- =============================================================================
-- 敏捷角色初始化
-- =============================================================================

-- 添加新的敏捷角色（IGNORE 避免重复）
INSERT IGNORE INTO sys_role (role_id, name, description) VALUES
('ROLE_PRODUCT_OWNER', 'Product Owner', '产品负责人，负责产品规划、需求管理、优先级排序'),
('ROLE_SCRUM_MASTER', 'Scrum Master', 'Scrum Master，负责 Scrum 流程执行、团队协调、Sprint 管理'),
('ROLE_TEAM_MEMBER', 'Team Member', '团队成员，负责任务执行、代码开发、Bug 修复'),
('ROLE_PROJECT_ADMIN', 'Project Admin', '项目管理员，拥有项目全部权限');

-- =============================================================================
-- 权限初始化
-- =============================================================================

-- 清空权限表后重新初始化
TRUNCATE TABLE sys_permission;
DELETE FROM sys_role_permission WHERE role_id IN (SELECT id FROM sys_role WHERE role_id IN ('ROLE_PRODUCT_OWNER', 'ROLE_SCRUM_MASTER', 'ROLE_TEAM_MEMBER', 'ROLE_PROJECT_ADMIN'));

-- 需求管理权限
INSERT INTO sys_permission (code, name, module) VALUES
('requirement:create:epic', '创建 Epic', 'requirement'),
('requirement:create:feature', '创建 Feature', 'requirement'),
('requirement:create:story', '创建 Story', 'requirement'),
('requirement:create:bug', '创建 Bug', 'requirement'),
('requirement:edit:epic', '编辑 Epic', 'requirement'),
('requirement:edit:feature', '编辑 Feature', 'requirement'),
('requirement:edit:story', '编辑 Story', 'requirement'),
('requirement:delete:epic', '删除 Epic', 'requirement'),
('requirement:delete:feature', '删除 Feature', 'requirement'),
('requirement:move:epic', '移动 Epic', 'requirement'),
('requirement:move:feature', '移动 Feature', 'requirement');

-- Sprint 权限
INSERT INTO sys_permission (code, name, module) VALUES
('sprint:manage', '管理 Sprint', 'sprint'),
('sprint:start', '开始 Sprint', 'sprint'),
('sprint:complete', '完成 Sprint', 'sprint');

-- 任务权限
INSERT INTO sys_permission (code, name, module) VALUES
('task:create', '创建任务', 'task'),
('task:assign', '分配任务', 'task'),
('task:update:own', '更新自己的任务', 'task'),
('task:complete:own', '完成自己的任务', 'task');

-- 项目权限
INSERT INTO sys_permission (code, name, module) VALUES
('project:manage', '管理项目', 'project');

-- =============================================================================
-- 角色权限映射
-- 使用数字 role_id (sys_role.id)
-- =============================================================================

-- Product Owner 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_id = 'ROLE_PRODUCT_OWNER' AND p.code IN ('requirement:create:epic', 'requirement:create:feature', 'requirement:create:story', 'requirement:create:bug', 'requirement:edit:epic', 'requirement:edit:feature', 'requirement:edit:story', 'requirement:delete:epic', 'requirement:delete:feature', 'requirement:move:epic', 'requirement:move:feature', 'sprint:manage', 'task:assign');

-- Scrum Master 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_id = 'ROLE_SCRUM_MASTER' AND p.code IN ('requirement:create:story', 'requirement:create:bug', 'sprint:manage', 'sprint:start', 'sprint:complete', 'task:create', 'task:assign');

-- Team Member 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_id = 'ROLE_TEAM_MEMBER' AND p.code IN ('requirement:create:story', 'requirement:create:bug', 'task:create', 'task:update:own', 'task:complete:own');

-- Project Admin 权限（全部权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_id = 'ROLE_PROJECT_ADMIN';

-- =============================================================================
-- Bug 状态初始化（每个项目独立）
-- =============================================================================

-- 禁用外键检查以便插入数据
SET FOREIGN_KEY_CHECKS = 0;

-- PRJ001 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ001', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ001', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ001', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ001', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ001', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ002 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ002', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ002', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ002', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ002', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ002', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ003 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ003', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ003', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ003', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ003', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ003', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ004 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ004', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ004', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ004', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ004', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ004', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ005 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ005', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ005', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ005', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ005', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ005', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ006 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ006', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ006', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ006', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ006', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ006', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ007 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ007', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ007', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ007', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ007', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ007', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ008 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ008', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ008', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ008', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ008', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ008', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ009 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ009', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ009', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ009', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ009', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ009', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- PRJ010 Bug 状态
INSERT IGNORE INTO bug_status (project_id, code, name_en, name_zh, color, sort_order) VALUES
('PRJ010', 'OPEN', 'Open', '待办', '#EF4444', 1),
('PRJ010', 'IN_PROGRESS', 'In Progress', '修复中', '#F59E0B', 2),
('PRJ010', 'IN_TEST', 'In Test', '待验证', '#3B82F6', 3),
('PRJ010', 'CLOSED', 'Closed', '已关闭', '#10B981', 4),
('PRJ010', 'REOPENED', 'Reopened', '重新打开', '#8B5CF6', 5);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- Bug 状态流转规则初始化
-- =============================================================================

-- OPEN -> IN_PROGRESS, CLOSED
-- IN_PROGRESS -> IN_TEST, REOPENED
-- IN_TEST -> CLOSED, IN_PROGRESS
-- CLOSED -> REOPENED
-- REOPENED -> IN_PROGRESS

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status) VALUES
('PRJ001', 'OPEN', 'IN_PROGRESS'),
('PRJ001', 'OPEN', 'CLOSED'),
('PRJ001', 'IN_PROGRESS', 'IN_TEST'),
('PRJ001', 'IN_PROGRESS', 'REOPENED'),
('PRJ001', 'IN_TEST', 'CLOSED'),
('PRJ001', 'IN_TEST', 'IN_PROGRESS'),
('PRJ001', 'CLOSED', 'REOPENED'),
('PRJ001', 'REOPENED', 'IN_PROGRESS');

-- 复制到其他项目
INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ002', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ003', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ004', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ005', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ006', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ007', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ008', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ009', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';

INSERT IGNORE INTO bug_status_transition (project_id, from_status, to_status)
SELECT 'PRJ010', from_status, to_status FROM bug_status_transition WHERE project_id = 'PRJ001';
