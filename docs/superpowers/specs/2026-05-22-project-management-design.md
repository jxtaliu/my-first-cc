# 项目管理模块 UI 设计规格

> **设计方向**: Industrial Precision（工业精密）
> **版本**: 1.0
> **日期**: 2026-05-22

---

## 一、设计理念

受工程蓝图和数据仪表盘启发，打造专业、高效、视觉精确的项目管理界面。
核心理念：**结构化、数据驱动、精密感、工业美学**

---

## 二、色彩体系

### 2.1 主色调

| 变量名 | 色值 | 用途 |
|--------|------|------|
| `--primary` | #1E3A5F | 深海蓝 - 专业可信 |
| `--secondary` | #2D5A87 | 中蓝 - 次级元素 |
| `--accent` | #00D4AA | 青绿色 - 进度/成功 |
| `--warning` | #FFB020 | 琥珀色 - 提醒 |
| `--danger` | #FF6B6B | 珊瑚红 - 阻塞/逾期 |
| `--neutral` | #64748B | 板岩灰 - 文字 |
| `--background` | #F8FAFC | 冷白 - 页面底色 |
| `--card` | #FFFFFF | 纯白 - 卡片 |
| `--border` | #E2E8F0 | 浅灰 - 分隔线 |

### 2.2 任务状态色

| 状态 | 色值 | 说明 |
|------|------|------|
| todo | #94A3B8 | 灰色 |
| in_progress | #3B82F6 | 蓝色 |
| development | #8B5CF6 | 紫色 |
| review | #F59E0B | 琥珀色 |
| done | #10B981 | 翠绿色 |
| blocked | #EF4444 | 红色 |

### 2.3 优先级色

| 优先级 | 色值 |
|--------|------|
| P0 (紧急) | #EF4444 |
| P1 (高) | #F97316 |
| P2 (中) | #FBBF24 |
| P3 (低) | #6B7280 |

### 2.4 CSS 变量定义

```css
:root {
  /* Primary Colors */
  --primary: #1E3A5F;
  --primary-light: #2D5A87;
  --primary-dark: #152A45;

  /* Accent Colors */
  --accent: #00D4AA;
  --accent-light: #33DDBB;
  --accent-dark: #00B894;

  /* Status Colors */
  --status-todo: #94A3B8;
  --status-in-progress: #3B82F6;
  --status-development: #8B5CF6;
  --status-review: #F59E0B;
  --status-done: #10B981;
  --status-blocked: #EF4444;

  /* Priority Colors */
  --priority-p0: #EF4444;
  --priority-p1: #F97316;
  --priority-p2: #FBBF24;
  --priority-p3: #6B7280;

  /* Background & Surface */
  --background: #F8FAFC;
  --card: #FFFFFF;
  --border: #E2E8F0;
  --border-light: #F1F5F9;

  /* Text */
  --text-primary: #1E293B;
  --text-secondary: #64748B;
  --text-muted: #94A3B8;

  /* Shadows */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.06);

  /* Border Radius */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
}
```

---

## 三、字体规范

```css
/* 标题字体 - Plus Jakarta Sans */
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;600;700&display=swap');

/* 正文字体 - Inter */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap');

/* 代码/数据字体 - JetBrains Mono */
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500&display=swap');

--font-heading: 'Plus Jakarta Sans', sans-serif;
--font-body: 'Inter', system-ui, sans-serif;
--font-mono: 'JetBrains Mono', monospace;
```

| 用途 | 字体 | 字号 | 字重 |
|------|------|------|------|
| 页面标题 | Plus Jakarta Sans | 24px | 700 |
| 卡片标题 | Plus Jakarta Sans | 18px | 600 |
| 表格表头 | Inter | 13px | 600 |
| 正文 | Inter | 14px | 400 |
| 标签文字 | Inter | 12px | 500 |
| 代码/数据 | JetBrains Mono | 13px | 400 |

---

## 四、页面布局规范

### 4.1 页面结构

```
┌─────────────────────────────────────────────────────────────────┐
│ Header: 项目选择器 | 视图切换 | 搜索 | 通知 | 用户             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Sidebar (可选)  │  Main Content Area                          │
│  - 项目导航      │  - 视图内容                                  │
│  - 快速筛选      │  - 数据表格/看板/图表                        │
│  - 收藏项目      │                                             │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ Footer: 分页信息 | 快捷操作                                      │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 间距系统

```css
--space-xs: 4px;
--space-sm: 8px;
--space-md: 12px;
--space-lg: 16px;
--space-xl: 20px;
--space-2xl: 24px;
--space-3xl: 32px;
```

### 4.3 卡片规范

```css
.card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-xl);
}

