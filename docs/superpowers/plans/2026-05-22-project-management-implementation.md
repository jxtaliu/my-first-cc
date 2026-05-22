# 项目管理模块实施计划

> **目标**: 实现完整的项目管理功能
> **设计规格**: `docs/superpowers/specs/2026-05-22-project-management-design.md`
> **前端框架**: Vue 3 + Element Plus
> **后端框架**: Spring Boot

---

## 一、完整功能清单

### 阶段一：基础设施 (预计 2 天)

#### 任务 1.1: 设计规范落地
- [x] 创建 `frontend/src/styles/variables.css` - CSS 变量定义
- [x] 创建 `frontend/src/styles/project-managment.css` - 项目管理专用样式
- [x] 更新 `frontend/src/styles/common.css` - 全局样式适配

#### 任务 1.2: 布局组件
- [x] 创建 `frontend/src/components/layout/ProjectLayout.vue` - 项目页面布局
- [x] 创建 `frontend/src/components/layout/Sidebar.vue` - 项目侧边栏

#### 任务 1.3: 看板组件库
- [x] 创建 `frontend/src/components/kanban/KanbanBoard.vue` - 看板主容器
- [x] 创建 `frontend/src/components/kanban/KanbanColumn.vue` - 看板列
- [x] 创建 `frontend/src/components/kanban/TaskCard.vue` - 任务卡片
- [x] 创建 `frontend/src/components/kanban/Swimlane.vue` - 泳道组件

---

### 阶段二：项目核心管理 (预计 2 天)

#### 任务 2.1: 项目模板管理
- [x] 创建 `ProjectTemplateController.java` - 项目模板 API
- [x] 创建 `ProjectTemplateService.java` - 模板服务
- [x] 创建 `ProjectTemplateMapper.java` - 模板 Mapper
- [x] 前端: `frontend/src/views/admin/ProjectTemplates.vue` - 模板管理页面
- [x] 实现模板 CRUD
- [x] 实现模板应用到项目

#### 任务 2.2: 项目角色管理
- [x] 扩展 `ProjectMember` - 添加角色字段 (项目负责人/项目经理/开发负责人/开发者/访客)
- [x] 创建 `ProjectRole` 枚举/实体
- [x] 实现角色权限校验
- [x] 前端: 项目设置页面 - 成员角色管理

#### 任务 2.3: 项目状态管理
- [x] 添加项目状态枚举 (规划中/启动中/进行中/已完成/暂停/已归档)
- [x] 实现状态流转校验
- [x] 前端: 项目状态选择器

---

### 阶段三：任务管理增强 (预计 3 天)

#### 任务 3.1: 任务状态自定义
- [x] 创建 `TaskStatus` 实体 - 可配置的任务状态
- [x] 创建 `StatusTransition` 实体 - 状态流转规则
- [x] 实现状态流转校验逻辑
- [x] 前端: 状态管理页面 - 添加/编辑/删除状态
- [x] 前端: 状态流转规则配置页面

#### 任务 3.2: 任务优先级
- [x] 添加 `Task` 优先级字段 (P0/P1/P2/P3)
- [x] 实现优先级筛选和排序
- [x] 前端: 优先级选择器组件
- [x] 前端: 看板列按优先级筛选

#### 任务 3.3: 任务依赖关系
- [x] 创建 `TaskDependency` 实体 - 任务依赖
- [x] 实现依赖类型 (FS/SS/FF/SF)
- [x] 实现依赖校验 (删除/移动时检查)
- [x] 前端: 任务详情 - 添加依赖弹窗
- [x] 前端: 甘特图 - 依赖线可视化

#### 任务 3.4: 任务工时
- [x] 添加 `remainingHours` 字段到 `Task`
- [x] 实现工时更新逻辑
- [x] 前端: 任务详情 - 工时编辑
- [x] 前端: 任务卡片显示预估/剩余/实际工时

---

### 阶段四：Sprint 管理 (预计 2 天)

#### 任务 4.1: Sprint 基础功能
- [x] 完善 `SprintService` - Sprint CRUD
- [x] 实现 Sprint 规划 - 从 Backlog 添加到 Sprint
- [x] 前端: Sprint 列表页面
- [x] 前端: Sprint 创建/编辑弹窗

