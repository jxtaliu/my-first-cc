import { defineStore } from 'pinia'
import { getTaskStatusesByProject, createTaskStatus, updateTaskStatus, deleteTaskStatus, reorderTaskStatuses } from '@/api/taskStatus'
import { getProject } from '@/api/project'

export const useTaskStatusStore = defineStore('taskStatus', {
  state: () => ({
    // Cache statuses by projectId
    statusesByProject: {},
    // Current project business ID cache
    projectIdCache: {},
    loading: false
  }),

  getters: {
    // Get statuses for a specific project
    getStatusesByProjectId: (state) => (projectId) => {
      return state.statusesByProject[projectId] || []
    },

    // Get status mapping (id -> code) for a project
    getStatusIdToCode: (state) => (projectId) => {
      const statuses = state.statusesByProject[projectId] || []
      const map = {}
      statuses.forEach(s => { map[s.id] = s.code?.toLowerCase() })
      return map
    },

    // Get code to id mapping for a project
    getStatusCodeToId: (state) => (projectId) => {
      const statuses = state.statusesByProject[projectId] || []
      const map = {}
      statuses.forEach(s => { map[s.code?.toLowerCase()] = s.id })
      return map
    }
  },

  actions: {
    // Fetch project business ID from numeric ID
    async getProjectBusinessId(numericId) {
      if (!numericId) return null

      // Check cache first
      if (this.projectIdCache[numericId]) {
        return this.projectIdCache[numericId]
      }

      try {
        const res = await getProject(numericId)
        const businessId = res.data?.projectId || numericId
        this.projectIdCache[numericId] = businessId
        return businessId
      } catch (e) {
        return numericId
      }
    },

    // Fetch statuses for a project
    async fetchStatuses(projectId) {
      if (!projectId) return []

      this.loading = true
      try {
        const res = await getTaskStatusesByProject(projectId)
        // Handle both wrapped (res.data) and unwrapped (res is array) responses
        const statuses = Array.isArray(res) ? res : (res?.data || [])
        this.statusesByProject[projectId] = statuses
        return statuses
      } catch (e) {
        console.error('Failed to fetch task statuses:', e)
        return []
      } finally {
        this.loading = false
      }
    },

    // Fetch statuses by numeric project ID (auto-resolve to business ID)
    async fetchStatusesByNumericId(numericId) {
      const businessId = await this.getProjectBusinessId(numericId)
      return this.fetchStatuses(businessId)
    },

    // Create new status
    async createStatus(projectId, data) {
      const res = await createTaskStatus({ ...data, projectId })
      // Refresh the list
      await this.fetchStatuses(projectId)
      return res
    },

    // Update existing status
    async updateStatus(id, data) {
      const res = await updateTaskStatus(id, data)
      return res
    },

    // Delete status
    async deleteStatus(id) {
      const res = await deleteTaskStatus(id)
      return res
    },

    // Reorder statuses
    async reorderStatuses(projectId, statusIds) {
      await reorderTaskStatuses(statusIds)
      // Refresh the list to get updated sort orders
      await this.fetchStatuses(projectId)
    },

    // Update single status in cache (optimistic update)
    updateStatusInCache(projectId, statusId, data) {
      const statuses = this.statusesByProject[projectId]
      if (!statuses) return

      const index = statuses.findIndex(s => s.id === statusId)
      if (index !== -1) {
        this.statusesByProject[projectId][index] = {
          ...statuses[index],
          ...data
        }
      }
    },

    // Remove status from cache
    removeStatusFromCache(projectId, statusId) {
      const statuses = this.statusesByProject[projectId]
      if (!statuses) return

      this.statusesByProject[projectId] = statuses.filter(s => s.id !== statusId)
    },

    // Clear cache for a project
    clearCache(projectId) {
      if (projectId) {
        delete this.statusesByProject[projectId]
      }
    }
  }
})
