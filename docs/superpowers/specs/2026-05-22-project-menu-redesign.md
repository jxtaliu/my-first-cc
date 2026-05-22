# 项目管理菜单重新设计

## 概述

重新设计项目管理模块的菜单结构，采用按职能分组的方式，提升用户体验和菜单组织效率。

## 目标

- 简化菜单结构，减少用户查找功能的时间
- 按职能分组，相关功能归类在一起
- 保持功能完整性的同时优化菜单层级

## 菜单结构设计

### 一级菜单
1. **仪表盘** - `/dashboard`
2. **项目管理** - 可展开/折叠的子菜单

### 项目管理子菜单（3个分组）

#### 1. 我的工作
| 功能 | 路由 | 说明 |
|------|------|------|
| 我的看板 | `/projects/my-board` | 当前用户所有任务按状态展示 |
| 我的任务 | `/projects/my-tasks` | 任务列表视图（新建） |
| 我的工时 | `/timesheet/my` | 工时记录（可复用现有工时页面） |

#### 2. 项目总览
| 功能 | 路由 | 说明 |
|------|------|------|
| 项目列表 | `/projects` | 所有项目卡片/列表 |
| 里程碑 | `/projects/milestones` | 跨项目里程碑 |
| 项目统计 | `/projects/stats` | 项目数据分析 |
| 甘特图 | `/projects/gantt` | 甘特图视图 |
| Backlog | `/projects/backlog` | 待办列表 |
| 项目对比 | `/projects/compare` | 多项目对比 |
| Portfolio | `/projects/portfolio` | 项目组合视图 |

#### 3. 项目设置
| 功能 | 路由 | 说明 |
|------|------|------|
| Sprint设置 | `/projects/settings/sprint` | Sprint周期配置 |
| 状态配置 | `/projects/settings/status` | 任务状态工作流 |
| 成员角色 | `/projects/settings/members` | 成员及权限 |
| 项目模板 | `/projects/settings/templates` | 项目管理模板（新建） |
| 通知设置 | `/projects/settings/notifications` | 通知偏好（新建） |

### 独立菜单项（不属于项目管理）
- **工时管理** - `/timesheet`
- **工时审批** - `/timesheet/approval`
- **通知中心** - `/notification`
- **管理** - 用户、角色、部门、字典管理

## 实现任务

### 1. Layout.vue 修改
- 将项目管理相关菜单改为可折叠的 el-sub-menu
- 按3个分组组织子菜单项

### 2. 新建页面组件
- `MyTasks.vue` - 我的任务列表页面
- `ProjectTemplates.vue` - 项目模板配置页面
- `NotificationSettings.vue` - 通知设置页面

### 3. 路由配置更新
- 添加 `/projects/my-tasks` 路由
- 添加 `/projects/settings/templates` 路由
- 添加 `/projects/settings/notifications` 路由

### 4. API 对齐
- 确保所有页面调用的 API 路径与后端控制器一致

## 验证方式

1. 登录系统后检查菜单显示
2. 点击各菜单项验证页面加载
3. 验证权限控制（无权限用户看不到特定菜单）
4. 响应式布局验证（菜单折叠/展开）

## 里程碑

- [ ] 完成 Layout.vue 菜单改造
- [ ] 创建 MyTasks.vue 页面
- [ ] 创建 ProjectTemplates.vue 页面
- [ ] 创建 NotificationSettings.vue 页面
- [ ] 更新路由配置
- [ ] 测试所有菜单功能