#### 任务 4.2: Sprint 设置
- [x] 添加 Sprint 目标、长度配置
- [x] 实现 Sprint 提醒功能
- [x] 前端: Sprint 设置页面
- [x] 前端: Sprint 目标编辑

#### 任务 4.3: Sprint 预估
- [x] 实现团队速率计算
- [x] 实现容量计算
- [x] 实现完成预测
- [x] 前端: Sprint 统计面板

---

### 阶段五：看板视图 (预计 3 天)

#### 任务 5.1: 我的看板
- [x] 创建 `frontend/src/views/project/MyBoard.vue` - 我的看板页面
- [x] 实现跨项目任务筛选
- [x] 实现 WIP 限制
- [x] 实现泳道切换

#### 任务 5.2: 项目看板
- [x] 创建 `frontend/src/views/project/ProjectBoard.vue` - 项目看板
- [x] 实现快速添加任务
- [x] 实现拖拽校验 (状态流转规则)

#### 任务 5.3: 冲刺看板
- [x] 创建 `frontend/src/views/project/SprintBoard.vue` - 冲刺看板
- [x] 实现 Sprint 选择器
- [x] 实现 Sprint 燃尽图

#### 任务 5.4: 团队看板 & Backlog
- [x] 创建 `frontend/src/views/project/TeamBoard.vue` - 团队看板
- [x] 创建 `frontend/src/views/project/BacklogBoard.vue` - Backlog

---

### 阶段六：甘特图 (预计 2 天)

#### 任务 6.1: 甘特图组件
- [x] 创建 `frontend/src/components/gantt/GanttChart.vue`
- [x] 创建 `frontend/src/components/gantt/GanttTask.vue`
- [x] 实现任务依赖线可视化
- [x] 实现拖拽调整时间

#### 任务 6.2: 甘特图页面
- [x] 创建 `frontend/src/views/project/GanttView.vue`
- [x] 实现里程碑标记
- [x] 实现时间轴缩放

---

### 阶段七：里程碑管理 (预计 2 天)

#### 任务 7.1: 后端 API
- [x] 创建 `MilestoneController.java`
- [x] 创建 `MilestoneService.java`
- [x] 创建 `Milestone` 实体
- [x] 实现跨项目里程碑

#### 任务 7.2: 前端页面
- [x] 创建 `frontend/src/views/project/Milestones.vue`
- [x] 创建 `frontend/src/views/project/MilestoneDetail.vue`
- [x] 实现里程碑创建/编辑
- [x] 实现任务关联 (手动 + 基于 Sprint 自动)

---

### 阶段八：跨项目视图 (预计 2 天)

#### 任务 8.1: 统计面板
- [x] 创建 `frontend/src/views/project/StatsDashboard.vue`
- [x] 创建 KPI 卡片组件
- [x] 创建燃尽图、雷达图等图表组件

#### 任务 8.2: 项目对比 & 全局看板
- [x] 创建 `frontend/src/views/project/ProjectCompare.vue`
- [x] 创建 `frontend/src/views/project/PortfolioBoard.vue`

---

### 阶段九：工时审批 (预计 1 天)

#### 任务 9.1: 审批流程
- [x] 扩展 TimesheetController - 添加审批 API
- [x] 实现项目经理审批逻辑

#### 任务 9.2: 前端审批页面
- [x] 创建 `frontend/src/views/timesheet/Approval.vue`
- [x] 实现审批列表、通过/驳回

---

### 阶段十：站内通知 (预计 1 天)

#### 任务 10.1: 通知系统
- [x] 创建 `NotificationController.java`
- [x] 创建 `NotificationService.java`
- [x] 创建 `Notification` 实体
- [x] 实现通知触发逻辑

#### 任务 10.2: 前端通知中心
- [x] 创建 `frontend/src/views/notification/NotificationCenter.vue`
- [x] 实现通知图标徽章
- [x] 实现通知列表

---

### 阶段十一：评论与附件 (预计 1 天)

#### 任务 11.1: 任务评论
- [x] 创建 `Comment` 实体
- [x] 实现 @提及 解析
- [x] 前端: 评论组件

#### 任务 11.2: 任务附件
- [x] 集成文件上传服务
- [x] 创建附件存储逻辑
- [x] 前端: 附件上传/预览组件

---

### 阶段十二：集成测试 (预计 2 天)

#### 任务 12.1: 页面集成
- [x] 更新路由配置
- [x] 更新侧边栏菜单