.card:hover {
  box-shadow: var(--shadow-md);
}
```

---

## 五、看板组件规范

### 5.1 看板列

```css
.kanban-column {
  width: 320px;
  min-width: 320px;
  background: var(--background);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 200px);
}

.kanban-column-header {
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
}

.kanban-column-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}
```

### 5.2 任务卡片

```css
.task-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: var(--space-lg);
  cursor: grab;
  transition: all 0.2s ease;
  position: relative;
  border-left: 4px solid var(--status-color);
}

.task-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.task-card.dragging {
  transform: scale(1.02);
  box-shadow: var(--shadow-lg);
  opacity: 0.9;
}
```

### 5.3 卡片内部结构

```
┌───────────────────────────────────────┐
│ [优先级] 任务标题                      │ ← 左侧4px色条 + 标题
│ ───────────────────────────────────── │
│ [Epic] [Feature]                      │ ← 类型标签组
│ ───────────────────────────────────── │
│ 👤 负责人    📅 截止日期               │
│ ⏱ 预计:4h   ⏰ 实际:3h                │
│ ───────────────────────────────────── │
│ ████████░░░░░░░░░░░  50%             │ ← 进度条
│ ───────────────────────────────────── │
│ 💬 3  📎 2  ⏳ 2天                   │ ← 底部信息
└───────────────────────────────────────┘
```

### 5.4 泳道配置

| 泳道维度 | 说明 |
|----------|------|
| 负责人 | 按任务负责人分组 |
| 优先级 | 按 P0/P1/P2/P3 分组 |
| 类型 | 按 Epic/Feature/Story/Task 分组 |
| 截止日期 | 按本周/下周/本月/已逾期分组 |

---

## 六、数据表格规范

### 6.1 表格样式

```css
.data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.data-table th {
  background: #FAFBFC;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid var(--border);
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
}

.data-table tr:hover td {
  background: var(--background);
}
```

### 6.2 排序图标

```
升序: ▲
降序: ▼
默认: —
```

---

## 七、图表规范

### 7.1 燃尽图

```
X轴: Sprint天数
Y轴: 剩余故事点/任务数
线条: 理想线(虚线) + 实际线(实线)
颜色: 理想线 #94A3B8, 实际线 #3B82F6
区域: 低于理想线填充 #10B98120, 高于理想线填充 #EF444420
```

### 7.2 累积流图 (CFD)

```
X轴: 时间
Y轴: 任务数量
区域: 自下而上堆叠
- Done: #10B981
- In Progress: #3B82F6
- To Do: #94A3B8
边框: 无边框，区域渐变填充
```

### 7.3 雷达图 (项目健康度)

```
维度: 进度 | 工时效率 | 质量 | 协作 | 交付
形状: 五边形
填充: #3B82F620 (浅蓝)
边框: #3B82F6 (深蓝)
顶点: #3B82F6
```

---

## 八、交互动效规范

### 8.1 过渡时长

```css
--transition-fast: 150ms;
--transition-normal: 200ms;
--transition-slow: 300ms;
```

### 8.2 动效规格

| 元素 | 动效 | 时长 | 缓动 |
|------|------|------|------|
| 卡片悬停 | translateY(-2px) + shadow | 200ms | ease |
| 卡片拖拽 | scale(1.02) + shadow | 150ms | ease |
| 弹窗打开 | scale(0.95→1) + opacity(0→1) | 200ms | ease-out |
| 数字变化 | 数字滚动动画 | 400ms | ease-out |
| 列加载 | opacity(0→1), stagger 100ms | 200ms | ease |
| 进度条 | width变化 | 300ms | ease-out |
| 状态切换 | background-color渐变 | 200ms | ease |

### 8.3 拖拽规范

```css
.drag-over {
  background: var(--accent-light);
  border: 2px dashed var(--accent);
}

.wip-exceeded {
  border: 2px solid var(--danger);
  animation: pulse-danger 1s infinite;
}

