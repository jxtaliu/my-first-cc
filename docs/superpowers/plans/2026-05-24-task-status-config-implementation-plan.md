# 任务状态配置实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 项目创建时自动从业务字典初始化任务状态配置，看板和冲刺页面统一从 task_status 表获取状态

**Architecture:**
1. 后端：ProjectService 在创建项目时调用 TaskStatusService 初始化状态
2. 后端：TaskStatusService 新增 initializeFromDict 和 reorder 方法
3. 前端：useKanban 改为直接从 task_status API 获取状态
4. 前端：StatusConfig 组件支持拖拽排序和条件删除

**Tech Stack:** Spring Boot + MyBatis-Plus, Vue 3 + Element Plus

---

## 文件结构

### 后端
- `backend/src/main/java/com/sme/pm/entity/TaskStatus.java` - 实体类
- `backend/src/main/java/com/sme/pm/mapper/TaskStatusMapper.java` - Mapper
- `backend/src/main/java/com/sme/pm/service/ITaskStatusService.java` - 服务接口
- `backend/src/main/java/com/sme/pm/service/impl/TaskStatusServiceImpl.java` - 服务实现
- `backend/src/main/java/com/sme/pm/controller/TaskStatusController.java` - 控制器
- `backend/src/main/java/com/sme/pm/service/impl/ProjectServiceImpl.java` - 项目服务（创建时调用初始化）

### 前端
- `frontend/src/composables/useKanban.js` - 看板逻辑
- `frontend/src/views/project/Settings/StatusConfig.vue` - 状态配置组件

### 数据库
- `backend/src/main/resources/schema.sql` - 确认字段
- `backend/src/main/resources/migration_pm_2026.sql` - 确认初始化数据

---

## Task 1: 后端 - TaskStatusService 新增 initializeFromDict 方法

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/service/ITaskStatusService.java`
- Modify: `backend/src/main/java/com/sme/pm/service/impl/TaskStatusServiceImpl.java`

- [ ] **Step 1: 在 ITaskStatusService.java 添加接口定义**

```java
/**
 * 从业务字典初始化项目任务状态
 * @param projectId 项目ID
 */
void initializeFromDict(String projectId);
```

- [ ] **Step 2: 在 TaskStatusServiceImpl.java 实现 initializeFromDict 方法**

```java
@Override
public void initializeFromDict(String projectId) {
    // 1. 查询 sys_dict_code 表，dict_type='task_status' 的所有字典项
    // 2. 遍历每项，创建 TaskStatus 记录，projectId 传入
    // 3. 字段映射：code, name(nameEn/nameZh), extra.color, sortOrder
}
```

- [ ] **Step 3: 注入 DictCodeMapper**

在 TaskStatusServiceImpl 中注入 DictCodeMapper 用于查询业务字典

- [ ] **Step 4: 提交**

```bash
git add backend/src/main/java/com/sme/pm/service/ITaskStatusService.java backend/src/main/java/com/sme/pm/service/impl/TaskStatusServiceImpl.java
git commit -m "feat(task-status): add initializeFromDict method"
```

---

## Task 2: 后端 - TaskStatusController 新增初始化和排序接口

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/controller/TaskStatusController.java`

- [ ] **Step 1: 添加初始化接口**

```java
@PostMapping("/init/{projectId}")
public Result<Void> initializeFromDict(@PathVariable String projectId) {
    taskStatusService.initializeFromDict(projectId);
    return Result.success();
}
```

- [ ] **Step 2: 添加批量排序接口**

```java
@PutMapping("/reorder")
public Result<Void> reorder(@RequestBody List<Long> statusIds) {
    // statusIds 数组顺序即为新的排序顺序
    taskStatusService.reorder(statusIds);
    return Result.success();
}
```

- [ ] **Step 3: 在 ITaskStatusService 添加 reorder 方法声明**

```java
void reorder(List<Long> statusIds);
```

- [ ] **Step 4: 在 TaskStatusServiceImpl 实现 reorder 方法**

```java
@Override
public void reorder(List<Long> statusIds) {
    for (int i = 0; i < statusIds.size(); i++) {
        TaskStatus status = new TaskStatus();
        status.setId(statusIds.get(i));
        status.setSortOrder(i + 1);
        taskStatusMapper.updateById(status);
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/sme/pm/controller/TaskStatusController.java backend/src/main/java/com/sme/pm/service/ITaskStatusService.java backend/src/main/java/com/sme/pm/service/impl/TaskStatusServiceImpl.java
git commit -m "feat(task-status): add init and reorder endpoints"
```

---

## Task 3: 后端 - ProjectService 创建项目时调用初始化

**Files:**
- Modify: `backend/src/main/java/com/sme/pm/service/impl/ProjectServiceImpl.java`

- [ ] **Step 1: 找到 create 方法，在创建成功后调用 initializeFromDict**

在 `create` 方法中，`projectService.create(project)` 成功后添加：
```java
// 初始化任务状态
taskStatusService.initializeFromDict(project.getProjectId());
```

