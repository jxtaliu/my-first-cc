import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { h } from 'vue'

// Mock Element Plus components
vi.mock('element-plus', () => ({
  ElInput: {
    name: 'ElInput',
    render() { return h('input') }
  },
  ElSelect: {
    name: 'ElSelect',
    render() { return h('select') }
  },
  ElOption: {
    name: 'ElOption',
    render() { return h('option') }
  },
  ElButton: {
    name: 'ElButton',
    render() { return h('button') }
  },
  ElIcon: {
    name: 'ElIcon',
    render() { return h('i') }
  },
  ElLoading: {
    directive: vi.fn(),
    service: vi.fn()
  },
  ElMessage: {
    error: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

// Mock vue-i18n
vi.mock('vue-i18n', () => ({
  useI18n: vi.fn(() => ({
    t: (k) => k,
    locale: { value: 'en-US' }
  }))
}))

// Mock vue-router
const mockRoute = {
  params: { projectId: '1' }
}
const mockRouter = {
  push: vi.fn()
}

vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => mockRoute),
  useRouter: vi.fn(() => mockRouter)
}))

// Mock API
vi.mock('@/api/project', () => ({
  getSprints: vi.fn().mockResolvedValue({ data: [] }),
  batchAssignTasks: vi.fn().mockResolvedValue({ data: { movedCount: 0 } }),
  batchRemoveTasks: vi.fn().mockResolvedValue({ data: { movedCount: 0 } })
}))

vi.mock('@/api/task', () => ({
  getTasksByProject: vi.fn().mockResolvedValue({ data: [] }),
  updateTask: vi.fn().mockResolvedValue({ data: {} })
}))

// Import SprintManagement after mocks
import SprintManagement from '../SprintManagement.vue'

describe('SprintManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should render page header', () => {
    const wrapper = mount(SprintManagement, {
      global: {
        mocks: {
          $t: (k) => k
        },
        plugins: [createPinia()],
        stubs: {
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-icon': true
        }
      }
    })
    expect(wrapper.find('.sprint-management-page').exists()).toBe(true)
  })

  it('should render backlog lane', () => {
    const wrapper = mount(SprintManagement, {
      global: {
        mocks: {
          $t: (k) => k
        },
        plugins: [createPinia()],
        stubs: {
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-icon': true
        }
      }
    })
    expect(wrapper.findAll('.sprint-lane').length).toBeGreaterThan(0)
  })
})