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

describe('Timesheet API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  let timesheetApi

  beforeEach(async () => {
    const module = await import('../timesheet.js')
    timesheetApi = module
  })

  describe('getTimesheets', () => {
    it('should call GET /timesheets with params', async () => {
      const mockTimesheets = [{ id: 1, hours: 8 }]
      mockRequest.get.mockResolvedValue({ data: mockTimesheets })

      const params = { startDate: '2024-01-01', endDate: '2024-01-07' }
      const result = await timesheetApi.getTimesheets(params)

      expect(mockRequest.get).toHaveBeenCalledWith('/timesheets', { params })
      expect(result.data).toEqual(mockTimesheets)
    })
  })

  describe('getTimesheet', () => {
    it('should call GET /timesheets/:id', async () => {
      const mockTimesheet = { id: 1, hours: 8, date: '2024-01-01' }
      mockRequest.get.mockResolvedValue({ data: mockTimesheet })

      const result = await timesheetApi.getTimesheet(1)

      expect(mockRequest.get).toHaveBeenCalledWith('/timesheets/1')
      expect(result.data).toEqual(mockTimesheet)
    })
  })

  describe('createTimesheet', () => {
    it('should call POST /timesheets with data', async () => {
      const timesheetData = { projectId: 1, taskId: 1, hours: 8, date: '2024-01-01' }
      const mockCreated = { id: 2, ...timesheetData }
      mockRequest.post.mockResolvedValue({ data: mockCreated })

      const result = await timesheetApi.createTimesheet(timesheetData)

      expect(mockRequest.post).toHaveBeenCalledWith('/timesheets', timesheetData)
      expect(result.data).toEqual(mockCreated)
    })
  })

  describe('updateTimesheet', () => {
    it('should call PUT /timesheets/:id with data', async () => {
      const updateData = { hours: 10 }
      mockRequest.put.mockResolvedValue({ data: { id: 1, ...updateData } })

      const result = await timesheetApi.updateTimesheet(1, updateData)

      expect(mockRequest.put).toHaveBeenCalledWith('/timesheets/1', updateData)
    })
  })

  describe('deleteTimesheet', () => {
    it('should call DELETE /timesheets/:id', async () => {
      mockRequest.delete.mockResolvedValue({ data: null })

      await timesheetApi.deleteTimesheet(1)

      expect(mockRequest.delete).toHaveBeenCalledWith('/timesheets/1')
    })
  })

  describe('approveTimesheet', () => {
    it('should call POST /timesheets/:id/approve', async () => {
      mockRequest.post.mockResolvedValue({ data: { id: 1, status: 'APPROVED' } })

      await timesheetApi.approveTimesheet(1)

      expect(mockRequest.post).toHaveBeenCalledWith('/timesheets/1/approve')
    })
  })

  describe('rejectTimesheet', () => {
    it('should call POST /timesheets/:id/reject', async () => {
      mockRequest.post.mockResolvedValue({ data: { id: 1, status: 'REJECTED' } })

      await timesheetApi.rejectTimesheet(1)

      expect(mockRequest.post).toHaveBeenCalledWith('/timesheets/1/reject')
    })
  })

  describe('getMyTimesheets', () => {
    it('should call GET /timesheets/my with params', async () => {
      const mockTimesheets = [{ id: 1, hours: 8 }]
      mockRequest.get.mockResolvedValue({ data: mockTimesheets })

      const params = { month: 1, year: 2024 }
      const result = await timesheetApi.getMyTimesheets(params)

      expect(mockRequest.get).toHaveBeenCalledWith('/timesheets/my', { params })
      expect(result.data).toEqual(mockTimesheets)
    })
  })

  describe('getTimesheetStats', () => {
    it('should call GET /timesheets/stats with params', async () => {
      const mockStats = { totalHours: 160, approvedHours: 140, pendingHours: 20 }
      mockRequest.get.mockResolvedValue({ data: mockStats })

      const params = { projectId: 1 }
      const result = await timesheetApi.getTimesheetStats(params)

      expect(mockRequest.get).toHaveBeenCalledWith('/timesheets/stats', { params })
      expect(result.data).toEqual(mockStats)
    })
  })
})
