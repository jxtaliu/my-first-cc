import { defineStore } from 'pinia'
import request from '@/api/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null
  }),

  actions: {
    async login(username, password) {
      const res = await request.post('/auth/login', { username, password })
      this.token = res.data.token
      this.user = res.data
      localStorage.setItem('token', this.token)
      return res.data
    },

    async register(data) {
      const res = await request.post('/auth/register', data)
      return res.data
    },

    async getCurrentUser() {
      const res = await request.get('/users/me')
      this.user = res.data
      return res.data
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})