@keyframes pulse-danger {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
```

---

## 九、状态设计

### 9.1 空状态

```
┌─────────────────────────────────────────┐
│                                         │
│              🗂️ 空空如也               │
│                                         │
│      还没有任务，点击下方按钮创建第一个    │
│                                         │
│          [+ 创建任务]  [查看Backlog]    │
│                                         │
└─────────────────────────────────────────┘
```

### 9.2 加载状态

```
骨架屏动画:
background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
background-size: 200% 100%;
animation: skeleton-loading 1.5s infinite;
```

### 9.3 错误状态

```
┌─────────────────────────────────────────┐
│                                         │
│              ⚠️ 加载失败               │
│                                         │
│        网络连接异常，请检查后重试          │
│                                         │
│              [重新加载]                  │
│                                         │
└─────────────────────────────────────────┘
```

---

## 十、响应式断点

```css
/* 桌面 */
@media (min-width: 1280px) {
  .kanban-column { width: 320px; }
}

/* 平板 */
@media (max-width: 1279px) {
  .kanban-column { width: 280px; min-width: 280px; }
}

/* 移动端 */
@media (max-width: 768px) {
  .kanban-column { width: 100%; min-width: 100%; }
  .sidebar { display: none; }
}
```

---

## 十一、组件清单

### 11.1 基础组件

| 组件 | 说明 | 状态 |
|------|------|------|
| Button | 按钮 | default, hover, active, disabled, loading |
| Input | 输入框 | default, focus, error, disabled |
| Select | 下拉选择 | default, open, selected, disabled |
| Tag | 标签 | 多种颜色主题 |
| Badge | 徽章 | 数字徽章, 点状徽章 |
| Avatar | 头像 | 单个, 堆叠组 |
| Progress | 进度条 | 线性, 环形 |
| Tooltip | 提示 | 多种位置 |

### 11.2 业务组件

| 组件 | 说明 |
|------|------|
| TaskCard | 任务卡片 |
| KanbanColumn | 看板列 |
| KanbanBoard | 看板容器 |
| GanttChart | 甘特图 |
| MilestoneCard | 里程碑卡片 |
| StatCard | 统计卡片 |
| RadarChart | 雷达图 |
| BurndownChart | 燃尽图 |
| CFDChart | 累积流图 |
| HeatmapChart | 热力图 |

---

## 十二、图标规范

使用 Element Plus 内置图标 + 自定义 SVG 图标

| 用途 | 图标 |
|------|------|
| 状态指示 | Element Plus status icons |
| 操作按钮 | Element Plus action icons |
| 优先级 | 自定义 P0/P1/P2/P3 图标 |
| 类型 | 自定义 Epic/Feature/Story/Task 图标 |
| 统计 | 自定义图表图标 |

---

## 附录: 组件代码示例

### 任务卡片 HTML 结构

```html
<div class="task-card" data-status="in_progress" draggable="true">
  <div class="task-card-header">
    <span class="priority-badge priority-p1">P1</span>
    <h4 class="task-title">任务标题</h4>
  </div>
  <div class="task-tags">
    <span class="task-tag tag-epic">Epic</span>
    <span class="task-tag tag-feature">Feature</span>
  </div>
  <div class="task-meta">
    <span class="assignee">
      <img class="avatar-sm" src="..." alt="张三">
      张三
    </span>
    <span class="estimate">预计: 4h</span>
  </div>
  <div class="task-progress">
    <div class="progress-bar" style="--progress: 50%"></div>
    <span class="progress-text">50%</span>
  </div>
  <div class="task-footer">
    <span class="comment-count">
      <svg>...</svg> 3
    </span>
    <span class="duration">⏳ 2天</span>
  </div>
</div>
```

---

## 十三、项目模板管理

### 13.1 模板内容

| 设置项 | 说明 |
|--------|------|
| 模板名称 | 如：敏捷开发模板 |
| Sprint 长度 | 1周/2周/4周 可选 |
| 任务状态 | 从预置状态中选择 |
| 优先级 | 是否启用 P0/P1/P2/P3 |
| 默认角色 | 项目负责人、项目经理、开发负责人 |
| 任务类型 | Epic/Feature/Story/Task |
| 状态流转规则 | 预设流转链 |
| 通知设置 | 默认通知规则 |

### 13.2 模板列表 UI

```
┌─────────────────────────────────────────────────────────────────┐
│  项目模板                                                   │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │ 🏗️ 敏捷开发模板 │ │ 📋 瀑布模板    │ │ 📝 日常任务模板 │ │
│  │ 默认4周Sprint   │ │ 阶段式交付     │ │ 无Sprint       │ │
│  │ P0-P3优先级     │ │ 无优先级       │ │ 简单流程        │ │
│  │ [使用] [编辑]   │ │ [使用] [编辑]   │ │ [使用] [编辑]   │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 十四、项目角色体系

### 14.1 项目内角色

| 角色 | 权限说明 |
|------|----------|
| 👑 项目负责人 | 全部权限 |
| 📋 项目经理 | 管理冲刺、审批工时 |
| 👥 开发负责人 | 分配任务、创建Epic |
| 👤 开发者 | 更新任务状态 |
| 👤 访客 | 只读 |

### 14.2 角色权限矩阵

| 权限 | 项目负责人 | 项目经理 | 开发负责人 | 开发者 | 访客 |
|------|-----------|----------|------------|--------|------|
| 编辑项目信息 | ✓ | - | - | - | - |
| 管理成员 | ✓ | ✓ | - | - | - |
| 创建 Sprint | ✓ | ✓ | - | - | - |
| 规划 Backlog | ✓ | ✓ | ✓ | - | - |
| 分配任务 | ✓ | ✓ | ✓ | - | - |
| 创建 Epic | ✓ | ✓ | ✓ | - | - |
| 创建 Task | ✓ | ✓ | ✓ | ✓ | - |
| 更新任务状态 | ✓ | ✓ | ✓ | ✓ | - |
| 审批工时 | ✓ | ✓ | - | - | - |
| 查看所有任务 | ✓ | ✓ | ✓ | ✓ | ✓ |

---

## 十五、项目状态设计

### 15.1 项目生命周期

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    │
│  规划中  │───▶│  启动中  │───▶│ 进行中  │───▶│  已完成  │───▶│
└─────────┘    └─────────┘    └─────────┘    └─────────┘    │
     │              │              │              │             │
     │              │              │              ▼             │
     │              │              │         ┌─────────┐      │
     │              │              │         │ 已归档   │◀─────┘
     │              │              │         └─────────┘
     │              │              │
     ▼              ▼              ▼
┌─────────────────────────────────┐
│         暂停/延期               │
└─────────────────────────────────┘
```

### 15.2 项目状态说明

| 状态 | 色值 | 说明 |
|------|------|------|
| 规划中 | #94A3B8 (灰) | 刚创建，尚未启动 |
| 启动中 | #3B82F6 (蓝) | 正在初始化 |
| 进行中 | #10B981 (绿) | 正式开发 |
| 已完成 | #8B5CF6 (紫) | 开发完成，等待验收 |
| 暂停 | #F59E0B (琥珀) | 暂时停止 |
| 已归档 | #64748B (深灰) | 不再活跃 |

---

## 十六、任务状态自定义

### 16.1 默认状态配置

| 状态 | 色值 | 类别 | 说明 |
|------|------|------|------|
| 待办 (Todo) | #94A3B8 | todo | 还未开始 |
| 进行中 (In Progress) | #3B82F6 | doing | 正在处理 |
| 开发完成 (Dev Done) | #8B5CF6 | doing | 代码完成待测试 |
| 测试中 (Testing) | #F59E0B | doing | 测试阶段 |
| 已完成 (Done) | #10B981 | done | 测试通过 |
| 已阻塞 (Blocked) | #EF4444 | alert | 被阻塞 |

### 16.2 状态流转规则

```
状态流转配置:

Todo ──────────▶ In Progress
  │                   │
  │                   ▼
  │              Dev Done ──────────▶ Testing
  │                   ▲                   │
  │                   │                   ▼
  │                   └───────────────────┘
  │
  └──────────────────────────────────────▶ Done

Blocked ───────────────────────────────▶ Todo
     │                                        │
     └────────────────────────────────────────┘
```

### 16.3 状态流转规则配置 UI

```
┌─────────────────────────────────────────────────────────────────┐
│  状态流转规则                                    [+ 添加规则]     │
├─────────────────────────────────────────────────────────────────┤
│  从状态          │  可流转至              │  操作               │
│  ────────────────┼──────────────────────┼─────────            │
│  待办            │  进行中, 已阻塞        │  [编辑] [删除]      │
│  进行中          │  待办, 开发完成, 已阻塞 │  [编辑] [删除]      │
│  开发完成        │  测试中, 进行中, 已阻塞 │  [编辑] [删除]      │
│  测试中          │  开发完成, 已完成, 已阻塞│  [编辑] [删除]      │
│  已完成          │  -                     │  [编辑]             │
│  已阻塞          │  待办, 进行中           │  [编辑] [删除]      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 十七、任务功能增强

### 17.1 任务类型

| 类型 | 图标 | 颜色 | 说明 |
|------|------|------|------|
| Epic | 📦 | #8B5CF6 (紫) | 大功能模块 |
| Feature | 🧩 | #3B82F6 (蓝) | 具体功能 |
| Story | 📋 | #10B981 (绿) | 用户故事 |
| Task | ✅ | #94A3B8 (灰) | 具体开发任务 |
| Bug | 🐛 | #EF4444 (红) | 缺陷修复 |
| Sub-task | ➗ | #64748B (深灰) | 子任务 |

### 17.2 任务类型层级

```
Epic (L1)
 ├── Feature (L2)
 │    ├── Story (L3)
 │    │    ├── Sub-task (L4)
 │    │    └── Sub-task (L4)
 │    └── Story (L3)
 │         └── Sub-task (L4)
 └── Feature (L2)
```

### 17.3 任务详情面板

```
┌─────────────────────────────────────────────────────────────────┐
│  [Epic] 用户认证模块                                    [⋮ 更多] │
├─────────────────────────────────────────────────────────────────┤
│  标题: 用户认证模块                                             │
│  ───────────────────────────────────────────────────────────   │
│  描述: 完整用户认证流程...                                       │
│  ───────────────────────────────────────────────────────────   │
│  附件: 📎 认证流程图.png   📎 API设计文档.pdf                  │
│  ───────────────────────────────────────────────────────────   │
│  预估: [8]h   剩余: [5]h   实际: [3]h                         │
│  ───────────────────────────────────────────────────────────   │
│  优先级: 🔴 P0    负责人: 👤 张三    截止: 📅 2024-02-01      │
│  ───────────────────────────────────────────────────────────   │
│  依赖: 🔗 前置: 用户注册模块 (未完成)                           │
│  ───────────────────────────────────────────────────────────   │
│  评论 (3):                                                     │
│  👤 李四: @张三 登录页面UI需要调整...                           │
└─────────────────────────────────────────────────────────────────┘
```

### 17.4 任务依赖关系

| 类型 | 说明 |
|------|------|
| FS (Finish to Start) | 前置任务完成后才能开始 |
| SS (Start to Start) | 前置任务开始后才能开始 |
| FF (Finish to Finish) | 前置任务完成后才能结束 |
| SF (Start to Finish) | 前置任务开始后才能结束 |

---

## 十八、Sprint 设置

### 18.1 Sprint 配置

| 设置项 | 说明 |
|--------|------|
| Sprint 长度 | 1周/2周/4周 |
| 开始/结束日期 | 自动计算 |
| 提醒设置 | Sprint 开始/结束/每日提醒 |
| Sprint 目标 | 描述本次 Sprint 目标 |

### 18.2 Sprint 预估

| 指标 | 说明 |
|------|------|
| 团队速率 | 过去 N 个 Sprint 的平均完成量 |
| 容量 | 可用工时 = 人数 × 每天工时 × 天数 |
| 预测 | 基于速率预测完成量 |

---

## 十九、工时管理

### 19.1 预估字段

| 字段 | 说明 |
|------|------|
| 预估工时 (Estimate) | 最初预估 |
| 剩余工时 (Remaining) | 还需要的工时 |
| 实际工时 (Actual) | 已消耗的工时 |

### 19.2 工时审批流程

```
填报人提交 → 项目经理审批 → 通过/驳回
                │
                ▼
          通知填报人结果
```

---

## 二十、站内通知

### 20.1 通知触发点

| 事件 | 通知对象 |
|------|----------|
| 任务分配 | assignee |
| @提及评论 | 被@人 |
| 任务状态变更 | assignee, 关注者 |
| 任务被阻塞 | assignee |
| 前置任务完成 | assignee (等待中的任务) |
| 里程碑临近/到期 | 项目经理 |
| Sprint 开始/结束 | 所有项目成员 |
| 工时被驳回/通过 | 填报人 |

---

## 二十一、附件功能

### 21.1 附件规格

| 项目 | 限制 |
|------|------|
| 单文件大小 | 20MB |
| 支持格式 | 图片、PDF、Word、Excel、压缩包 |
| 预览 | 图片/PDF 在线预览 |

---

**文档版本历史**
- v1.0 (2026-05-22): 初始版本
- v1.1 (2026-05-22): 增加项目管理完整功能（模板、角色、状态、工时、通知、附件）
