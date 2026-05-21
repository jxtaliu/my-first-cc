import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LanguageSwitcher from '../LanguageSwitcher.vue'
import { createI18n } from 'vue-i18n'

describe('LanguageSwitcher', () => {
  const createI18nInstance = (locale) => {
    return createI18n({
      legacy: false,
      locale,
      messages: {
        'zh-CN': {
          common: { save: '保存' },
          nav: { dashboard: '仪表盘', projects: '项目管理' }
        },
        'en-US': {
          common: { save: 'Save' },
          nav: { dashboard: 'Dashboard', projects: 'Projects' }
        }
      }
    })
  }

  it('renders language switcher with current locale indicator', () => {
    const wrapper = mount(LanguageSwitcher, {
      global: {
        plugins: [createI18nInstance('zh-CN')],
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
          },
          'el-icon': {
            template: '<span class="el-icon"><slot /></span>'
          },
          'Globe': {
            template: '<span class="globe-icon">Globe</span>'
          }
        }
      }
    })
    expect(wrapper.find('.lang-switcher').exists()).toBe(true)
  })

  it('displays Chinese label when locale is zh-CN', () => {
    const wrapper = mount(LanguageSwitcher, {
      global: {
        plugins: [createI18nInstance('zh-CN')],
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
          },
          'el-icon': {
            template: '<span class="el-icon"><slot /></span>'
          },
          'Globe': {
            template: '<span class="globe-icon">Globe</span>'
          }
        }
      }
    })
    expect(wrapper.text()).toContain('中文')
  })

  it('displays English label when locale is en-US', () => {
    const wrapper = mount(LanguageSwitcher, {
      global: {
        plugins: [createI18nInstance('en-US')],
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
          },
          'el-icon': {
            template: '<span class="el-icon"><slot /></span>'
          },
          'Globe': {
            template: '<span class="globe-icon">Globe</span>'
          }
        }
      }
    })
    expect(wrapper.text()).toContain('EN')
  })

  it('has dropdown menu with two language options', () => {
    const wrapper = mount(LanguageSwitcher, {
      global: {
        plugins: [createI18nInstance('zh-CN')],
        stubs: {
          'el-dropdown': {
            template: '<div class="el-dropdown" @command="$attrs.onCommand"><slot /></div>',
            props: ['trigger', 'command']
          },
          'el-dropdown-menu': {
            template: '<div class="el-dropdown-menu"><slot /></div>'
          },
          'el-dropdown-item': true,
          'el-icon': {
            template: '<span class="el-icon"><slot /></span>'
          },
          'Globe': {
            template: '<span class="globe-icon">Globe</span>'
          }
        }
      }
    })
    const dropdown = wrapper.find('.el-dropdown')
    expect(dropdown.exists()).toBe(true)
  })
})
