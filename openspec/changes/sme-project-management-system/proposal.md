## Why

中小企业技术团队缺乏系统化的项目管理和工时统计工具。现有方案（Jira 等）复杂度过高或成本高昂，导致团队依赖 Excel 或口头同步，造成工时统计困难、项目进度不透明、资源分配不均、跨部门协作成本高等问题。

## What Changes

- 构建完整的项目管理体系，支持 Scrum Sprint 和 Kanban 两种模式
- 实现灵活的层级任务结构（Epic → Feature → Story → Task），团队可自定义
- 提供工时填报、汇总、审批功能，支持日报/周报/月报视图
- 建立四层权限体系（超级管理员、部门管理员、项目管理员、普通成员），支持角色叠加
- 预留多语言框架（i18n）和多种部署方式（SaaS/私有化/混合）
- 第一阶段 MVP 包含：用户管理、权限管理、项目管理、工时管理、基础仪表盘

## Capabilities

### New Capabilities

- `user-auth`: 本地账号注册、登录、JWT 认证
- `role-permission`: 角色创建、权限分配、用户角色管理
- `project-management`: 项目 CRUD、Sprint 管理（固定/敏捷/看板）、灵活层级任务
- `time-tracking`: 工时填报、日周月报视图、工时统计
- `dashboard`: 项目进度概览、工作量统计、待办任务
- `multi-tenancy`: 多语言框架预留（中文/英文/其他）

### Modified Capabilities

- （无）

## Impact

- 新增后端模块：User、Role、Permission、Project、Sprint、Task、Timesheet、Report
- 新增前端模块：Auth、Users、Roles、Projects、Board/Sprints、Timesheet、Dashboard
- 新增数据库表：user、role、permission、user_role、project、sprint、task、timesheet
- 依赖：SpringBoot 3.x、MyBatis-Plus、MySQL、Redis、Vue3、Vite、Element Plus
