import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useKanban } from '../useKanban'

// Mock the API modules
vi.mock('@/api/taskStatus', () => ({
  getTaskStatusesByProject: vi.fn(),
  getSystemTaskStatuses: vi.fn()
}))

import { getTaskStatusesByProject, getSystemTaskStatuses } from '@/api/taskStatus'

describe('useKanban', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  describe('loadTaskStatuses', () => {
    const mockSystemStatuses = [
      { id: 1, code: 'todo', name: 'To Do', color: '#94A3B8' },
      { id: 2, code: 'in_progress', name: 'In Progress', color: '#3B82F6' },
      { id: 3, code: 'done', name: 'Done', color: '#10B981' }
    ]

    const mockProjectStatuses = [
      { id: 10, code: 'BACKLOG', name: 'Backlog', color: '#6366F1' },
      { id: 11, code: 'IN_DEV', name: 'In Development', color: '#8B5CF6' },
      { id: 12, code: 'IN_TEST', name: 'In Testing', color: '#F59E0B' },
      { id: 13, code: 'RELEASED', name: 'Released', color: '#10B981' }
    ]

    it('testLoadTaskStatuses_shouldMapToColumns', async () => {
      getSystemTaskStatuses.mockResolvedValue({ data: mockSystemStatuses })

      const { columns, taskStatuses, loading, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(null)

      expect(columns.value).toHaveLength(3)
      expect(columns.value[0]).toEqual({
        id: 'todo',
        status: 'todo',
        title: 'To Do',
        color: '#94A3B8',
        statusId: 1
      })
      expect(columns.value[1]).toEqual({
        id: 'in_progress',
        status: 'in_progress',
        title: 'In Progress',
        color: '#3B82F6',
        statusId: 2
      })
      expect(columns.value[2]).toEqual({
        id: 'done',
        status: 'done',
        title: 'Done',
        color: '#10B981',
        statusId: 3
      })
      expect(taskStatuses.value).toEqual(mockSystemStatuses)
      expect(loading.value).toBe(false)
    })

    it('testLoadTaskStatuses_shouldHandleProjectSpecificStatuses', async () => {
      getTaskStatusesByProject.mockResolvedValue({ data: mockProjectStatuses })

      const { columns, taskStatuses, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(123)

      expect(getTaskStatusesByProject).toHaveBeenCalledWith(123)
      expect(columns.value).toHaveLength(4)
      expect(columns.value[0].id).toBe('BACKLOG')
      expect(columns.value[0].statusId).toBe(10)
      expect(taskStatuses.value).toEqual(mockProjectStatuses)
    })

    it('testLoadTaskStatuses_shouldFallbackToSystemStatuses_whenProjectHasNoCustomStatuses', async () => {
      // Project has no custom statuses, should fallback to system statuses
      getTaskStatusesByProject.mockResolvedValue({ data: [] })
      getSystemTaskStatuses.mockResolvedValue({ data: mockSystemStatuses })

      const { columns, taskStatuses, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(999)

      expect(getTaskStatusesByProject).toHaveBeenCalledWith(999)
      expect(getSystemTaskStatuses).toHaveBeenCalled()
      expect(columns.value).toHaveLength(3)
      expect(columns.value[0].id).toBe('todo')
      expect(taskStatuses.value).toEqual(mockSystemStatuses)
    })

    it('testLoadTaskStatuses_shouldFallbackToSystemStatuses_whenProjectReturnsNull', async () => {
      // Project returns null data, should fallback to system statuses
      getTaskStatusesByProject.mockResolvedValue({ data: null })
      getSystemTaskStatuses.mockResolvedValue({ data: mockSystemStatuses })

      const { columns, taskStatuses, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(123)

      expect(getSystemTaskStatuses).toHaveBeenCalled()
      expect(columns.value).toHaveLength(3)
      expect(taskStatuses.value).toEqual(mockSystemStatuses)
    })

    it('testLoadTaskStatuses_shouldSetLoadingState', async () => {
      let resolvePromise
      const promise = new Promise(resolve => { resolvePromise = resolve })
      getSystemTaskStatuses.mockReturnValue(promise)

      const { loading, loadTaskStatuses } = useKanban()

      const loadPromise = loadTaskStatuses(null)
      expect(loading.value).toBe(true)

      resolvePromise({ data: mockSystemStatuses })
      await loadPromise
      expect(loading.value).toBe(false)
    })
  })

  describe('normalizeTask', () => {
    it('testNormalizeTask_shouldUseStatusMapping', async () => {
      const mockStatuses = [
        { id: 1, code: 'todo', name: 'To Do', color: '#94A3B8' },
        { id: 2, code: 'in_progress', name: 'In Progress', color: '#3B82F6' },
        { id: 3, code: 'done', name: 'Done', color: '#10B981' }
      ]
      getSystemTaskStatuses.mockResolvedValue({ data: mockStatuses })

      const { normalizeTask, loadTaskStatuses, statusIdToCode } = useKanban()

      await loadTaskStatuses(null)

      const apiTask = {
        id: 1,
        title: 'Test Task',
        status: 2  // status ID
      }

      const normalized = normalizeTask(apiTask)

      expect(statusIdToCode.value).toEqual({ 1: 'todo', 2: 'in_progress', 3: 'done' })
      expect(normalized.status).toBe('in_progress')
      expect(normalized.priority).toBe('P3') // default priority
    })
  })

  describe('status mappings', () => {
    it('testStatusMappings_shouldBeInverseOfEachOther', async () => {
      const mockStatuses = [
        { id: 10, code: 'BACKLOG', name: 'Backlog', color: '#6366F1' },
        { id: 11, code: 'IN_DEV', name: 'In Development', color: '#8B5CF6' },
        { id: 12, code: 'IN_TEST', name: 'In Testing', color: '#F59E0B' },
        { id: 13, code: 'RELEASED', name: 'Released', color: '#10B981' }
      ]
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { statusIdToCode, statusCodeToId, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(123)

      // Verify statusIdToCode
      expect(statusIdToCode.value).toEqual({
        10: 'BACKLOG',
        11: 'IN_DEV',
        12: 'IN_TEST',
        13: 'RELEASED'
      })

      // Verify statusCodeToId is the inverse
      expect(statusCodeToId.value).toEqual({
        'BACKLOG': 10,
        'IN_DEV': 11,
        'IN_TEST': 12,
        'RELEASED': 13
      })

      // Verify round-trip
      Object.entries(statusIdToCode.value).forEach(([id, code]) => {
        expect(statusCodeToId.value[code]).toBe(parseInt(id))
      })
    })
  })
})