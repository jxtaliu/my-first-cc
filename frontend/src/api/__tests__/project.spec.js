import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock request module
const mockRequest = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}

vi.mock('../request.js', () => ({
  default: mockRequest
}))

describe('Project API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // Re-import to get fresh mocks
  let projectApi

  beforeEach(async () => {
    const module = await import('../project.js')
    projectApi = module
  })

  describe('getProjects', () => {
    it('should call GET /projects with params', async () => {
      const mockProjects = [{ id: 1, name: 'Project 1' }]
      mockRequest.get.mockResolvedValue({ data: mockProjects })

      const params = { status: 'active' }
      const result = await projectApi.getProjects(params)

      expect(mockRequest.get).toHaveBeenCalledWith('/projects', { params })
      expect(result.data).toEqual(mockProjects)
    })

    it('should call GET /projects without params', async () => {
      mockRequest.get.mockResolvedValue({ data: [] })

      await projectApi.getProjects()

      expect(mockRequest.get).toHaveBeenCalledWith('/projects', { params: undefined })
    })
  })

  describe('getProject', () => {
    it('should call GET /projects/:id', async () => {
      const mockProject = { id: 1, name: 'Project 1' }
      mockRequest.get.mockResolvedValue({ data: mockProject })

      const result = await projectApi.getProject(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/projects/1')
      expect(result.data).toEqual(mockProject)
    })
  })

  describe('createProject', () => {
    it('should call POST /projects with data', async () => {
      const projectData = { name: 'New Project', type: 'SCRUM' }
      const mockCreated = { id: 2, ...projectData }
      mockRequest.post.mockResolvedValue({ data: mockCreated })

      const result = await projectApi.createProject(projectData)

      expect(mockRequest.post).toHaveBeenCalledWith('/projects', projectData)
      expect(result.data).toEqual(mockCreated)
    })
  })

  describe('updateProject', () => {
    it('should call PUT /projects/:id with data', async () => {
      const updateData = { name: 'Updated Project' }
      mockRequest.put.mockResolvedValue({ data: { id: 1, ...updateData } })

      const result = await projectApi.updateProject(1, updateData)

      expect(mockRequest.put).toHaveBeenCalledWith('/projects/1', updateData)
    })
  })

  describe('deleteProject', () => {
    it('should call DELETE /projects/:id', async () => {
      mockRequest.delete.mockResolvedValue({ data: null })

      await projectApi.deleteProject(1)

      expect(mockRequest.delete).toHaveBeenCalledWith('/projects/1')
    })
  })

  describe('archiveProject', () => {
    it('should call POST /projects/:id/archive', async () => {
      mockRequest.post.mockResolvedValue({ data: { id: 1, archived: true } })

      await projectApi.archiveProject(1)

      expect(mockRequest.post).toHaveBeenCalledWith('/projects/1/archive')
    })
  })

  describe('restoreProject', () => {
    it('should call POST /projects/:id/restore', async () => {
      mockRequest.post.mockResolvedValue({ data: { id: 1, archived: false } })

      await projectApi.restoreProject(1)

      expect(mockRequest.post).toHaveBeenCalledWith('/projects/1/restore')
    })
  })

  describe('getProjectMembers', () => {
    it('should call GET /v1/projects/:id/members', async () => {
      const mockMembers = [{ id: 1, name: 'Member 1' }]
      mockRequest.get.mockResolvedValue({ data: mockMembers })

      const result = await projectApi.getProjectMembers(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/projects/1/members')
      expect(result.data).toEqual(mockMembers)
    })
  })

  describe('addProjectMember', () => {
    it('should call POST /v1/projects/:id/members with data', async () => {
      const memberData = { userId: 2, role: 'DEVELOPER' }
      mockRequest.post.mockResolvedValue({ data: memberData })

      await projectApi.addProjectMember(1, memberData)

      expect(mockRequest.post).toHaveBeenCalledWith('/v1/projects/1/members', memberData)
    })
  })

  describe('removeProjectMember', () => {
    it('should call DELETE /v1/projects/:id/members/:userId', async () => {
      mockRequest.delete.mockResolvedValue({ data: null })

      await projectApi.removeProjectMember(1, 2)

      expect(mockRequest.delete).toHaveBeenCalledWith('/v1/projects/1/members/2')
    })
  })

  describe('getSprints', () => {
    it('should call GET /v1/projects/:id/sprints', async () => {
      const mockSprints = [{ id: 1, name: 'Sprint 1' }]
      mockRequest.get.mockResolvedValue({ data: mockSprints })

      const result = await projectApi.getSprints(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/projects/1/sprints')
      expect(result.data).toEqual(mockSprints)
    })
  })

  describe('createSprint', () => {
    it('should call POST /v1/projects/:id/sprints with data', async () => {
      const sprintData = { name: 'Sprint 1', startDate: '2024-01-01' }
      mockRequest.post.mockResolvedValue({ data: { id: 1, ...sprintData } })

      await projectApi.createSprint(1, sprintData)

      expect(mockRequest.post).toHaveBeenCalledWith('/v1/projects/1/sprints', sprintData)
    })
  })

  describe('getProjectStats', () => {
    it('should call GET /v1/projects/:id/stats', async () => {
      const mockStats = { totalTasks: 10, completedTasks: 5 }
      mockRequest.get.mockResolvedValue({ data: mockStats })

      const result = await projectApi.getProjectStats(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/projects/1/stats')
      expect(result.data).toEqual(mockStats)
    })
  })
})
