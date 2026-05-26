# Milestone Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement project milestone management - a simple milestone list with due dates, manual completion, and Gantt/timeline views.

**Architecture:** Project-level milestones (no cross-project). Backend has existing Milestone entity and basic CRUD; frontend has Milestones.vue with mock data and GanttView.vue for tasks. Need to connect to real API and add milestone views.

**Tech Stack:** Vue 3, Element Plus, Spring Boot, MyBatis-Plus

---

## Files to Modify/Create

### Backend
- Modify: `backend/src/main/java/com/sme/pm/entity/Milestone.java` - Adjust status values
- Modify: `backend/src/main/java/com/sme/pm/controller/MilestoneController.java` - Add complete endpoint, adjust project-milestone API
- Modify: `backend/src/main/java/com/sme/pm/service/impl/MilestoneServiceImpl.java` - Add completeMilestone method
- Modify: `backend/src/main/resources/schema.sql` - Add milestone table if needed
- Modify: `backend/src/main/resources/migration_pm_2026.sql` - Add milestone data if needed

### Frontend
- Modify: `frontend/src/api/milestone.js` - Add completeMilestone API
- Modify: `frontend/src/views/project/Milestones.vue` - Connect to real API, implement list management
- Modify: `frontend/src/views/project/GanttView.vue` - Add milestone view mode
- Modify: `frontend/src/locales/zh-CN.js` - Add milestone i18n keys
- Modify: `frontend/src/locales/en-US.js` - Add milestone i18n keys

---

## Task 1: Backend - Add milestone status values and complete endpoint

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/entity/Milestone.java:24`
- Modify: `backend/src/main/java/com/sme/pm/controller/MilestoneController.java`
- Modify: `backend/src/main/java/com/sme/pm/service/IMilestoneService.java`
- Modify: `backend/src/main/java/com/sme/pm/service/impl/MilestoneServiceImpl.java`

- [ ] **Step 1: Update Milestone entity status values**

Change status from `ACTIVE, ACHIEVED, FAILED` to `PLANNING, IN_PROGRESS, COMPLETED`.

```java
private String status;  // PLANNING, IN_PROGRESS, COMPLETED
```

- [ ] **Step 2: Add completeMilestone to IMilestoneService**

```java
void completeMilestone(Long id);
```

- [ ] **Step 3: Implement completeMilestone in MilestoneServiceImpl**

```java
@Override
public void completeMilestone(Long id) {
    Milestone milestone = getById(id);
    if (milestone == null) {
        throw new IllegalArgumentException("Milestone not found");
    }
    milestone.setStatus("COMPLETED");
    updateById(milestone);
}
```

- [ ] **Step 4: Add complete endpoint to MilestoneController**

```java
@PutMapping("/{id}/complete")
public Result<Milestone> complete(@PathVariable Long id) {
    milestoneService.completeMilestone(id);
    return Result.success(milestoneService.getById(id));
}
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/sme/pm/entity/Milestone.java
git add backend/src/main/java/com/sme/pm/service/IMilestoneService.java
git add backend/src/main/java/com/sme/pm/service/impl/MilestoneServiceImpl.java
git add backend/src/main/java/com/sme/pm/controller/MilestoneController.java
git commit -m "feat: add milestone status values and complete endpoint"
```

---

## Task 2: Frontend - Add completeMilestone API

**Files:**
- Modify: `frontend/src/api/milestone.js`

- [ ] **Step 1: Add completeMilestone export**

```javascript
export function completeMilestone(id) {
  return request.put(`/v1/milestones/${id}/complete`)
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/milestone.js
git commit -m "feat: add completeMilestone API"
```

---

## Task 3: Frontend - Connect Milestones.vue to real API

**Files:**
- Modify: `frontend/src/views/project/Milestones.vue`
- Modify: `frontend/src/locales/zh-CN.js`
- Modify: `frontend/src/locales/en-US.js`

**Milestones.vue current state:** Uses mock data, has cross-project tabs (need to remove), has create/edit dialog.

Changes needed:
1. Remove cross-project tabs (project-level only)
2. Connect to real API: loadMilestones, createMilestone, updateMilestone, deleteMilestone, completeMilestone
3. Add status filter (All / Planning / In Progress / Completed)
4. Add "Mark Complete" button to milestone card
5. Remove "relatedProjects" field (no task association per spec)

- [ ] **Step 1: Update imports**

```javascript
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import MilestoneCard from '@/components/charts/MilestoneCard.vue'
import { useProjectStore } from '@/stores/project'
import {
  getMilestonesByProject,
  createMilestone,
  updateMilestone,
  deleteMilestone,
  completeMilestone
} from '@/api/milestone'
```

- [ ] **Step 2: Add projectStore and refactor loadMilestones**

```javascript
const projectStore = useProjectStore()

const milestones = ref([])
const loading = ref(false)
const activeTab = ref('all')  // Will be used as status filter
const filterStatus = ref(null)  // null = all, PLANNING, IN_PROGRESS, COMPLETED

const loadMilestones = async () => {
  const projectId = projectStore.currentProjectId
  if (!projectId) {
    milestones.value = []
    return
  }
  loading.value = true
  try {
    const res = await getMilestonesByProject(projectId)
    milestones.value = res.data || []
  } catch (e) {
    console.error('Failed to load milestones:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMilestones()
})

watch(() => projectStore.currentProjectId, () => {
  loadMilestones()
})
```

- [ ] **Step 3: Simplify milestone list (remove cross-project tabs, add status filter)**

Replace tabs with status filter:

```html
<div class="milestones-tabs">
  <el-select v-model="filterStatus" :placeholder="$t('project.filterByStatus')" clearable>
    <el-option :label="$t('project.allMilestones')" :value="null" />
    <el-option :label="$t('project.planning')" value="PLANNING" />
    <el-option :label="$t('project.inProgress')" value="IN_PROGRESS" />
    <el-option :label="$t('project.completed')" value="COMPLETED" />
  </el-select>
  <el-button type="primary" @click="onCreateMilestone">
    <el-icon><Plus /></el-icon>
    {{ $t('project.createMilestone') }}
  </el-button>
</div>
```

- [ ] **Step 4: Simplify filtered milestones computed**

```javascript
const filteredMilestones = computed(() => {
  let result = milestones.value
  if (filterStatus.value) {
    result = result.filter(m => m.status === filterStatus.value)
  }
  return result
})
```

- [ ] **Step 5: Add Mark Complete and Delete actions to milestone card**

Add handlers:

```javascript
const onMarkComplete = async (milestone) => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmCompleteMilestone'),
      t('project.completeMilestone'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await completeMilestone(milestone.id)
    ElMessage.success(t('project.milestoneCompleted'))
    loadMilestones()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('project.completeFailed'))
    }
  }
}

