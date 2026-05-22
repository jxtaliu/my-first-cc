# 项目管理菜单重新设计实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重新设计项目管理菜单，采用按职能分组的组织方式

**Architecture:** 修改 Layout.vue 的菜单结构，将项目管理相关功能分组到3个子分组（我的工作、项目总览、项目设置），新增3个页面组件，修改路由配置

**Tech Stack:** Vue 3, Element Plus, Vue Router, Pinia

---

## 文件变更清单

### 需要修改的文件
- `frontend/src/components/Layout.vue` - 菜单结构改造
- `frontend/src/router/index.js` - 添加新路由
- `frontend/src/locales/zh-CN.js` - 添加中文翻译
- `frontend/src/locales/en-US.js` - 添加英文翻译

### 需要新建的文件
- `frontend/src/views/project/MyTasks.vue` - 我的任务页面
- `frontend/src/views/project/settings/ProjectTemplates.vue` - 项目模板配置页面
- `frontend/src/views/project/settings/NotificationSettings.vue` - 通知设置页面

---

## 实现任务

### Task 1: 修改 Layout.vue 菜单结构

**Files:**
- Modify: `frontend/src/components/Layout.vue`

- [ ] **Step 1: 读取当前 Layout.vue 完整内容**

确认当前菜单结构，特别是项目管理相关的菜单项

- [ ] **Step 2: 替换菜单结构**

将项目管理相关的独立菜单项替换为一个可折叠的 el-sub-menu，包含3个分组：

```vue
<el-sub-menu index="project-management">
  <template #title>
    <el-icon><Folder /></el-icon>
    <span>{{ $t('nav.projects') }}</span>
  </template>
  
  <!-- 分组1: 我的工作 -->
  <el-menu-item-group>
    <template #title>{{ $t('nav.myWork') }}</template>
    <el-menu-item index="/projects/my-board">
      <el-icon><Grid /></el-icon>
      <span>{{ $t('nav.myBoard') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/my-tasks">
      <el-icon><List /></el-icon>
      <span>{{ $t('nav.myTasks') }}</span>
    </el-menu-item>
    <el-menu-item index="/timesheet/my">
      <el-icon><Clock /></el-icon>
      <span>{{ $t('nav.myTimesheet') }}</span>
    </el-menu-item>
  </el-menu-item-group>
  
  <!-- 分组2: 项目总览 -->
  <el-menu-item-group>
    <template #title>{{ $t('nav.projectOverview') }}</template>
    <el-menu-item index="/projects">
      <el-icon><FolderOpened /></el-icon>
      <span>{{ $t('nav.projects') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/milestones">
      <el-icon><Flag /></el-icon>
      <span>{{ $t('nav.milestones') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/stats">
      <el-icon><DataAnalysis /></el-icon>
      <span>{{ $t('nav.projectStats') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/gantt">
      <el-icon><TrendCharts /></el-icon>
      <span>{{ $t('nav.gantt') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/backlog">
      <el-icon><List /></el-icon>
      <span>{{ $t('nav.backlog') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/compare">
      <el-icon><Connection /></el-icon>
      <span>{{ $t('nav.compare') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/portfolio">
      <el-icon><OfficeBuilding /></el-icon>
      <span>{{ $t('nav.portfolio') }}</span>
    </el-menu-item>
  </el-menu-item-group>
  
  <!-- 分组3: 项目设置 -->
  <el-menu-item-group>
    <template #title>{{ $t('nav.projectSettings') }}</template>
    <el-menu-item index="/projects/settings/sprint">
      <el-icon><Timer /></el-icon>
      <span>{{ $t('nav.sprintSettings') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/settings/status">
      <el-icon><Grid /></el-icon>
      <span>{{ $t('nav.statusConfig') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/settings/members">
      <el-icon><User /></el-icon>
      <span>{{ $t('nav.memberRoles') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/settings/templates">
      <el-icon><DocumentCopy /></el-icon>
      <span>{{ $t('nav.projectTemplates') }}</span>
    </el-menu-item>
    <el-menu-item index="/projects/settings/notifications">
      <el-icon><Bell /></el-icon>
      <span>{{ $t('nav.notificationSettings') }}</span>
    </el-menu-item>
  </el-menu-item-group>
</el-sub-menu>
```

- [ ] **Step 3: 更新图标导入**

添加缺失的图标导入：FolderOpened, Flag, TrendCharts, Connection, DocumentCopy

- [ ] **Step 4: 提交代码**

```bash
git add frontend/src/components/Layout.vue
git commit -m "feat: restructure project menu with grouped submenus"
```

---

### Task 2: 添加国际化翻译

**Files:**
- Modify: `frontend/src/locales/zh-CN.js`
- Modify: `frontend/src/locales/en-US.js`

- [ ] **Step 1: 添加中文翻译**

在 zh-CN.js 中添加：

```javascript
nav: {
  // ... existing
  myWork: '我的工作',
  projectOverview: '项目总览',
  projectSettings: '项目设置',
  myTasks: '我的任务',
  myTimesheet: '我的工时',
  projectStats: '项目统计',
  projectTemplates: '项目模板',
  notificationSettings: '通知设置',
  gantt: '甘特图',
  compare: '项目对比',
  portfolio: 'Portfolio',
}
```

- [ ] **Step 2: 添加英文翻译**

在 en-US.js 中添加对应翻译

- [ ] **Step 3: 提交代码**

```bash
git add frontend/src/locales/zh-CN.js frontend/src/locales/en-US.js
git commit -m "feat: add i18n keys for new menu structure"
```

---

### Task 3: 添加路由配置

