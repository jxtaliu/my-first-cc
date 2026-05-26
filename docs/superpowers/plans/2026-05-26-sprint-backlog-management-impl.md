# 冲刺与任务池关联管理实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在统一页面管理 backlog 与各冲刺的任务，支持跨泳道拖拽和批量操作

**Architecture:** 采用泳道式布局，每个冲刺和 backlog 各占一条泳道。任务通过 sprintId 关联冲刺，sprintId=null 表示 backlog。使用虚拟滚动优化大量任务渲染性能。

**Tech Stack:** Vue 3 + Element Plus + vue-virtual-scroller + HTML5 Drag & Drop

---

## 文件结构

```
frontend/src/
├── views/project/
│   └── SprintManagement.vue           # 主页面组件
├── components/sprint/                # 新增目录
│   ├── SprintLane.vue                # 单个泳道组件
│   ├── TaskCard.vue                  # 任务卡片（复用现有）
│   └── BatchActionBar.vue           # 批量操作栏
└── router/index.js                   # 路由配置

backend/src/main/java/com/sme/pm/
├── controller/SprintController.java  # 新增批量操作 API
├── service/impl/SprintServiceImpl.java
└── mapper/SprintMapper.java
```

---

## Task 1: 创建 SprintManagement.vue 主页面

**Files:**
- Create: `frontend/src/views/project/SprintManagement.vue`

- [ ] **Step 1: 创建基础页面结构和 API 引入**

```vue
<template>
  <div class="sprint-management-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>{{ $t('project.sprintManagement') }}</h1>
      <div class="header-actions">
        <el-button type="primary" @click="onCreateSprint">
          {{ $t('project.createSprint') }}
        </el-button>
      </div>
    </div>

    <!-- 泳道列表 -->
    <div class="lanes-container">
      <SprintLane
        v-for="lane in lanes"
        :key="lane.id"
        :lane="lane"
        @drop="onDropTask"
        @select-task="onSelectTask"
      />
    </div>

    <!-- 批量操作栏 -->
    <BatchActionBar
      v-if="selectedTasks.length > 0"
      :selected-count="selectedTasks.length"
      :sprints="sprints"
      @batch-assign="onBatchAssign"
      @batch-remove="onBatchRemove"
      @clear-selection="clearSelection"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getSprints } from '@/api/project'
import { getTasksByProject } from '@/api/task'
import SprintLane from '@/components/sprint/SprintLane.vue'
import BatchActionBar from '@/components/sprint/BatchActionBar.vue'

const sprints = ref([])
const tasks = ref([])
const selectedTasks = ref([])

const lanes = computed(() => {
  const sprintLanes = sprints.value.map(s => ({
    id: s.id,
    name: s.name,
    status: s.status,
    taskCount: tasks.value.filter(t => t.sprintId === s.id).length,
    tasks: tasks.value.filter(t => t.sprintId === s.id),
    capacityHours: s.capacityHours
  }))
  const backlogLane = {
    id: null,
    name: 'Backlog',
    status: 'BACKLOG',
    taskCount: tasks.value.filter(t => !t.sprintId).length,
    tasks: tasks.value.filter(t => !t.sprintId)
  }
  return [backlogLane, ...sprintLanes]
})

onMounted(async () => {
  const projectId = route.params.projectId
  const [sprintsRes, tasksRes] = await Promise.all([
    getSprints(projectId),
    getTasksByProject(projectId)
  ])
  sprints.value = sprintsRes.data || []
  tasks.value = tasksRes.data || []
})
</script>
```

- [ ] **Step 2: 添加路由配置**

Modify: `frontend/src/router/index.js` (在 children 数组中添加)

```javascript
{
  path: 'sprint-management/:projectId',
  name: 'SprintManagement',
  component: () => import('@/views/project/SprintManagement.vue')
}
```

- [ ] **Step 3: 添加 i18n key**

Modify: `frontend/src/locales/zh-CN.js`

```javascript
project: {
  // ... existing keys
  sprintManagement: '冲刺管理',
  createSprint: '创建冲刺',
  // ... existing keys
}
```

Modify: `frontend/src/locales/en-US.js`

```javascript
project: {
  // ... existing keys
  sprintManagement: 'Sprint Management',
  createSprint: 'Create Sprint',
  // ... existing keys
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/project/SprintManagement.vue
git add frontend/src/router/index.js
git add frontend/src/locales/zh-CN.js frontend/src/locales/en-US.js
git commit -m "feat: create SprintManagement page skeleton"
```

---

## Task 2: 创建 SprintLane 泳道组件

**Files:**
- Create: `frontend/src/components/sprint/SprintLane.vue`

- [ ] **Step 1: 创建泳道组件基础结构**