const onDeleteMilestone = async (milestone) => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmDeleteMilestone'),
      t('project.deleteMilestone'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await deleteMilestone(milestone.id)
    ElMessage.success(t('project.milestoneDeleted'))
    loadMilestones()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('project.deleteFailed'))
    }
  }
}
```

- [ ] **Step 6: Update MilestoneCard click handler to show actions**

Change MilestoneCard click to show dropdown menu with Edit/Mark Complete/Delete options.

- [ ] **Step 7: Add i18n keys**

zh-CN.js:
```javascript
planning: 'Planning',
inProgress: 'In Progress',
completed: 'Completed',
confirmCompleteMilestone: 'Are you sure you want to mark this milestone as complete?',
completeMilestone: 'Complete Milestone',
milestoneCompleted: 'Milestone marked as complete',
completeFailed: 'Failed to complete milestone',
confirmDeleteMilestone: 'Are you sure you want to delete this milestone?',
milestoneDeleted: 'Milestone deleted',
deleteFailed: 'Failed to delete milestone',
filterByStatus: 'Filter by Status',
```

en-US.js:
```javascript
planning: 'Planning',
inProgress: 'In Progress',
completed: 'Completed',
confirmCompleteMilestone: 'Are you sure you want to mark this milestone as complete?',
completeMilestone: 'Complete Milestone',
milestoneCompleted: 'Milestone marked as complete',
completeFailed: 'Failed to complete milestone',
confirmDeleteMilestone: 'Are you sure you want to delete this milestone?',
milestoneDeleted: 'Milestone deleted',
deleteFailed: 'Failed to delete milestone',
filterByStatus: 'Filter by Status',
```

- [ ] **Step 8: Commit**

```bash
git add frontend/src/views/project/Milestones.vue
git add frontend/src/api/milestone.js
git add frontend/src/locales/zh-CN.js
git add frontend/src/locales/en-US.js
git commit -m "feat: connect Milestones.vue to real API with status management"
```

---

## Task 4: Frontend - Add milestone view to GanttView

**Files:**
- Modify: `frontend/src/views/project/GanttView.vue`

Add view mode selector (Tasks / Milestones) and render milestones in timeline when in milestone mode.

- [ ] **Step 1: Add view mode state**

```javascript
const viewMode = ref('tasks')  // 'tasks' or 'milestones'
```

- [ ] **Step 2: Add view mode selector to header**

```html
<el-select v-model="viewMode" :placeholder="$t('project.viewMode')" style="width: 120px">
  <el-option :label="$t('project.tasks')" value="tasks" />
  <el-option :label="$t('project.milestones')" value="milestones" />
