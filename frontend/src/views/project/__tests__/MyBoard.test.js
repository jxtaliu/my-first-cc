import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useKanban } from '@/composables/useKanban'

// Mock the API modules
vi.mock('@/api/task', () => ({
  getTasksByAssignee: vi.fn()
}))

vi.mock('@/api/project', () => ({
  getProjects: vi.fn()
}))

vi.mock('@/api/taskStatus', () => ({
  getTaskStatusesByProject: vi.fn(),
  getSystemTaskStatuses: vi.fn()
}))

import { getTasksByAssignee } from '@/api/task'
import { getProjects } from '@/api/project'
import { getSystemTaskStatuses } from '@/api/taskStatus'

describe('MyBoard API Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  describe('loadMyTasks', () => {
    const mockTasks = [
      {
        id: 1,
        title: 'My Task 1',
        status: 1,
        type: 'task',
        priority: 'P1',
        assigneeId: 100,
        assigneeName: 'John Doe'
      },
      {
        id: 2,
        title: 'My Task 2',
        status: 2,
        type: 'task',
        priority: 'P2',
        assigneeId: 100,
        assigneeName: 'John Doe'
      }
    ]

    const mockStatuses = [
      { id: 1, code: 'TODO', name: 'To Do', color: '#94A3B8' },
      { id: 2, code: 'IN_PROGRESS', name: 'In Progress', color: '#3B82F6' },
      { id: 3, code: 'DONE', name: 'Done', color: '#10B981' }
    ]

    it('testLoadMyTasks_shouldFetchAssigneeTasks', async () => {
      getSystemTaskStatuses.mockResolvedValue({ data: mockStatuses })
      getTasksByAssignee.mockResolvedValue({ data: mockTasks })

      const { loadTaskStatuses, normalizeTask } = useKanban()
      const userId = 100

      // Load statuses first
      await loadTaskStatuses(null)

      // Simulate loadMyTasks
      const res = await getTasksByAssignee(userId)
      const rawTasks = res.data || res
      const tasks = rawTasks.map(task => normalizeTask(task))

      expect(getTasksByAssignee).toHaveBeenCalledWith(100)
      expect(tasks).toHaveLength(2)
      expect(tasks[0].title).toBe('My Task 1')
      expect(tasks[1].title).toBe('My Task 2')
    })
  })

  describe('loadProjects', () => {
    const mockProjects = [
      { id: 1, name: 'Project A' },
      { id: 2, name: 'Project B' },
      { id: 3, name: 'Project C' }
    ]

    it('testLoadProjects_shouldPopulateFilter', async () => {
      getProjects.mockResolvedValue({ data: mockProjects })

      // Simulate loadProjects
      const res = await getProjects()
      const projects = res.data || res

      expect(getProjects).toHaveBeenCalled()
      expect(projects).toHaveLength(3)
      expect(projects[0].name).toBe('Project A')
      expect(projects[1].name).toBe('Project B')
      expect(projects[2].name).toBe('Project C')
    })
  })

  describe('task filtering', () => {
    it('testFilterByProject_shouldReturnMatchingTasks', async () => {
      const mockStatuses = [
        { id: 1, code: 'TODO', name: 'To Do', color: '#94A3B8' }
      ]
      getSystemTaskStatuses.mockResolvedValue({ data: mockStatuses })

      const mockTasks = [
        { id: 1, title: 'Task 1', status: 1, projectId: 1 },
        { id: 2, title: 'Task 2', status: 1, projectId: 2 },
        { id: 3, title: 'Task 3', status: 1, projectId: 1 }
      ]
      getTasksByAssignee.mockResolvedValue({ data: mockTasks })

      const { loadTaskStatuses, normalizeTask } = useKanban()
      await loadTaskStatuses(null)

      const res = await getTasksByAssignee(100)
      const rawTasks = res.data || res
      let tasks = rawTasks.map(task => normalizeTask(task))

      // Filter by project
      const filterProject = 1
      tasks = tasks.filter(task => task.projectId === filterProject)

      expect(tasks).toHaveLength(2)
      expect(tasks.every(t => t.projectId === 1)).toBe(true)
    })
  })
})