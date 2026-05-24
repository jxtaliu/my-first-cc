import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockRequest = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}

vi.mock('../request.js', () => ({
  default: mockRequest
}))

describe('TaskStatus API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  let taskStatusApi

  beforeEach(async () => {
    const module = await import('../taskStatus.js')
    taskStatusApi = module
  })

  describe('getTaskStatusesByProject', () => {
    it('should call GET /v1/task-statuses/project/:projectId', async () => {
      const mockStatuses = [
        { id: 1, code: 'TODO', name: 'To Do', color: '#909399' }
      ]
      mockRequest.get.mockResolvedValue({ data: mockStatuses })

      const result = await taskStatusApi.getTaskStatusesByProject('PRJ_001')

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/task-statuses/project/PRJ_001')
      expect(result.data).toEqual(mockStatuses)
    })
  })

  describe('getSystemTaskStatuses', () => {
    it('should call GET /v1/task-statuses/system', async () => {
      const mockStatuses = [
        { id: 1, code: 'TODO', name: 'To Do', color: '#909399' }
      ]
      mockRequest.get.mockResolvedValue({ data: mockStatuses })

      const result = await taskStatusApi.getSystemTaskStatuses()

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/task-statuses/system')
      expect(result.data).toEqual(mockStatuses)
    })
  })

  describe('createTaskStatus', () => {
    it('should call POST /v1/task-statuses with data', async () => {
      const statusData = { code: 'TODO', name: 'To Do', color: '#909399' }
      const mockCreated = { id: 1, ...statusData }
      mockRequest.post.mockResolvedValue({ data: mockCreated })

      const result = await taskStatusApi.createTaskStatus(statusData)

      expect(mockRequest.post).toHaveBeenCalledWith('/v1/task-statuses', statusData)
      expect(result.data).toEqual(mockCreated)
    })
  })

  describe('updateTaskStatus', () => {
    it('should call PUT /v1/task-statuses/:id with data', async () => {
      const updateData = { name: 'Updated Name', color: '#FF0000' }
      mockRequest.put.mockResolvedValue({ data: { id: 1, ...updateData } })

      const result = await taskStatusApi.updateTaskStatus(1, updateData)

      expect(mockRequest.put).toHaveBeenCalledWith('/v1/task-statuses/1', updateData)
    })
  })

  describe('deleteTaskStatus', () => {
    it('should call DELETE /v1/task-statuses/:id', async () => {
      mockRequest.delete.mockResolvedValue({ data: null })

      await taskStatusApi.deleteTaskStatus(1)

      expect(mockRequest.delete).toHaveBeenCalledWith('/v1/task-statuses/1')
    })
  })

  describe('reorderTaskStatuses', () => {
    it('should call PUT /v1/task-statuses/reorder with statusIds array', async () => {
      mockRequest.put.mockResolvedValue({ data: null })
      const statusIds = [3, 1, 2]

      await taskStatusApi.reorderTaskStatuses(statusIds)

      expect(mockRequest.put).toHaveBeenCalledWith('/v1/task-statuses/reorder', statusIds)
    })
  })
})