</el-select>
```

- [ ] **Step 3: Add milestone loading and data**

```javascript
const milestoneLoading = ref(false)
const milestones = ref([])

const loadMilestones = async () => {
  const projectId = projectStore.currentProjectId
  if (!projectId) return
  milestoneLoading.value = true
  try {
    const res = await getMilestonesByProject(projectId)
    milestones.value = res.data || []
  } catch (e) {
    console.error('Failed to load milestones:', e)
  } finally {
    milestoneLoading.value = false
  }
}

watch(() => projectStore.currentProjectId, () => {
  if (viewMode.value === 'milestones') {
    loadMilestones()
  }
})

watch(viewMode, (newMode) => {
  if (newMode === 'milestones') {
    loadMilestones()
  }
})
```

- [ ] **Step 4: Add milestone rendering to timeline**

In the timeline body, add milestone bars when viewMode === 'milestones':

```html
<div v-if="viewMode === 'milestones'" class="milestone-gantt">
  <div
    v-for="milestone in milestones"
    :key="milestone.id"
    class="milestone-gantt-row"
  >
    <div class="milestone-gantt-label">{{ milestone.name }}</div>
    <div class="milestone-gantt-bar-container">
      <div
        class="milestone-gantt-bar"
        :class="'status-' + milestone.status?.toLowerCase()"
        :style="getMilestoneBarStyle(milestone)"
      >
        {{ milestone.name }}
      </div>
    </div>
  </div>
