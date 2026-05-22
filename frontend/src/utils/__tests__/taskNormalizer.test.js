import { describe, it, expect } from 'vitest'
import { normalizeTask, denormalizeTask } from '../taskNormalizer'

describe('taskNormalizer', () => {
  describe('normalizeTask', () => {
    it('testNormalizeTask_shouldMapApiFieldsCorrectly', () => {
      const apiTask = {
        id: 1,
        title: 'Test Task',
        description: 'Test Description',
        type: 'task',
        priority: 'P1',
        statusId: 10,
        status: 'IN_PROGRESS',
        assigneeId: 100,
        assigneeName: 'John Doe',
        estimateHours: 8,
        remainingHours: 5,
        actualHours: 3,
        progress: 37,
        dueDate: '2024-12-31',
        commentCount: 5,
        attachmentCount: 2
      }
      const statusIdToCode = { 10: 'in_progress' }

      const result = normalizeTask(apiTask, statusIdToCode)

      expect(result.id).toBe(1)
      expect(result.title).toBe('Test Task')
      expect(result.description).toBe('Test Description')
      expect(result.type).toBe('task')
      expect(result.priority).toBe('P1')
      expect(result.status).toBe('in_progress')
      expect(result.statusId).toBe(10)
      expect(result.assigneeId).toBe(100)
      expect(result.assigneeName).toBe('John Doe')
      expect(result.estimateHours).toBe(8)
      expect(result.remainingHours).toBe(5)
      expect(result.actualHours).toBe(3)
      expect(result.progress).toBe(37)
      expect(result.dueDate).toBe('2024-12-31')
      expect(result.commentCount).toBe(5)
      expect(result.attachmentCount).toBe(2)
      expect(result._raw).toBe(apiTask)
    })

    it('testNormalizeTask_shouldUseDefaultPriority_whenNotProvided', () => {
      const apiTask = {
        id: 1,
        title: 'Test Task',
        statusId: 10
      }

      const result = normalizeTask(apiTask)

      expect(result.priority).toBe('P3')
    })

    it('testNormalizeTask_shouldHandleNullTask', () => {
      const result = normalizeTask(null)
      expect(result).toBeNull()
    })

    it('testNormalizeTask_shouldHandleStatusIdToCodeMapping', () => {
      const apiTask = {
        id: 1,
        title: 'Test Task',
        statusId: 20,
        status: 20
      }
      const statusIdToCode = {
        10: 'todo',
        20: 'in_progress',
        30: 'done'
      }

      const result = normalizeTask(apiTask, statusIdToCode)

      expect(result.status).toBe('in_progress')
      expect(result.statusId).toBe(20)
    })
  })

  describe('denormalizeTask', () => {
    it('testDenormalizeTask_shouldMapToApiFormat', () => {
      const task = {
        title: 'Test Task',
        description: 'Test Description',
        type: 'task',
        priority: 'P1',
        status: 'in_progress',
        statusId: 20,
        assigneeId: 100,
        estimateHours: 8,
        remainingHours: 5,
        actualHours: 3,
        progress: 37
      }
      const statusCodeToId = {
        'todo': 10,
        'in_progress': 20,
        'done': 30
      }

      const result = denormalizeTask(task, statusCodeToId)

      expect(result.title).toBe('Test Task')
      expect(result.description).toBe('Test Description')
      expect(result.type).toBe('task')
      expect(result.priority).toBe('P1')
      expect(result.statusId).toBe(20)
      expect(result.assigneeId).toBe(100)
      expect(result.estimateHours).toBe(8)
      expect(result.remainingHours).toBe(5)
      expect(result.actualHours).toBe(3)
      expect(result.progress).toBe(37)
    })

    it('testDenormalizeTask_shouldUseTaskStatusId_whenCodeNotMapped', () => {
      const task = {
        title: 'Test Task',
        status: 'unknown_status',
        statusId: 50
      }
      const statusCodeToId = {}

      const result = denormalizeTask(task, statusCodeToId)

      expect(result.statusId).toBe(50)
    })
  })

  describe('round-trip conversion', () => {
    it('testStatusMappings_shouldBeInverseOfEachOther', () => {
      const statusIdToCode = {
        10: 'todo',
        20: 'in_progress',
        30: 'done'
      }
      const statusCodeToId = {
        'todo': 10,
        'in_progress': 20,
        'done': 30
      }

      const apiTask = {
        id: 1,
        title: 'Test',
        statusId: 20
      }

      const normalized = normalizeTask(apiTask, statusIdToCode)
      expect(normalized.status).toBe('in_progress')

      const denormalized = denormalizeTask(normalized, statusCodeToId)
      expect(denormalized.statusId).toBe(20)
    })
  })
})