# 任务状态配置设计方案

## 概述

当项目创建时，从业务字典 `task_status` 初始化项目专属的任务状态到 `task_status` 表。看板和冲刺页面的任务状态统一从 `task_status` 表获取，支持查看、编辑、删除（含条件限制）、拖拽排序。

## 1. 数据初始化流程

### 项目创建时

- `ProjectServiceImpl.create()` 调用 `TaskStatusService.initializeFromDict(projectId)`
- 从 `sys_dict_code` 表读取 `dict_type='task_status'` 的所有字典项
- 为每项在 `task_status` 表中创建一条记录，`project_id` 指向新项目

**字段映射：**
| sys_dict_code | task_status |
|---------------|-------------|
| code | code |
| name | name / name_zh |
| extra.color | color |
| sort_order | sort_order |

## 2. 看板/冲刺状态获取逻辑

### 修改 useKanban.js

- 移除对业务字典 `/dicts/codes/task_status` 的依赖
- 直接调用 `/api/v1/task-statuses/project/{projectId}` 获取项目状态
- 如果项目无自定义状态（返回空），则调用 `/api/v1/task-statuses/system` 作为兜底

## 3. 任务状态配置页签功能

| 操作 | 说明 |
|------|------|
| 查看 | 显示当前项目所有状态，按 sort_order 排序 |
| 编辑 | 编辑名称、颜色、所属分类（todo/doing/done/alert） |
| 删除 | 条件下可删除：状态下无任务时才允许删除 |
| 拖拽排序 | 通过拖拽调整 sort_order |

## 4. 后端 API 变更

### 新增接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/task-statuses/init/{projectId}` | POST | 从业务字典初始化项目状态 |
| `/api/v1/task-statuses/reorder` | PUT | 批量更新排序 |

### 修改接口

- `TaskStatusService.findByProjectId(String projectId)` - 仅返回该项目状态，不合并系统默认

## 5. 涉及文件

### 后端
- `ProjectServiceImpl.java` - 项目创建时调用初始化
- `TaskStatusServiceImpl.java` - 新增 initializeFromDict、reorder 方法
- `TaskStatusController.java` - 新增 init、reorder 端点
- `ITaskStatusService.java` - 新增接口定义

### 前端
- `useKanban.js` - 移除字典依赖，直接从 task_status 表获取
- `StatusConfig.vue` - 支持拖拽排序，删除前检查任务引用

## 6. 数据库变更

无需表结构变更，sort_order 字段已存在。
