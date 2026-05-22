# 项目管理模块实施计划

> **目标**: 实现完整的项目管理功能，包含看板、甘特图、里程碑、跨项目统计
> **设计规格**: `docs/superpowers/specs/2026-05-22-project-management-design.md`
> **前端框架**: Vue 3 + Element Plus
> **后端框架**: Spring Boot

---

## 一、任务分解

### 阶段一：基础设施 (预计 2 天)

#### 任务 1.1: 设计规范落地
- [ ] 创建 `frontend/src/styles/variables.css` - CSS 变量定义
- [ ] 创建 `frontend/src/styles/project-managment.css` - 项目管理专用样式
- [ ] 更新 `frontend/src/styles/common.css` - 全局样式适配

#### 任务 1.2: 布局组件
- [ ] 创建 `frontend/src/components/layout/ProjectLayout.vue` - 项目页面布局
- [ ] 创建 `frontend/src/components/layout/Sidebar.vue` - 项目侧边栏

#### 任务 1.3: 看板组件库
- [ ] 创建 `frontend/src/components/kanban/KanbanBoard.vue` - 看板主容器
- [ ] 创建 `frontend/src/components/kanban/KanbanColumn.vue` - 看板列
- [ ] 创建 `frontend/src/components/kanban/TaskCard.vue` - 任务卡片
- [ ] 创建 `frontend/src/components/kanban/Swimlane.vue` - 泳道组件

---

### 阶段二：看板视图 (预计 3 天)

#### 任务 2.1: 我的看板
- [ ] 创建 `frontend/src/views/project/MyBoard.vue` - 我的看板页面
- [ ] 实现任务筛选逻辑（按项目、状态、优先级）
- [ ] 实现 WIP 限制逻辑
- [ ] 实现泳道切换

#### 任务 2.2: 项目看板
- [ ] 创建 `frontend/src/views/project/ProjectBoard.vue` - 项目看板页面
- [ ] 实现项目内所有任务的看板展示
- [ ] 实现快速添加任务
- [ ] 实现拖拽校验

#### 任务 2.3: 冲刺看板
- [ ] 创建 `frontend/src/views/project/SprintBoard.vue` - 冲刺看板页面
- [ ] 实现 Sprint 选择器
- [ ] 实现 Sprint 燃尽图
- [ ] 实现任务状态流转规则

#### 任务 2.4: 团队看板
- [ ] 创建 `frontend/src/views/project/TeamBoard.vue` - 团队看板页面
- [ ] 实现团队筛选
- [ ] 实现多成员视图

#### 任务 2.5: Backlog 看板
- [ ] 创建 `frontend/src/views/project/BacklogBoard.vue` - Backlog 页面
- [ ] 实现优先级排序
- [ ] 实现快速规划到 Sprint

---

### 阶段三：甘特图 (预计 2 天)

#### 任务 3.1: 甘特图组件
- [ ] 创建 `frontend/src/components/gantt/GanttChart.vue` - 甘特图主组件
- [ ] 创建 `frontend/src/components/gantt/GanttTask.vue` - 甘特图任务条
- [ ] 创建 `frontend/src/components/gantt/GanttTimeline.vue` - 时间轴

#### 任务 3.2: 甘特图页面
- [ ] 创建 `frontend/src/views/project/GanttView.vue` - 甘特图页面
- [ ] 实现任务依赖关系可视化
- [ ] 实现拖拽调整时间
- [ ] 实现里程碑标记

---

### 阶段四：里程碑管理 (预计 2 天)

#### 任务 4.1: 后端 API
- [ ] 创建 `MilestoneController.java` - 里程碑控制器
- [ ] 实现 MilestoneService - 里程碑服务
- [ ] 创建 Milestone Entity

#### 任务 4.2: 前端页面
- [ ] 创建 `frontend/src/views/project/Milestones.vue` - 里程碑列表页
- [ ] 创建 `frontend/src/views/project/MilestoneDetail.vue` - 里程碑详情页
- [ ] 实现里程碑创建/编辑弹窗
- [ ] 实现任务关联功能

---

### 阶段五：跨项目视图 (预计 2 天)

#### 任务 5.1: 统计面板
- [ ] 创建 `frontend/src/views/project/StatsDashboard.vue` - 统计仪表盘
- [ ] 实现 KPI 卡片组件
- [ ] 实现燃尽图组件
- [ ] 实现 CFD 图表组件

