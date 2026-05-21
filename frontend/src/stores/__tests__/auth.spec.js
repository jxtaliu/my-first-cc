import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

// Mock request module
const mockPost = vi.fn()
const mockGet = vi.fn()

vi.mock('@/api/request.js', () => ({
  default: {
    post: mockPost,
    get: mockGet
  }
}))

vi.mock('@/router', () => ({
  default: {
    push: vi.fn()
  }
}))

describe('useAuthStore', () => {
  let store

  beforeEach(async () => {
    localStorage.clear()
    vi.clearAllMocks()
    setActivePinia(createPinia())

    // Reset module state by creating a fresh import
    vi.resetModules()

    const { useAuthStore } = await import('../auth.js')
    store = useAuthStore()
  })

  it('should have empty token and user initially', () => {
    expect(store.token).toBe('')
    expect(store.user).toBeNull()
  })

  it('should login successfully with valid credentials', async () => {
    const mockUser = { id: 1, username: 'admin', token: 'jwt-token' }
    mockPost.mockResolvedValue({ data: mockUser })

    const result = await store.login('admin', 'password123')

    expect(mockPost).toHaveBeenCalledWith('/auth/login', {
      username: 'admin',
      password: 'password123'
    })
    expect(store.token).toBe('jwt-token')
    expect(store.user).toEqual(mockUser)
    expect(localStorage.getItem('token')).toBe('jwt-token')
  })

  it('should store user data on login', async () => {
    const mockResponse = {
      data: {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        token: 'token123'
      }
    }
    mockPost.mockResolvedValue(mockResponse)

    await store.login('testuser', 'password')

    expect(store.user).toBeDefined()
    expect(store.user.username).toBe('testuser')
    expect(store.user.email).toBe('test@example.com')
  })

  it('should logout and clear state', async () => {
    // First login
    const mockUser = { id: 1, username: 'admin', token: 'token' }
    mockPost.mockResolvedValue({ data: mockUser })
    await store.login('admin', 'password')

    // Then logout
    store.logout()

    expect(store.token).toBe('')
    expect(store.user).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('should get current user', async () => {
    const mockUser = { id: 1, username: 'admin', email: 'admin@example.com' }
    mockGet.mockResolvedValue({ data: mockUser })

    const result = await store.getCurrentUser()

    expect(mockGet).toHaveBeenCalledWith('/users/me')
    expect(store.user).toEqual(mockUser)
  })

  it('should register new user', async () => {
    const userData = { username: 'newuser', password: 'password', email: 'new@example.com' }
    mockPost.mockResolvedValue({ data: { id: 2, ...userData } })

    const result = await store.register(userData)

    expect(mockPost).toHaveBeenCalledWith('/auth/register', userData)
  })

  it('should return user data from login', async () => {
    const mockUser = { id: 1, username: 'admin', token: 'token' }
    mockPost.mockResolvedValue({ data: mockUser })

    const result = await store.login('admin', 'admin123')

    expect(result).toEqual(mockUser)
  })
})