```vue
<template>
  <div
    class="sprint-lane"
    :class="{ 'is-drag-over': isDragOver }"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <!-- 泳道头部 -->
    <div class="lane-header">
      <span class="lane-name">{{ lane.name }}</span>
      <span class="lane-count">{{ lane.taskCount }}</span>
    </div>

    <!-- 任务列表 -->
    <div class="lane-tasks">
      <div
        v-for="task in lane.tasks"
        :key="task.id"
        class="task-item"
        draggable="true"
        @dragstart="onDragStart($event, task)"
      >
        <el-checkbox
          :model-value="selectedTasks.includes(task.id)"
          @change="onSelect(task.id)"
        />
        <span class="task-title">{{ task.title }}</span>
        <span class="task-type">{{ task.type }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  lane: { type: Object, required: true }
})

const emit = defineEmits(['drop', 'select-task', 'select'])

const isDragOver = ref(false)
const selectedTasks = ref([])

const onDragOver = () => { isDragOver.value = true }
const onDragLeave = () => { isDragOver.value = false }
const onDrop = (e) => {
  isDragOver.value = false
  const taskId = parseInt(e.dataTransfer.getData('taskId'))
  emit('drop', { taskId, targetSprintId: props.lane.id })
}
const onDragStart = (e, task) => {
  e.dataTransfer.setData('taskId', task.id)
}
const onSelect = (taskId) => {
  const idx = selectedTasks.value.indexOf(taskId)
  if (idx >= 0) {
    selectedTasks.value.splice(idx, 1)
  } else {
    selectedTasks.value.push(taskId)
  }
  emit('select', selectedTasks.value)
}
</script>
```

- [ ] **Step 2: 创建目录结构**

```bash
mkdir -p frontend/src/components/sprint
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/sprint/SprintLane.vue
git commit -m "feat: create SprintLane component"
```

---

## Task 3: 添加 BatchActionBar 批量操作栏组件

**Files:**
- Create: `frontend/src/components/sprint/BatchActionBar.vue`

- [ ] **Step 1: 创建批量操作栏组件**

```vue
<template>
  <div class="batch-action-bar">
    <span class="selected-count">{{ $t('project.selectedTasks', { count: selectedCount }) }}</span>
    <el-select v-model="targetSprintId" :placeholder="$t('project.selectSprint')">
      <el-option :label="$t('project.backlog')" :value="null" />
      <el-option
        v-for="sprint in sprints"
        :key="sprint.id"
        :label="sprint.name"
        :value="sprint.id"
      />
    </el-select>
    <el-button @click="emit('batch-assign', targetSprintId)">
      {{ $t('project.assignToSprint') }}
    </el-button>
    <el-button @click="emit('batch-remove')">
      {{ $t('project.removeFromSprint') }}
    </el-button>
    <el-button @click="emit('clear-selection')">
      {{ $t('common.cancel') }}
    </el-button>
  </div>
</template>

<script setup>
const props = defineProps({
  selectedCount: { type: Number, required: true },
  sprints: { type: Array, default: () => [] }
})
const emit = defineEmits(['batch-assign', 'batch-remove', 'clear-selection'])
const targetSprintId = ref(null)
</script>
```

- [ ] **Step 2: 添加 i18n keys**

Modify: `frontend/src/locales/zh-CN.js`

```javascript
project: {
  // ... existing
  selectedTasks: '已选择 {count} 个任务',
  assignToSprint: '分配到冲刺',
  removeFromSprint: '从冲刺移出',
  backlog: 'Backlog',
  selectSprint: '选择冲刺'
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/sprint/BatchActionBar.vue
git add frontend/src/locales/zh-CN.js frontend/src/locales/en-US.js
git commit -m "feat: add BatchActionBar component"
```

---

## Task 4: 实现拖拽功能

**Files:**
- Modify: `frontend/src/views/project/SprintManagement.vue`
- Modify: `frontend/src/components/sprint/SprintLane.vue`

- [ ] **Step 1: 实现 onDropTask 处理**

Modify `SprintManagement.vue` - 添加 `onDropTask` 方法：

```javascript
const onDropTask = async ({ taskId, targetSprintId }) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task || task.sprintId === targetSprintId) return

  try {
    await updateTask(taskId, { sprintId: targetSprintId })
    task.sprintId = targetSprintId
    ElMessage.success(t('project.taskMoved'))
  } catch (e) {
    ElMessage.error(t('project.taskMoveFailed'))
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/project/SprintManagement.vue
git commit -m "feat: implement drag-drop between lanes"
```

---