#### 任务 5.2: 项目对比
- [ ] 创建 `frontend/src/views/project/ProjectCompare.vue` - 项目对比页
- [ ] 实现雷达图组件
- [ ] 实现对比表格
- [ ] 实现热力图组件

#### 任务 5.3: 全局看板
- [ ] 创建 `frontend/src/views/project/PortfolioBoard.vue` - 项目组合看板
- [ ] 实现多项目选择器
- [ ] 实现跨项目统计汇总

---

### 阶段六：工时审批 (预计 1 天)

#### 任务 6.1: 后端审批流
- [ ] 扩展 TimesheetController - 添加审批 API
- [ ] 实现项目经理审批逻辑

#### 任务 6.2: 前端审批页面
- [ ] 创建 `frontend/src/views/timesheet/Approval.vue` - 审批页面
- [ ] 实现审批列表
- [ ] 实现通过/驳回功能

---

### 阶段七：站内通知 (预计 1 天)

#### 任务 7.1: 通知系统
- [ ] 创建 `NotificationController.java` - 通知控制器
- [ ] 实现通知服务
- [ ] 创建 `frontend/src/views/notification/NotificationCenter.vue` - 通知中心
- [ ] 实现通知图标徽章

---

### 阶段八：集成与测试 (预计 2 天)

#### 任务 8.1: 页面集成
- [ ] 更新 `frontend/src/router/index.js` - 路由配置
- [ ] 更新侧边栏导航菜单

#### 任务 8.2: 测试
- [ ] 看板拖拽功能测试
- [ ] 甘特图交互测试
- [ ] 审批流程测试
- [ ] 通知触发测试

---

## 二、文件清单

### 新建文件

```
backend/src/main/java/com/sme/pm/
├── entity/
│   └── Milestone.java
├── mapper/
│   └── MilestoneMapper.java
├── service/
│   ├── MilestoneService.java
│   └── impl/MilestoneServiceImpl.java
├── controller/
│   ├── MilestoneController.java
│   └── NotificationController.java
└── dto/
    ├── MilestoneDTO.java
    └── NotificationDTO.java

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
│       └── MilestoneCard.vue
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
│   └── PortfolioBoard.vue
├── views/timesheet/
│   └── Approval.vue
├── views/notification/
│   └── NotificationCenter.vue
├── styles/
│   ├── variables.css
│   └── project-managment.css
└── api/
    ├── milestone.js
    └── notification.js
```

### 修改文件

```
frontend/src/
├── router/index.js - 添加路由
├── views/project/List.vue - 项目列表增强
├── views/project/Detail.vue - 项目详情重构
├── App.vue - 布局调整
└── components/Layout.vue - 添加通知入口

backend/src/main/java/com/sme/pm/
├── entity/Project.java - 添加字段
├── service/ProjectService.java - 添加统计方法
└── controller/ProjectController.java - 添加统计 API
```

---

## 三、技术实现细节

### 3.1 拖拽实现
使用 `vuedraggable` 库实现看板拖拽：
```bash
npm install vuedraggable@next
```

### 3.2 图表实现
使用 `Chart.js` + `vue-chartjs`：
```bash
npm install chart.js vue-chartjs
```

### 3.3 甘特图实现
使用 `vue-ganttastic` 或自研：
```bash
npm install vue-ganttastic
```

### 3.4 状态管理
使用 Pinia store 管理看板状态：
```javascript
// stores/kanban.js
export const useKanbanStore = defineStore('kanban', {
  state: () => ({
    columns: [],
    tasks: [],
    swimlaneMode: 'none'
  })
})
```

---

## 四、优先级建议

**第一优先级** (MVP):
1. 项目看板 + 任务卡片
2. 冲刺看板 + 燃尽图
3. 我的看板
4. 甘特图基础版

**第二优先级**:
1. 里程碑管理
2. 跨项目统计
3. 工时审批
4. 站内通知

**第三优先级**:
1. 团队看板
2. Backlog 看板
3. 项目对比
4. 热力图

---

## 五、风险与注意事项

1. **性能**: 大量任务时需实现虚拟滚动
2. **冲突**: 多人同时拖拽时需处理冲突
3. **数据**: 统计指标需后端聚合计算
4. **响应式**: 移动端看板体验需专门优化
