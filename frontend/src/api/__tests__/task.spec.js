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

describe('Task API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  let taskApi

  beforeEach(async () => {
    const module = await import('../task.js')
    taskApi = module
  })

  describe('getTasks', () => {
    it('should call GET /tasks with params', async () => {
      const mockTasks = [{ id: 1, title: 'Task 1' }]
      mockRequest.get.mockResolvedValue({ data: mockTasks })

      const params = { projectId: 1, status: 'open' }
      const result = await taskApi.getTasks(params)

      expect(mockRequest.get).toHaveBeenCalledWith('/tasks', { params })
      expect(result.data).toEqual(mockTasks)
    })
  })

  describe('getTask', () => {
    it('should call GET /tasks/:id', async () => {
      const mockTask = { id: 1, title: 'Task 1' }
      mockRequest.get.mockResolvedValue({ data: mockTask })

      const result = await taskApi.getTask(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/tasks/1')
      expect(result.data).toEqual(mockTask)
    })
  })

  describe('createTask', () => {
    it('should call POST /tasks with data', async () => {
      const taskData = { title: 'New Task', projectId: 1 }
      const mockCreated = { id: 2, ...taskData }
      mockRequest.post.mockResolvedValue({ data: mockCreated })

      const result = await taskApi.createTask(taskData)

      expect(mockRequest.post).toHaveBeenCalledWith('/tasks', taskData)
      expect(result.data).toEqual(mockCreated)
    })
  })

  describe('updateTask', () => {
    it('should call PUT /tasks/:id with data', async () => {
      const updateData = { title: 'Updated Task', status: 'in_progress' }
      mockRequest.put.mockResolvedValue({ data: { id: 1, ...updateData } })

      const result = await taskApi.updateTask(1, updateData)

      expect(mockRequest.put).toHaveBeenCalledWith('/tasks/1', updateData)
    })
  })

  describe('deleteTask', () => {
    it('should call DELETE /tasks/:id', async () => {
      mockRequest.delete.mockResolvedValue({ data: null })

      await taskApi.deleteTask(1)

      expect(mockRequest.delete).toHaveBeenCalledWith('/tasks/1')
    })
  })

  describe('getTasksBySprint', () => {
    it('should call GET /tasks with sprintId param', async () => {
      const mockTasks = [{ id: 1, title: 'Task 1' }]
      mockRequest.get.mockResolvedValue({ data: mockTasks })

      const result = await taskApi.getTasksBySprint(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/tasks', { params: { sprintId: 1 } })
      expect(result.data).toEqual(mockTasks)
    })
  })

  describe('getTasksByProject', () => {
    it('should call GET /tasks with projectId param', async () => {
      const mockTasks = [{ id: 1, title: 'Task 1' }]
      mockRequest.get.mockResolvedValue({ data: mockTasks })

      const result = await taskApi.getTasksByProject(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/tasks', { params: { projectId: 1 } })
      expect(result.data).toEqual(mockTasks)
    })
  })

  describe('moveTask', () => {
    it('should call PUT /tasks/:id/move with position data', async () => {
      const moveData = { targetStatus: 'done', position: 1 }
      mockRequest.put.mockResolvedValue({ data: { id: 1, ...moveData } })

      const result = await taskApi.moveTask(1, moveData)

      expect(mockRequest.put).toHaveBeenCalledWith('/tasks/1/move', moveData)
    })
  })

  describe('assignTask', () => {
    it('should call PUT /tasks/:id/assign with userId', async () => {
      mockRequest.put.mockResolvedValue({ data: { id: 1, assigneeId: 2 } })

      const result = await taskApi.assignTask(1, 2)

      expect(mockRequest.put).toHaveBeenCalledWith('/tasks/1/assign', { userId: 2 })
    })
  })

  describe('getTaskComments', () => {
    it('should call GET /tasks/:id/comments', async () => {
      const mockComments = [{ id: 1, text: 'Comment 1' }]
      mockRequest.get.mockResolvedValue({ data: mockComments })

      const result = await taskApi.getTaskComments(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/tasks/1/comments')
      expect(result.data).toEqual(mockComments)
    })
  })

  describe('addTaskComment', () => {
    it('should call POST /tasks/:id/comments with data', async () => {
      const commentData = { text: 'New comment' }
      mockRequest.post.mockResolvedValue({ data: { id: 2, ...commentData } })

      const result = await taskApi.addTaskComment(1, commentData)

      expect(mockRequest.post).toHaveBeenCalledWith('/tasks/1/comments', commentData)
    })
  })

  describe('getTaskCountByStatus', () => {
    it('should call GET /v1/tasks/count with statusId param', async () => {
      mockRequest.get.mockResolvedValue({ data: 5 })

      const result = await taskApi.getTaskCountByStatus(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/tasks/count', { params: { statusId: 1 } })
      expect(result.data).toBe(5)
    })
  })
})