## Task 5: 实现批量操作 API（后端）

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/controller/SprintController.java`
- Modify: `backend/src/main/java/com/sme/pm/service/impl/SprintServiceImpl.java`

- [ ] **Step 1: 添加批量操作 Controller 方法**

```java
@PostMapping("/{sprintId}/tasks/batch")
public Result<Map<String, Integer>> batchAddTasks(
    @PathVariable Long sprintId,
    @RequestBody Map<String, List<Long>> body) {
    List<Long> taskIds = body.get("taskIds");
    int count = sprintService.batchAddTasks(sprintId, taskIds);
    return Result.success(Map.of("movedCount", count));
}

@DeleteMapping("/{sprintId}/tasks/batch")
public Result<Map<String, Integer>> batchRemoveTasks(
    @PathVariable Long sprintId,
    @RequestBody Map<String, List<Long>> body) {
    List<Long> taskIds = body.get("taskIds");
    int count = sprintService.batchRemoveTasks(sprintId, taskIds);
    return Result.success(Map.of("movedCount", count));
}
```

- [ ] **Step 2: 添加 Service 方法**

```java
public int batchAddTasks(Long sprintId, List<Long> taskIds) {
    if (taskIds == null || taskIds.isEmpty()) return 0;
    for (Long taskId : taskIds) {
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setSprintId(sprintId);
            taskMapper.updateById(task);
        }
    }
    return taskIds.size();
}

public int batchRemoveTasks(Long sprintId, List<Long> taskIds) {
    if (taskIds == null || taskIds.isEmpty()) return 0;
    for (Long taskId : taskIds) {
        Task task = taskMapper.selectById(taskId);
        if (task != null && sprintId.equals(task.getSprintId())) {
            task.setSprintId(null);
            taskMapper.updateById(task);
        }
    }
    return taskIds.size();
}
```

- [ ] **Step 3: 添加单元测试**

Create: `backend/src/test/java/com/sme/pm/service/impl/SprintServiceImplTest.java`

```java
@Test
void batchAddTasks_shouldMoveTasksToSprint() {
    Long sprintId = 1L;
    List<Long> taskIds = Arrays.asList(10L, 11L, 12L);

    when(taskMapper.selectById(10L)).thenReturn(createTask(10L, null));
    when(taskMapper.selectById(11L)).thenReturn(createTask(11L, null));
    when(taskMapper.selectById(12L)).thenReturn(createTask(12L, null));
    when(taskMapper.updateById(any())).thenReturn(1);

    int count = sprintService.batchAddTasks(sprintId, taskIds);

    assertEquals(3, count);
    verify(taskMapper).updateById(argThat(t -> t.getSprintId().equals(sprintId)));
}
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/sme/pm/controller/SprintController.java
git add backend/src/main/java/com/sme/pm/service/impl/SprintServiceImpl.java
git add backend/src/test/java/com/sme/pm/service/impl/SprintServiceImplTest.java
git commit -m "feat: add batch task operations API"
```

---

## Task 6: 实现批量操作前端对接

**Files:**
- Modify: `frontend/src/views/project/SprintManagement.vue`
- Modify: `frontend/src/components/sprint/BatchActionBar.vue`
- Modify: `frontend/src/api/project.js`

- [ ] **Step 1: 添加 API 方法**

Modify: `frontend/src/api/project.js`

```javascript
export function batchAssignTasks(sprintId, taskIds) {
  return request.post(`/sprints/${sprintId}/tasks/batch`, { taskIds })
}

export function batchRemoveTasks(sprintId, taskIds) {
  return request.delete(`/sprints/${sprintId}/tasks/batch`, { taskIds })
}
```

- [ ] **Step 2: 实现批量操作方法**

Modify: `SprintManagement.vue`

```javascript
import { batchAssignTasks, batchRemoveTasks } from '@/api/project'

const selectedTaskIds = ref([])

const onBatchAssign = async (targetSprintId) => {
  try {
    await batchAssignTasks(targetSprintId, selectedTaskIds.value)
    // Update local state
    selectedTaskIds.value.forEach(id => {
      const task = tasks.value.find(t => t.id === id)
      if (task) task.sprintId = targetSprintId
    })
    selectedTaskIds.value = []
    ElMessage.success(t('project.tasksAssigned'))
  } catch (e) {
    ElMessage.error(t('project.assignFailed'))
  }
}