</div>
```

- [ ] **Step 5: Add getMilestoneBarStyle function**

```javascript
const getMilestoneBarStyle = (milestone) => {
  if (!milestone.targetDate) return {}
  const today = new Date()
  const targetDate = new Date(milestone.targetDate)
  const daysDiff = Math.floor((targetDate - today) / (1000 * 60 * 60 * 24))
  // Position from today
  const left = Math.max(0, daysDiff) * dayWidth  // pixels per day
  return {
    left: left + 'px',
    backgroundColor: milestone.status === 'COMPLETED' ? '#10B981' :
                     daysDiff < 0 ? '#EF4444' : '#3B82F6'
  }
}
```

- [ ] **Step 6: Add i18n keys**

```javascript
viewMode: 'View Mode',
tasks: 'Tasks',
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/views/project/GanttView.vue
git add frontend/src/locales/zh-CN.js
git add frontend/src/locales/en-US.js
git commit -m "feat: add milestone view mode to GanttView"
```

---

## Task 5: Frontend - Add milestone timeline view

**Files:**
- Create: `frontend/src/views/project/MilestoneTimeline.vue`
- Modify: `frontend/src/router/index.js`

Timeline view shows milestones on a horizontal timeline with today marker.

- [ ] **Step 1: Create MilestoneTimeline.vue**

```vue
<template>
  <div class="milestone-timeline-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('project.milestoneTimeline') }}</h1>
    </div>
    <div class="timeline-container" v-loading="loading">
      <div class="timeline-header">
        <div class="timeline-today" :style="{ left: todayPosition + 'px' }">
          <div class="today-label">{{ $t('project.today') }}</div>
          <div class="today-line"></div>
        </div>
      </div>
      <div class="timeline-body">
        <div
          v-for="milestone in milestones"
          :key="milestone.id"
          class="timeline-milestone"
        >
          <div class="milestone-name">{{ milestone.name }}</div>
          <div class="milestone-dots">
            <div
              class="milestone-dot"
              :class="'status-' + milestone.status?.toLowerCase()"
              :style="{ left: getMilestonePosition(milestone) + 'px' }"
              :title="milestone.targetDate"
            >
              <span class="dot-date">{{ formatDate(milestone.targetDate) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useProjectStore } from '@/stores/project'
import { getMilestonesByProject } from '@/api/milestone'

const { t } = useI18n()
const projectStore = useProjectStore()
const loading = ref(false)
const milestones = ref([])

const todayPosition = computed(() => {
  return 200  // Fixed position for today marker
})

const timelineSpan = 90  // days
const dayWidth = 20  // pixels per day

const getMilestonePosition = (milestone) => {
  if (!milestone.targetDate) return 0
  const today = new Date()
  const target = new Date(milestone.targetDate)
  const daysDiff = Math.floor((target - today) / (1000 * 60 * 60 * 24))
  return todayPosition + (daysDiff * dayWidth)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const loadMilestones = async () => {
  const projectId = projectStore.currentProjectId
  if (!projectId) return
  loading.value = true
  try {
    const res = await getMilestonesByProject(projectId)
    milestones.value = res.data || []
  } catch (e) {
    console.error('Failed to load milestones:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMilestones()
})

watch(() => projectStore.currentProjectId, () => {
  loadMilestones()
})
</script>

<style scoped>
.timeline-container {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
}
.timeline-header {
  position: relative;
  height: 40px;
  border-bottom: 1px solid var(--el-border-color);
}
.timeline-today {
  position: absolute;
  top: 0;
}
.today-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.today-line {
  width: 1px;
  height: 400px;
  background: var(--el-color-primary);
  opacity: 0.5;
}
.timeline-body {
  padding-top: 20px;
}
.timeline-milestone {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.milestone-name {
  width: 200px;
  font-size: 14px;
}
.milestone-dots {
  flex: 1;
  position: relative;
  height: 24px;
}
.milestone-dot {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  top: 6px;
}
.milestone-dot.status-planning { background: #3B82F6; }
.milestone-dot.status-in_progress { background: #F59E0B; }
.milestone-dot.status-completed { background: #10B981; }
.dot-date {
  position: absolute;
  top: 16px;
  left: -20px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
</style>
```

- [ ] **Step 2: Add route to router**

```javascript
{
  path: 'projects/milestone-timeline',
  name: 'MilestoneTimeline',
  component: () => import('@/views/project/MilestoneTimeline.vue')
}
```

- [ ] **Step 3: Add i18n keys**

```javascript
milestoneTimeline: 'Milestone Timeline',
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/project/MilestoneTimeline.vue
git add frontend/src/router/index.js
git add frontend/src/locales/zh-CN.js
git add frontend/src/locales/en-US.js
git commit -m "feat: add milestone timeline view"
```

---

## Verification

1. **Milestone List Page** (`/projects/milestones`)
   - Create milestone with name, description, due date
   - Edit milestone
   - Mark milestone as complete
   - Delete milestone
   - Filter by status (All / Planning / In Progress / Completed)
   - Switch project - milestones list should refresh

2. **Gantt View** (`/projects/gantt`)
   - Select "Milestones" view mode
   - Milestones display on timeline
   - Different colors for COMPLETED vs pending vs overdue

3. **Timeline View** (`/projects/milestone-timeline`)
   - Milestones display on horizontal timeline
   - Today marker visible
   - Milestone dots positioned by due date

---

## Spec Coverage Checklist

| Requirement | Task |
|-------------|------|
| Just a due date (no tasks) | Task 3 (simplified form) |
| Project-level only (no cross-project) | Task 3 (removed cross-project tabs) |
| Manual mark complete | Task 1, 3 (completeMilestone API + button) |
| List management | Task 3 (CRUD operations) |
| Gantt view | Task 4 (milestone view mode) |
| Timeline view | Task 5 (MilestoneTimeline) |
| Project switch refresh | Task 3 (watch projectStore.currentProjectId) |
