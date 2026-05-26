# 冲刺与任务池关联管理设计方案

> **功能：** 在统一页面管理 backlog 与各冲刺的任务，支持跨泳道拖拽和批量操作

## 1. 概述

### 1.1 背景
当前系统已实现：
- 任务通过 `sprintId` 关联冲刺
- BacklogBoard 支持将 backlog 任务拖入冲刺
- 缺少统一的冲刺管理视图和批量操作能力

### 1.2 目标
- 提供统一的泳道视图，同时展示 backlog 和所有冲刺的任务
- 支持虚拟滚动以应对上千任务的性能问题
- 支持拖拽和批量操作

---

## 2. 功能设计

### 2.1 页面布局

**路由：** `/projects/sprint-management/:projectId`

**侧边栏入口：** `项目管理 → 冲刺管理`

**布局结构：**
```
┌─────────────────────────────────────────────────────────────────────┐
│  项目: PRJ001 - 冲刺管理                    [搜索] [类型▼] [优先级▼] │
├─────────────────────────────────────────────────────────────────────┤
│  [+ 新建冲刺]  [+ 规划冲刺]                    [视图: 泳道 | 列表]  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ═══ Backlog (未规划) ═══  156个任务           [全选] [批量操作▼] │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ [虚拟滚动区域]                                               │  │
│  │ ☐ [任务卡片] ☐ [任务卡片] ☐ [任务卡片] ...             │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ═══ Sprint 1 - 进行中 (ACTIVE) ═══  42个任务  容量: 120h      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ [虚拟滚动区域]                                               │  │
│  │ ☐ [任务卡片] ☐ [任务卡片] ...                             │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ═══ Sprint 2 - 规划中 (PLANNING) ═══  28个任务                 │
│  ...                                                              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────┘
         ↑ 拖拽任务可移入移出任意冲刺
```

### 2.2 泳道组件 (SprintLane)

| 属性 | 说明 |
|-----|------|
| name | 泳道名称（Backlog / Sprint名称） |
| status | 状态（null / PLANNING / ACTIVE / COMPLETED） |
| taskCount | 任务总数 |
| capacityHours | 冲刺容量（仅冲刺有） |
| tasks | 任务列表 |
| hasMore | 是否有更多未加载任务 |

**泳道内支持：**
- 虚拟滚动（只渲染可见区域）
- 拖拽放置区域
- 复选框批量选择

### 2.3 任务卡片 (TaskCard)

| 显示字段 | 说明 |
|---------|------|
| checkbox | 复选框，用于批量选择 |
| title | 任务标题 |
| type | 类型标签（EPIC/FEATURE/STORY/TASK/SUBTASK/BUG） |
| priority | 优先级（P0/P1/P2/P3） |
| assignee | 负责人头像+名字 |
| estimateHours | 预估工时 |

### 2.4 核心功能

#### 2.4.1 拖拽操作
- 单个任务可拖拽到任意泳道
- 拖拽时目标泳道高亮显示
- 放置后立即调用 API 更新 `sprintId`

#### 2.4.2 批量操作
底部批量操作栏（选中任务后出现）：
| 操作 | 说明 |
|-----|------|
| 批量移入冲刺 | 选择目标冲刺，批量移动 |
| 批量移出 | 移回 backlog（sprintId = null） |
| 全选 | 当前泳道全选 |

#### 2.4.3 搜索与筛选
- 搜索：按任务标题/描述
- 筛选：类型、优先级、负责人
- 排序：创建时间、优先级、标题

#### 2.4.4 虚拟滚动
- 使用 `vue-virtual-scroller` 或自定义实现
- 每条泳道独立虚拟滚动
- 支持分页加载（一次20条）

---

## 3. API 设计

### 3.1 新增接口