const onBatchRemove = async () => {
  // Get current sprint id from first selected task
  const firstTask = tasks.value.find(t => selectedTaskIds.value.includes(t.id))
  if (!firstTask?.sprintId) return

  try {
    await batchRemoveTasks(firstTask.sprintId, selectedTaskIds.value)
    selectedTaskIds.value.forEach(id => {
      const task = tasks.value.find(t => t.id === id)
      if (task) task.sprintId = null
    })
    selectedTaskIds.value = []
    ElMessage.success(t('project.tasksRemoved'))
  } catch (e) {
    ElMessage.error(t('project.removeFailed'))
  }
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/api/project.js
git add frontend/src/views/project/SprintManagement.vue
git commit -m "feat: implement batch operations UI"
```

---

## Task 7: 添加虚拟滚动支持

**Files:**
- Modify: `frontend/src/components/sprint/SprintLane.vue`
- Install: `vue-virtual-scroller`

- [ ] **Step 1: 安装虚拟滚动库**

```bash
cd frontend
npm install vue-virtual-scroller
```

- [ ] **Step 2: 修改 SprintLane 使用虚拟滚动**

```vue
<template>
  <div class="sprint-lane">
    <!-- ... header ... -->

    <div class="lane-tasks">
      <RecycleScroller
        class="scroller"
        :items="lane.tasks"
        :item-size="50"
        key-field="id"
        v-slot="{ item }"
      >
        <div class="task-item" draggable="true">
          <el-checkbox @change="onSelect(item.id)" />
          <span>{{ item.title }}</span>
        </div>
      </RecycleScroller>
    </div>
  </div>
</template>

<script setup>
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
</script>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/sprint/SprintLane.vue
git commit -m "feat: add virtual scrolling for large task lists"
```

---

## Task 8: 添加搜索与筛选功能

**Files:**
- Modify: `frontend/src/views/project/SprintManagement.vue`

- [ ] **Step 1: 添加搜索和筛选逻辑**

```javascript
const searchQuery = ref('')
const filterType = ref(null)
const filterPriority = ref(null)

const filteredTasks = computed(() => {
  let result = tasks.value

  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(t =>
      t.title?.toLowerCase().includes(q) ||
      t.description?.toLowerCase().includes(q)
    )
  }

  if (filterType.value) {
    result = result.filter(t => t.type === filterType.value)
  }

  if (filterPriority.value) {
    result = result.filter(t => t.priority === filterPriority.value)
  }

  return result
})
```

- [ ] **Step 2: 添加筛选 UI**

```vue
<div class="filters">
  <el-input v-model="searchQuery" :placeholder="$t('project.searchTasks')" />
  <el-select v-model="filterType" clearable>
    <el-option label="Task" value="TASK" />
    <el-option label="Story" value="STORY" />
    <!-- ... -->
  </el-select>
</div>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/project/SprintManagement.vue
git commit -m "feat: add search and filter functionality"
```

---

## Task 9: 添加侧边栏菜单入口

**Files:**
- Modify: `frontend/src/components/Layout.vue`

- [ ] **Step 1: 在侧边栏添加入口**

```vue
<el-sub-menu index="/project-settings">
  <template #title>
    <el-icon><Setting /></el-icon>
    <span>{{ $t('nav.projectSettings') }}</span>
  </template>
  <el-menu-item index="/projects/settings/sprint">
    <!-- ... existing items ... -->
  </el-menu-item>
  <!-- 新增 -->
  <el-menu-item index="/projects/sprint-management">
    <el-icon><Timer /></el-icon>
    <span>{{ $t('nav.sprintManagement') }}</span>
  </el-menu-item>
</el-sub-menu>
```

- [ ] **Step 2: 添加 i18n key**

Modify: `frontend/src/locales/zh-CN.js`

```javascript
nav: {
  // ... existing
  sprintManagement: '冲刺管理',
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/Layout.vue
git add frontend/src/locales/zh-CN.js frontend/src/locales/en-US.js
git commit -m "feat: add sprint management sidebar menu"
```

---

## Task 10: 集成测试与修复

**Files:**
- Create: `frontend/src/views/project/__tests__/SprintManagement.test.js`

- [ ] **Step 1: 编写集成测试**

```javascript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SprintManagement from '../SprintManagement.vue'

describe('SprintManagement', () => {
  it('should render lanes', () => {
    const wrapper = mount(SprintManagement, {
      global: { mocks: { $t: (k) => k } }
    })
    expect(wrapper.find('.sprint-lane').exists()).toBe(true)
  })
})
```

- [ ] **Step 2: 运行测试并修复问题**

Run: `cd frontend && npm test`

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/project/__tests__/SprintManagement.test.js
git commit -m "test: add SprintManagement tests"
```

---

## 验证清单

- [ ] 页面可以访问：`/projects/sprint-management/:projectId`
- [ ] 泳道正确显示 backlog 和所有冲刺
- [ ] 任务可以从 backlog 拖拽到冲刺
- [ ] 任务可以从冲刺拖拽到 backlog
- [ ] 任务可以在冲刺之间拖拽
- [ ] 批量选择任务
- [ ] 批量分配到冲刺
- [ ] 批量从冲刺移出
- [ ] 虚拟滚动正常工作（大量任务时）
- [ ] 搜索功能正常
- [ ] 筛选功能正常
- [ ] 侧边栏菜单入口正常
