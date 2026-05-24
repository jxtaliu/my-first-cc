import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useKanban } from '../useKanban'

// Mock data storage
let mockStoreData = {}

// Mock the API modules
vi.mock('@/api/taskStatus', () => ({
  getTaskStatusesByProject: vi.fn(),
  getSystemTaskStatuses: vi.fn()
}))

// Mock the taskStatus store
vi.mock('@/stores/taskStatus', () => ({
  useTaskStatusStore: vi.fn(() => ({
    statusesByProject: mockStoreData,
    loading: false,
    getStatusesByProjectId: (projectId) => mockStoreData[projectId] || [],
    getStatusIdToCode: (projectId) => {
      const statuses = mockStoreData[projectId] || []
      const map = {}
      statuses.forEach(s => { map[s.id] = s.code?.toLowerCase() })
      return map
    },
    getStatusCodeToId: (projectId) => {
      const statuses = mockStoreData[projectId] || []
      const map = {}
      statuses.forEach(s => { map[s.code?.toLowerCase()] = s.id })
      return map
    },
    getProjectBusinessId: vi.fn((id) => Promise.resolve(id)),
    fetchStatuses: vi.fn(async (projectId) => {
      const { getTaskStatusesByProject } = await import('@/api/taskStatus')
      const res = await getTaskStatusesByProject(projectId)
      mockStoreData[projectId] = res.data || []
      return mockStoreData[projectId]
    })
  }))
}))

import { getTaskStatusesByProject, getSystemTaskStatuses } from '@/api/taskStatus'

describe('useKanban', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Clear the mock store data
    mockStoreData = {}
    setActivePinia(createPinia())
  })

  describe('loadTaskStatuses', () => {
    const mockSystemStatuses = [
      { id: 1, code: 'todo', nameEn: 'To Do', color: '#94A3B8' },
      { id: 2, code: 'in_progress', nameEn: 'In Progress', color: '#3B82F6' },
      { id: 3, code: 'done', nameEn: 'Done', color: '#10B981' }
    ]

    const mockProjectStatuses = [
      { id: 10, code: 'BACKLOG', nameEn: 'Backlog', color: '#6366F1' },
      { id: 11, code: 'IN_DEV', nameEn: 'In Development', color: '#8B5CF6' },
      { id: 12, code: 'IN_TEST', nameEn: 'In Testing', color: '#F59E0B' },
      { id: 13, code: 'RELEASED', nameEn: 'Released', color: '#10B981' }
    ]

    it('testLoadTaskStatuses_shouldMapToColumns', async () => {
      // Pre-populate store with system statuses for null projectId
      mockStoreData['null'] = mockSystemStatuses
      getSystemTaskStatuses.mockResolvedValue({ data: mockSystemStatuses })
      // Pre-populate store with system statuses for null projectId
      mockStoreData['null'] = mockSystemStatuses

      const { columns, taskStatuses, loading, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(null)

      expect(columns.value).toHaveLength(3)
      expect(columns.value[0]).toEqual({
        id: 'todo',
        status: 'todo',
        title: 'To Do',
        titleZh: '',
        color: '#94A3B8',
        statusId: 1
      })
      expect(columns.value[1]).toEqual({
        id: 'in_progress',
        status: 'in_progress',
        title: 'In Progress',
        titleZh: '',
        color: '#3B82F6',
        statusId: 2
      })
      expect(columns.value[2]).toEqual({
        id: 'done',
        status: 'done',
        title: 'Done',
        titleZh: '',
        color: '#10B981',
        statusId: 3
      })
      expect(loading.value).toBe(false)
    })

    it('testLoadTaskStatuses_shouldHandleProjectSpecificStatuses', async () => {
      getTaskStatusesByProject.mockResolvedValue({ data: mockProjectStatuses })

      const { columns, taskStatuses, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(123)

      expect(getTaskStatusesByProject).toHaveBeenCalledWith(123)
      expect(columns.value).toHaveLength(4)
      expect(columns.value[0].id).toBe('backlog')
      expect(columns.value[0].statusId).toBe(10)
    })

    it('testLoadTaskStatuses_shouldFallbackToSystemStatuses_whenProjectHasNoCustomStatuses', async () => {
      // Project has no custom statuses, should fallback to system statuses
      getTaskStatusesByProject.mockResolvedValue({ data: [] })
      getSystemTaskStatuses.mockResolvedValue({ data: mockSystemStatuses })
      mockStoreData['null'] = mockSystemStatuses

      const { columns, taskStatuses, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(999)

      expect(getTaskStatusesByProject).toHaveBeenCalledWith(999)
      expect(getSystemTaskStatuses).toHaveBeenCalled()
      expect(columns.value).toHaveLength(3)
      expect(columns.value[0].id).toBe('todo')
    })

    it('testLoadTaskStatuses_shouldFallbackToSystemStatuses_whenProjectReturnsNull', async () => {
      // Project returns null data, should fallback to system statuses
      getTaskStatusesByProject.mockResolvedValue({ data: null })
      getSystemTaskStatuses.mockResolvedValue({ data: mockSystemStatuses })
      mockStoreData['null'] = mockSystemStatuses

      const { columns, taskStatuses, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(123)

      expect(getSystemTaskStatuses).toHaveBeenCalled()
      expect(columns.value).toHaveLength(3)
    })

    it('testLoadTaskStatuses_shouldSetLoadingState', async () => {
      let resolvePromise
      const promise = new Promise(resolve => { resolvePromise = resolve })
      getSystemTaskStatuses.mockReturnValue(promise)
      mockStoreData['null'] = []

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
        { id: 1, code: 'todo', nameEn: 'To Do', color: '#94A3B8' },
        { id: 2, code: 'in_progress', nameEn: 'In Progress', color: '#3B82F6' },
        { id: 3, code: 'done', nameEn: 'Done', color: '#10B981' }
      ]
      getSystemTaskStatuses.mockResolvedValue({ data: mockStatuses })
      mockStoreData['null'] = mockStatuses

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
        { id: 10, code: 'BACKLOG', nameEn: 'Backlog', color: '#6366F1' },
        { id: 11, code: 'IN_DEV', nameEn: 'In Development', color: '#8B5CF6' },
        { id: 12, code: 'IN_TEST', nameEn: 'In Testing', color: '#F59E0B' },
        { id: 13, code: 'RELEASED', nameEn: 'Released', color: '#10B981' }
      ]
      getTaskStatusesByProject.mockResolvedValue({ data: mockStatuses })

      const { statusIdToCode, statusCodeToId, loadTaskStatuses } = useKanban()

      await loadTaskStatuses(123)

      // Verify statusIdToCode
      expect(statusIdToCode.value).toEqual({
        10: 'backlog',
        11: 'in_dev',
        12: 'in_test',
        13: 'released'
      })

      // Verify statusCodeToId is the inverse
      expect(statusCodeToId.value).toEqual({
        'backlog': 10,
        'in_dev': 11,
        'in_test': 12,
        'released': 13
      })

      // Verify round-trip
      Object.entries(statusIdToCode.value).forEach(([id, code]) => {
        expect(statusCodeToId.value[code]).toBe(parseInt(id))
      })
    })
  })
})