- [ ] **Step 2: 确保 TaskStatusService 已注入**

检查是否已注入 `TaskStatusService`，如果没有需要添加

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/sme/pm/service/impl/ProjectServiceImpl.java
git commit -m "feat(project): initialize task statuses on project creation"
```

---

## Task 4: 前端 - useKanban 改为直接从 task_status API 获取

**Files:**
- Modify: `frontend/src/composables/useKanban.js`

- [ ] **Step 1: 修改 loadTaskStatuses 方法**

移除对 `getDictCodesByType('task_status')` 的调用，改为只调用 `getTaskStatusesByProject(projectId)`

```javascript
async function loadTaskStatuses(projectId) {
  loading.value = true
  try {
    // 直接从 task_status 表获取项目状态
    let statuses = []
    if (projectId) {
      const projectRes = await getTaskStatusesByProject(projectId)
      statuses = projectRes.data || projectRes
    }
    // Fallback to system statuses if project has no custom statuses
    if (!statuses || !Array.isArray(statuses) || statuses.length === 0) {
      const systemRes = await getSystemTaskStatuses()
      statuses = systemRes.data || systemRes
    }
    taskStatuses.value = Array.isArray(statuses) ? statuses : []
    // 直接使用 task_status 表数据生成列
    if (statuses.length > 0) {
      columns.value = statuses.map(s => ({
        id: s.code.toLowerCase(),
        status: s.code.toLowerCase(),
        title: s.name,
        color: s.color || '#94A3B8',
        statusId: s.id,
        sortOrder: s.sortOrder
      }))
    }
  } finally {
    loading.value = false
  }
}
```

- [ ] **Step 2: 移除不需要的导入**

移除 `getDictCodesByType` 的导入

- [ ] **Step 3: 提交**

```bash
git add frontend/src/composables/useKanban.js
git commit -m "refactor(useKanban): fetch statuses from task_status API only"
```

---

## Task 5: 前端 - StatusConfig 支持拖拽排序

**Files:**
- Modify: `frontend/src/views/project/Settings/StatusConfig.vue`

- [ ] **Step 1: 添加拖拽排序支持**

使用 Element Plus 的 `el-table` 配合第三方拖拽库（如 sortablejs）或使用 `vuedraggable`

- [ ] **Step 2: 实现拖拽后的排序更新**

```javascript
const onDragEnd = async () => {
  // 调用 PUT /api/v1/task-statuses/reorder 接口
  const statusIds = statusList.value.map(s => s.id)
  await reorderTaskStatuses(statusIds)
}
```

- [ ] **Step 3: 添加 reorder API 方法**

在 `frontend/src/api/taskStatus.js` 添加：
```javascript
export function reorderTaskStatuses(statusIds) {
  return request.put('/v1/task-statuses/reorder', statusIds)
}
```

- [ ] **Step 4: 提交**

```bash
git add frontend/src/views/project/Settings/StatusConfig.vue frontend/src/api/taskStatus.js
git commit -m "feat(StatusConfig): add drag-to-reorder support"
```

---

## Task 6: 前端 - StatusConfig 删除前检查任务引用

**Files:**
- Modify: `frontend/src/views/project/Settings/StatusConfig.vue`

- [ ] **Step 1: 修改删除逻辑**

在删除前调用 API 检查该状态下是否有任务：
```javascript
const onDelete = async (status) => {
  // 调用 GET /api/v1/tasks/count?statusId=xxx 获取任务数量
  // 如果有任务，提示用户不允许删除
  // 如果没有任务，执行删除
}
```

- [ ] **Step 2: 后端添加任务数量查询接口（如不存在）**

在 TaskController 或新建接口：
```java
@GetMapping("/count")
public Result<Long> countByStatus(@RequestParam Long statusId) {
    long count = taskService.countByStatusId(statusId);
    return Result.success(count);
}
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/views/project/Settings/StatusConfig.vue backend/src/main/java/com/sme/pm/controller/TaskController.java (if modified)
git commit -m "feat(StatusConfig): check task count before delete"
```

---

## Task 7: 验证和测试

- [ ] **Step 1: 创建新项目，验证 task_status 表中是否自动初始化了状态数据**

```sql
SELECT * FROM task_status WHERE project_id = '新项目的project_id';
```

- [ ] **Step 2: 访问项目看板，验证是否显示正确的状态列**

- [ ] **Step 3: 访问项目冲刺，验证是否显示正确的状态列**

- [ ] **Step 4: 在状态配置页签，验证拖拽排序是否生效**

- [ ] **Step 5: 在状态配置页签，验证有任务时不能删除状态**

---

## 执行方式选择

**计划已完成并保存到 `docs/superpowers/plans/2026-05-24-task-status-config-implementation-plan.md`**

两种执行方式：

**1. Subagent-Driven (推荐)** - 每个任务由新的 subagent 执行，任务间有审核点

**2. Inline Execution** - 在当前 session 中连续执行，带检查点

选择哪种方式？