**Files:**
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: 添加新路由**

在 children 数组中添加：

```javascript
{
  path: 'projects/my-tasks',
  name: 'MyTasks',
  component: () => import('@/views/project/MyTasks.vue')
},
{
  path: 'projects/settings/templates',
  name: 'ProjectTemplates',
  component: () => import('@/views/project/settings/ProjectTemplates.vue')
},
{
  path: 'projects/settings/notifications',
  name: 'NotificationSettings',
  component: () => import('@/views/project/settings/NotificationSettings.vue')
},
```

- [ ] **Step 2: 提交代码**

```bash
git add frontend/src/router/index.js
git commit -m "feat: add routes for new project menu pages"
```

---

### Task 4: 创建 MyTasks.vue 页面

**Files:**
- Create: `frontend/src/views/project/MyTasks.vue`

- [ ] **Step 1: 创建页面组件**

创建任务列表页面，包含：
- 任务筛选（状态、优先级、类型）
- 任务表格展示
- 分页功能

```vue
<template>
  <div class="my-tasks-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('nav.myTasks') }}</h1>
    </div>
    
    <el-card>
      <el-table :data="tasks" v-loading="loading">
        <el-table-column prop="title" :label="$t('task.title')" />
        <el-table-column prop="status" :label="$t('task.status')" width="120" />
        <el-table-column prop="priority" :label="$t('task.priority')" width="100" />
        <el-table-column prop="projectName" :label="$t('task.project')" width="150" />
        <el-table-column prop="dueDate" :label="$t('task.dueDate')" width="120" />
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        class="mt-4"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTasksByAssignee } from '@/api/task'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const tasks = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(async () => {
  await loadTasks()
})

async function loadTasks() {
  loading.value = true
  try {
    const res = await getTasksByAssignee(authStore.user?.id)
    tasks.value = res.data || res
    total.value = tasks.value.length
  } finally {
    loading.value = false
  }
}
</script>
```

- [ ] **Step 2: 提交代码**

```bash
git add frontend/src/views/project/MyTasks.vue
git commit -m "feat: create MyTasks page"
```

---

### Task 5: 创建 ProjectTemplates.vue 页面

**Files:**
- Create: `frontend/src/views/project/settings/ProjectTemplates.vue`

- [ ] **Step 1: 创建页面组件**

创建项目管理模板配置页面，包含：
- 模板列表展示
- 模板详情/编辑
- 默认模板设置

```vue
<template>
  <div class="project-templates-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('nav.projectTemplates') }}</h1>
      <el-button type="primary" @click="onCreate">
        {{ $t('common.create') }}
      </el-button>
    </div>
    
    <el-card v-for="template in templates" :key="template.id" class="template-card">
      <template #header>
        <div class="template-header">
          <span>{{ template.name }}</span>
          <el-tag v-if="template.isDefault">{{ $t('template.default') }}</el-tag>
        </div>
      </template>
      <p>{{ template.description }}</p>
      <div class="template-meta">
        <span>{{ $t('template.sprintDuration') }}: {{ template.sprintDuration }} {{ $t('template.days') }}</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTemplates } from '@/api/template'

const templates = ref([])

onMounted(async () => {
  const res = await getTemplates()
  templates.value = res.data || res
})
</script>
```

- [ ] **Step 2: 提交代码**

```bash
git add frontend/src/views/project/settings/ProjectTemplates.vue
git commit -m "feat: create ProjectTemplates settings page"
```

---

### Task 6: 创建 NotificationSettings.vue 页面

**Files:**
- Create: `frontend/src/views/project/settings/NotificationSettings.vue`

- [ ] **Step 1: 创建页面组件**

创建通知设置页面，包含：
- 通知类型开关
- 通知方式配置（邮件、应用内）
- 免打扰时间设置

```vue
<template>
  <div class="notification-settings-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('nav.notificationSettings') }}</h1>
    </div>
    
    <el-card>
      <el-form :model="settings" label-width="200px">
        <el-form-item :label="$t('notification.taskAssigned')">
          <el-switch v-model="settings.taskAssigned" />
        </el-form-item>
        <el-form-item :label="$t('notification.taskStatusChanged')">
          <el-switch v-model="settings.taskStatusChanged" />
        </el-form-item>
        <el-form-item :label="$t('notification.sprintStart')">
          <el-switch v-model="settings.sprintStart" />
        </el-form-item>
        <el-form-item :label="$t('notification.sprintEnd')">
          <el-switch v-model="settings.sprintEnd" />
        </el-form-item>
        <el-form-item :label="$t('notification.timesheetApproved')">
          <el-switch v-model="settings.timesheetApproved" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const settings = ref({
  taskAssigned: true,
  taskStatusChanged: true,
  sprintStart: true,
  sprintEnd: true,
  timesheetApproved: true,
})
</script>
```

- [ ] **Step 2: 提交代码**

```bash
git add frontend/src/views/project/settings/NotificationSettings.vue
git commit -m "feat: create NotificationSettings page"
```

---

### Task 7: 最终验证

- [ ] **Step 1: 运行前端构建验证**

```bash
cd frontend && npm run build
```

- [ ] **Step 2: 检查所有菜单项**

确认 Layout.vue 中所有菜单项的图标和路由都正确

- [ ] **Step 3: 推送代码**

```bash
git push
```

---

## 验证方式

1. 启动前端 `npm run dev`
2. 登录后检查侧边栏菜单
3. 点击"项目管理"展开子菜单
4. 验证3个分组都正确显示
5. 点击各菜单项验证页面加载
6. 验证路由正确跳转
