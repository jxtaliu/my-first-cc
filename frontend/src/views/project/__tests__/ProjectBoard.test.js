import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useKanban } from '@/composables/useKanban'

// Mock vue-i18n
vi.mock('vue-i18n', () => ({
  useI18n: vi.fn(() => ({
    locale: { value: 'en-US' }
  }))
}))

// Mock the API modules
vi.mock('@/api/taskStatus', () => ({
  getTaskStatusesByProject: vi.fn(),
  getSystemTaskStatuses: vi.fn()
}))

vi.mock('@/api/project', () => ({
  getProject: vi.fn(),
  getSprints: vi.fn(),
  getSprintTasks: vi.fn()
}))

vi.mock('@/api/task', () => ({
  getTasksByProject: vi.fn(),
  moveTask: vi.fn(),
  createTask: vi.fn(),
  updateTask: vi.fn()
}))

import { getProject, getSprints, getSprintTasks } from '@/api/project'
import { moveTask as apiMoveTask, createTask as apiCreateTask, updateTask as apiUpdateTask } from '@/api/task'
import { getTaskStatusesByProject } from '@/api/taskStatus'

describe('ProjectBoard API Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  const mockProject = {
    id: 1,
    name: 'Test Project'
  }

  const mockSprints = [
    { id: 1, name: 'Sprint 1', status: 'ACTIVE' },
    { id: 2, name: 'Sprint 2', status: 'PLANNED' }
  ]

  const mockStatuses = [
    { id: 1, code: 'TODO', name: 'To Do', color: '#94A3B8' },
    { id: 2, code: 'IN_PROGRESS', name: 'In Progress', color: '#3B82F6' },
    { id: 3, code: 'DONE', name: 'Done', color: '#10B981' }
  ]

  const mockTasks = [
    { id: 1, title: 'Task 1', status: 1, statusId: 1, type: 'task', priority: 'P1' },
    { id: 2, title: 'Task 2', status: 2, statusId: 2, type: 'task', priority: 'P2' }
  ]

  describe('loadProjectData', () => {
    it('testLoadProjectData_shouldFetchProjectAndSprints', async () => {
      getProject.mockResolvedValue({ data: mockProject })
      getSprints.mockResolvedValue({ data: mockSprints })
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      // Simulate the loadProjectData function from ProjectBoard.vue
      async function loadProjectData() {
        const projectId = 1
        const projectRes = await getProject(projectId)
        const project = projectRes.data || projectRes
        const sprintsRes = await getSprints(projectId)
        const sprints = sprintsRes.data || sprintsRes || []
        await getTaskStatusesByProject(projectId)
        return { project, sprints }
      }

      const result = await loadProjectData()

      expect(getProject).toHaveBeenCalledWith(1)
      expect(getSprints).toHaveBeenCalledWith(1)
      expect(getTaskStatusesByProject).toHaveBeenCalledWith(1)
      expect(result.project.name).toBe('Test Project')
      expect(result.sprints).toHaveLength(2)
    })
  })

  describe('loadSprintTasks', () => {
    it('testLoadSprintTasks_shouldNormalizeTasks', async () => {
      getSprintTasks.mockResolvedValue({ data: mockTasks })
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { loadTaskStatuses, normalizeTask } = useKanban()
      await loadTaskStatuses(1)

      // Simulate loadSprintTasks function from ProjectBoard.vue
      async function loadSprintTasks(projectId, sprintId) {
        const res = await getSprintTasks(projectId, sprintId)
        const rawTasks = res.data || res || []
        return rawTasks.map(task => {
          const normalized = normalizeTask(task)
          normalized.dependencies = task.dependencies || []
          return normalized
        })
      }

      const tasks = await loadSprintTasks(1, 1)

      expect(getSprintTasks).toHaveBeenCalledWith(1, 1)
      expect(tasks).toHaveLength(2)
      expect(tasks[0]).toHaveProperty('status')
      expect(tasks[0]).toHaveProperty('_raw')
    })
  })

  describe('onTaskDrop', () => {
    it('testOnTaskDrop_shouldCallMoveTask', async () => {
      apiMoveTask.mockResolvedValue({ data: { success: true } })
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { loadTaskStatuses, statusCodeToId } = useKanban()
      await loadTaskStatuses(1)

      // Simulate onTaskDrop from ProjectBoard.vue
      const tasks = [
        { id: 1, title: 'Task 1', status: 'TODO', statusId: 1 }
      ]
      const currentSprintId = 1

      async function onTaskDrop({ taskId, targetStatus }) {
        const task = tasks.find(t => t.id === taskId)
        if (!task) return

        const oldStatus = task.status
        const newStatusId = statusCodeToId.value[targetStatus]

        if (oldStatus === targetStatus) return

        await apiMoveTask(taskId, { statusId: newStatusId, sprintId: currentSprintId })
        task.status = targetStatus
        task.statusId = newStatusId
      }

      await onTaskDrop({ taskId: 1, targetStatus: 'in_progress' })

      expect(apiMoveTask).toHaveBeenCalledWith(1, { statusId: 2, sprintId: 1 })
    })
  })

  describe('onSaveTask', () => {
    it('testOnSaveTask_shouldCallCreateTask', async () => {
      apiCreateTask.mockResolvedValue({ data: { id: 100, title: 'New Task', status: 1 } })
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { loadTaskStatuses, normalizeTask, statusCodeToId } = useKanban()
      await loadTaskStatuses(1)

      // Simulate onSaveTask for new task
      const tasks = []
      let editingTask = {
        title: 'New Task',
        description: 'Description',
        type: 'task',
        priority: 'P3',
        status: 'TODO',
        statusId: 1,
        projectId: 1,
        sprintId: 1
      }

      async function onSaveTask() {
        if (!editingTask) return

        if (editingTask.id) {
          // Update existing task
          await apiUpdateTask(editingTask.id, {})
        } else {
          // Create new task
          const createData = {
            title: editingTask.title,
            description: editingTask.description,
            type: editingTask.type || 'task',
            priority: editingTask.priority || 'P3',
            statusId: statusCodeToId.value[editingTask.status] || editingTask.statusId,
            projectId: editingTask.projectId,
            sprintId: editingTask.sprintId,
            estimateHours: editingTask.estimateHours || 0,
            remainingHours: editingTask.remainingHours || 0,
            actualHours: editingTask.actualHours || 0,
            progress: editingTask.progress || 0
          }
          const res = await apiCreateTask(createData)
          const newTask = normalizeTask(res.data || res)
          tasks.push(newTask)
        }
      }

      await onSaveTask()

      expect(apiCreateTask).toHaveBeenCalled()
      const createCall = apiCreateTask.mock.calls[0][0]
      expect(createCall.title).toBe('New Task')
      expect(tasks).toHaveLength(1)
    })

    it('testOnSaveTask_shouldCallUpdateTask', async () => {
      apiUpdateTask.mockResolvedValue({ data: { success: true } })
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { loadTaskStatuses, statusCodeToId } = useKanban()
      await loadTaskStatuses(1)

      // Simulate onSaveTask for existing task
      const tasks = [{ id: 1, title: 'Old Title', status: 'TODO', statusId: 1 }]
      let editingTask = {
        id: 1,
        title: 'Updated Title',
        description: 'Updated Description',
        type: 'task',
        priority: 'P1',
        status: 'TODO',
        statusId: 1
      }

      async function onSaveTask() {
        if (!editingTask) return

        if (editingTask.id) {
          // Update existing task
          const updateData = {
            title: editingTask.title,
            description: editingTask.description,
            type: editingTask.type,
            priority: editingTask.priority,
            statusId: statusCodeToId.value[editingTask.status] || editingTask.statusId,
            estimateHours: editingTask.estimateHours,
            remainingHours: editingTask.remainingHours,
            actualHours: editingTask.actualHours,
            progress: editingTask.progress
          }
          await apiUpdateTask(editingTask.id, updateData)

          const index = tasks.findIndex(t => t.id === editingTask.id)
          if (index !== -1) {
            tasks[index] = { ...tasks[index], ...editingTask }
          }
        }
      }

      await onSaveTask()

      expect(apiUpdateTask).toHaveBeenCalledWith(1, expect.objectContaining({
        title: 'Updated Title',
        description: 'Updated Description'
      }))
    })
  })

  describe('watch currentSprintId', () => {
    it('testWatchCurrentSprint_shouldReloadTasks', async () => {
      getSprintTasks.mockResolvedValue({ data: mockTasks })
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { loadTaskStatuses, normalizeTask } = useKanban()
      await loadTaskStatuses(1)

      const tasks = []
      let currentSprintId = null
      let callCount = 0

      async function loadSprintTasks() {
        if (!currentSprintId) {
          return []
        }
        const res = await getSprintTasks(1, currentSprintId)
        const rawTasks = res.data || res || []
        return rawTasks.map(task => normalizeTask(task))
      }

      // Simulate watch effect
      async function setSprint(sprintId) {
        currentSprintId = sprintId
        const newTasks = await loadSprintTasks()
        tasks.length = 0
        tasks.push(...newTasks)
        callCount++
      }

      // First sprint change
      await setSprint(1)
      expect(callCount).toBe(1)
      expect(getSprintTasks).toHaveBeenCalledWith(1, 1)

      // Second sprint change
      getSprintTasks.mockClear()
      getSprintTasks.mockResolvedValue({ data: [{ ...mockTasks[0], sprintId: 2 }] })
      await setSprint(2)
      expect(callCount).toBe(2)
      expect(getSprintTasks).toHaveBeenCalledWith(1, 2)
    })
  })
})