```javascript
// 批量移动任务到冲刺
POST /api/v1/sprints/{sprintId}/tasks/batch
Content-Type: application/json
Body: { taskIds: [1, 2, 3] }
Response: { code: 200, message: "success", data: { movedCount: 3 } }

// 批量移出冲刺（回到 backlog）
DELETE /api/v1/sprints/{sprintId}/tasks/batch
Content-Type: application/json
Body: { taskIds: [1, 2, 3] }
Response: { code: 200, message: "success", data: { movedCount: 3 } }

// 获取冲刺任务列表（分页）
GET /api/v1/sprints/{sprintId}/tasks?page=1&pageSize=20
Response: { code: 200, message: "success", data: { tasks: [...], total: 42, page: 1 } }

// 获取 backlog 任务列表（分页）
GET /api/v1/projects/{projectId}/backlog/tasks?page=1&pageSize=20
Response: { code: 200, message: "success", data: { tasks: [...], total: 156, page: 1 } }
```

### 3.2 复用接口

| 接口 | 用途 |
|-----|------|
| GET /api/v1/projects/{projectId}/sprints | 获取冲刺列表 |
| GET /api/v1/projects/{projectId}/tasks | 获取项目所有任务 |
| PUT /api/v1/tasks/{id} | 更新任务（包含 sprintId） |

---

## 4. 数据模型

### 4.1 前端数据结构

```typescript
// 任务卡片
interface TaskCard {
  id: number
  title: string
  type: 'EPIC' | 'FEATURE' | 'STORY' | 'TASK' | 'SUBTASK' | 'BUG'
  priority: 'P0' | 'P1' | 'P2' | 'P3'
  assigneeId: number | null
  assigneeName: string
  estimateHours: number
  sprintId: number | null  // null = backlog
}

// 泳道
interface SprintLane {
  id: number | null  // null = backlog
  name: string
  status: 'BACKLOG' | 'PLANNING' | 'ACTIVE' | 'COMPLETED'
  taskCount: number
  capacityHours?: number
  tasks: TaskCard[]
  hasMore: boolean
  loading: boolean
}
```

### 4.2 后端模型（复用现有）

```java
// Task 实体已有字段
public class Task {
    private Long sprintId;  // 关联冲刺，null 表示 backlog
    // ... 其他字段
}
```

---

## 5. 组件结构

```
views/project/
└── SprintManagement.vue      # 主页面组件

components/sprint/
├── SprintLane.vue             # 单个泳道组件
│   ├── TaskCard.vue          # 任务卡片
│   └── VirtualScroller.vue   # 虚拟滚动容器
├── BatchActionBar.vue        # 底部批量操作栏
├── SprintHeader.vue          # 泳道头部
├── TaskFilter.vue            # 筛选器
└── SprintSelector.vue       # 冲刺选择弹窗
```

---

## 6. 实现计划

### Phase 1: 基础框架
- 创建 SprintManagement.vue 页面
- 实现泳道列表渲染
- 接入现有 API 获取任务数据

### Phase 2: 虚拟滚动
- 引入虚拟滚动组件
- 实现分页加载
- 优化大量任务渲染性能

### Phase 3: 拖拽功能
- 实现任务卡片的拖拽
- 跨泳道放置
- 乐观更新 UI

### Phase 4: 批量操作
- 复选框批量选择
- 批量操作栏
- 批量 API 对接

### Phase 5: 搜索与筛选
- 搜索功能
- 多维度筛选
- 排序能力

---

## 7. 菜单配置

### 侧边栏菜单
```javascript
{
  path: '/projects/sprint-management/:projectId',
  name: 'SprintManagement',
  component: () => import('@/views/project/SprintManagement.vue'),
  meta: {
    title: '冲刺管理',
    icon: 'Timer',
    parent: 'ProjectManagement'
  }
}
```

### 菜单位置
```
侧边栏 → 项目管理 → 冲刺管理
```

---

## 8. 预期效果

1. **统一视图** - 在一个页面同时管理 backlog 和所有冲刺
2. **性能优化** - 虚拟滚动支持上千任务流畅操作
3. **操作便捷** - 拖拽移动、批量操作提高效率
4. **状态清晰** - 每个泳道显示任务数量和容量信息