#### 任务 12.2: 功能测试
- [x] 看板拖拽测试
- [x] 甘特图交互测试
- [x] 审批流程测试
- [x] 通知触发测试

---

## 二、文件清单

### 后端新建文件

```
backend/src/main/java/com/sme/pm/
├── entity/
│   ├── Milestone.java
│   ├── ProjectTemplate.java
│   ├── TaskStatus.java
│   ├── StatusTransition.java
│   ├── TaskDependency.java
│   └── Notification.java
├── mapper/
│   ├── MilestoneMapper.java
│   ├── ProjectTemplateMapper.java
│   ├── TaskStatusMapper.java
│   ├── StatusTransitionMapper.java
│   ├── TaskDependencyMapper.java
│   └── NotificationMapper.java
├── service/
│   ├── MilestoneService.java
│   ├── ProjectTemplateService.java
│   ├── TaskStatusService.java
│   ├── NotificationService.java
│   └── impl/
│       ├── MilestoneServiceImpl.java
│       ├── ProjectTemplateServiceImpl.java
│       ├── TaskStatusServiceImpl.java
│       └── NotificationServiceImpl.java
└── controller/
    ├── MilestoneController.java
    ├── ProjectTemplateController.java
    ├── TaskStatusController.java
    ├── NotificationController.java
    └── CommentController.java
```

### 前端新建文件

```
frontend/src/
├── components/
│   ├── layout/
│   │   ├── ProjectLayout.vue
│   │   └── Sidebar.vue
│   ├── kanban/
│   │   ├── KanbanBoard.vue
│   │   ├── KanbanColumn.vue
│   │   ├── TaskCard.vue
│   │   └── Swimlane.vue
│   ├── gantt/
│   │   ├── GanttChart.vue
│   │   ├── GanttTask.vue
│   │   └── GanttTimeline.vue
│   ├── charts/
│   │   ├── BurndownChart.vue
│   │   ├── CFDChart.vue
│   │   ├── RadarChart.vue
│   │   └── HeatmapChart.vue
│   └── common/
│       ├── StatCard.vue
│       ├── PriorityBadge.vue
│       ├── StatusTag.vue
│       └── CommentList.vue
├── views/project/
│   ├── MyBoard.vue
│   ├── ProjectBoard.vue
│   ├── SprintBoard.vue
│   ├── TeamBoard.vue
│   ├── BacklogBoard.vue
│   ├── GanttView.vue
│   ├── Milestones.vue
│   ├── MilestoneDetail.vue
│   ├── StatsDashboard.vue
│   ├── ProjectCompare.vue
│   ├── PortfolioBoard.vue
│   └── Settings/
│       ├── ProjectSettings.vue
│       ├── MemberRoles.vue
│       ├── StatusConfig.vue
│       └── SprintSettings.vue
├── views/timesheet/
│   └── Approval.vue
├── views/notification/
│   └── NotificationCenter.vue
├── views/admin/
│   └── ProjectTemplates.vue
├── styles/
│   ├── variables.css
│   └── project-managment.css
└── api/
    ├── milestone.js
    ├── template.js
    ├── task-status.js
    ├── notification.js
    └── comment.js
```

---

## 三、技术栈

| 用途 | 技术 |
|------|------|
| 看板拖拽 | `vuedraggable@next` |
| 图表 | `Chart.js` + `vue-chartjs` |
| 甘特图 | `vue-ganttastic` 或自研 |
| 状态管理 | Pinia |
| 文件上传 | Element Plus upload |

---

## 四、优先级建议

**第一优先级 (MVP)**:
1. 项目模板管理
2. 项目看板 + 任务卡片
3. 冲刺看板 + 燃尽图
4. 任务状态自定义
5. 任务优先级

**第二优先级**:
1. 甘特图
2. 里程碑管理
3. 跨项目统计
4. Sprint 设置

**第三优先级**:
1. 工时审批
2. 站内通知
3. 团队看板
4. Backlog 看板

**第四优先级**:
1. 任务评论 + @提及
2. 任务附件
3. 项目对比

---

**总预计工时**: 约 20 个工作日

---

**文档版本历史**
- v1.0 (2026-05-22): 初始版本
- v1.1 (2026-05-22): 增加完整功能清单（模板、角色、状态、工时、通知、附件）
