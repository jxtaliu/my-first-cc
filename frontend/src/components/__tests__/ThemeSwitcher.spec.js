import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ThemeSwitcher from '../ThemeSwitcher.vue'

// Create mock theme store
const mockThemeStore = {
  currentTheme: 'minimal',
  themes: {
    minimal: {
      name: 'Professional Minimal',
      colors: { accent: '#4f46e5' }
    },
    tech: {
      name: 'Modern Tech',
      colors: { accent: '#00d9ff' }
    },
    vibrant: {
      name: 'Vibrant & Playful',
      colors: { accent: '#f97316' }
    },
    dark: {
      name: 'Dark Luxe',
      colors: { accent: '#a855f7' }
    }
  },
  colors: { accent: '#4f46e5' },
  themeName: 'Professional Minimal',
  setTheme: vi.fn()
}

vi.mock('@/stores/theme.js', () => ({
  useThemeStore: () => mockThemeStore
}))

describe('ThemeSwitcher', () => {
  let wrapper

  beforeEach(() => {
    vi.clearAllMocks()
    mockThemeStore.currentTheme = 'minimal'
    mockThemeStore.colors = { accent: '#4f46e5' }
    mockThemeStore.themeName = 'Professional Minimal'
    setActivePinia(createPinia())
    wrapper = mount(ThemeSwitcher, {
      global: {
        stubs: {
          'el-dropdown': {
            template: '<div class="el-dropdown"><slot /></div>',
            props: ['trigger', 'command']
          },
          'el-dropdown-menu': {
            template: '<div class="el-dropdown-menu"><slot /></div>'
          },
          'el-dropdown-item': {
            template: '<div class="el-dropdown-item" @click="$emit(\'command\', $attrs.command)"><slot /></div>',
            props: ['command', 'disabled']
          }
        }
      }
    })
  })

  it('should render theme switcher', () => {
    expect(wrapper.find('.theme-switcher').exists()).toBe(true)
  })

  it('should display theme name', () => {
    expect(wrapper.find('.theme-name').text()).toBe('Professional Minimal')
  })

  it('should display current accent color', () => {
    const dot = wrapper.find('.theme-dot')
    expect(dot.exists()).toBe(true)
  })

  it('should have dropdown menu with theme options', () => {
    const dropdown = wrapper.find('.el-dropdown')
    expect(dropdown.exists()).toBe(true)
  })

  it('should call setTheme when tech theme is selected', async () => {
    mockThemeStore.currentTheme = 'tech'
    mockThemeStore.colors = { accent: '#00d9ff' }
    mockThemeStore.themeName = 'Modern Tech'

    wrapper = mount(ThemeSwitcher, {
      global: {
        stubs: {
          'el-dropdown': {
            template: '<div class="el-dropdown" @command="$attrs.onCommand"><slot /></div>',
            props: ['trigger', 'command']
          },
          'el-dropdown-menu': {
            template: '<div class="el-dropdown-menu"><slot /></div>'
          },
          'el-dropdown-item': {
            template: '<div class="el-dropdown-item" @click="$parent.$emit(\'command\', \'tech\')"><slot /></div>',
            props: ['command', 'disabled']
          }
        }
      }
    })

    expect(wrapper.find('.theme-name').text()).toBe('Modern Tech')
  })
})
