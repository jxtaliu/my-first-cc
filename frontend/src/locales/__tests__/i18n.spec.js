import { describe, it, expect } from 'vitest'
import zhCN from '../zh-CN.js'
import enUS from '../en-US.js'

describe('Locale files', () => {
  describe('zh-CN.js', () => {
    it('should have all required keys in common', () => {
      expect(zhCN.common).toBeDefined()
      expect(zhCN.common.save).toBe('保存')
      expect(zhCN.common.cancel).toBe('取消')
      expect(zhCN.common.delete).toBe('删除')
      expect(zhCN.common.edit).toBe('编辑')
      expect(zhCN.common.add).toBe('添加')
    })

    it('should have all required keys in auth', () => {
      expect(zhCN.auth).toBeDefined()
      expect(zhCN.auth.login).toBe('登录')
      expect(zhCN.auth.username).toBe('用户名')
      expect(zhCN.auth.password).toBe('密码')
    })

    it('should have all required keys in nav', () => {
      expect(zhCN.nav).toBeDefined()
      expect(zhCN.nav.dashboard).toBe('仪表盘')
      expect(zhCN.nav.projects).toBe('项目管理')
      expect(zhCN.nav.timesheet).toBe('工时管理')
    })

    it('should have all required keys in dashboard', () => {
      expect(zhCN.dashboard).toBeDefined()
      expect(zhCN.dashboard.projects).toBe('项目数')
      expect(zhCN.dashboard.tasks).toBe('任务数')
      expect(zhCN.dashboard.hoursThisWeek).toBe('本周工时')
    })

    it('should have all required keys in project', () => {
      expect(zhCN.project).toBeDefined()
      expect(zhCN.project.name).toBe('项目名称')
      expect(zhCN.project.description).toBe('描述')
      expect(zhCN.project.scrum).toBe('Scrum')
      expect(zhCN.project.kanban).toBe('Kanban')
    })

    it('should have all required keys in timesheet', () => {
      expect(zhCN.timesheet).toBeDefined()
      expect(zhCN.timesheet.title).toBe('工时管理')
      expect(zhCN.timesheet.totalHours).toBe('总工时')
      expect(zhCN.timesheet.approved).toBe('已审批')
    })

    it('should have all required keys in admin', () => {
      expect(zhCN.admin).toBeDefined()
      expect(zhCN.admin.title).toBe('用户管理')
      expect(zhCN.admin.addUser).toBe('添加用户')
      expect(zhCN.admin.superAdmin).toBe('超级管理员')
    })
  })

  describe('en-US.js', () => {
    it('should have all required keys in common', () => {
      expect(enUS.common).toBeDefined()
      expect(enUS.common.save).toBe('Save')
      expect(enUS.common.cancel).toBe('Cancel')
    })

    it('should have all required keys in auth', () => {
      expect(enUS.auth).toBeDefined()
      expect(enUS.auth.login).toBe('Login')
      expect(enUS.auth.username).toBe('Username')
    })

    it('should have all required keys in nav', () => {
      expect(enUS.nav).toBeDefined()
      expect(enUS.nav.dashboard).toBe('Dashboard')
      expect(enUS.nav.projects).toBe('Projects')
    })

    it('should have all required keys in dashboard', () => {
      expect(enUS.dashboard).toBeDefined()
      expect(enUS.dashboard.projects).toBe('Projects')
      expect(enUS.dashboard.tasks).toBe('Tasks')
    })

    it('should have all required keys in project', () => {
      expect(enUS.project).toBeDefined()
      expect(enUS.project.name).toBe('Project Name')
      expect(enUS.project.scrum).toBe('Scrum')
    })

    it('should have all required keys in timesheet', () => {
      expect(enUS.timesheet).toBeDefined()
      expect(enUS.timesheet.title).toBe('Timesheet')
      expect(enUS.timesheet.totalHours).toBe('Total Hours')
    })

    it('should have all required keys in admin', () => {
      expect(enUS.admin).toBeDefined()
      expect(enUS.admin.title).toBe('User Management')
      expect(enUS.admin.superAdmin).toBe('Super Admin')
    })
  })

  describe('locale key consistency', () => {
    const zhKeys = getAllKeys(zhCN)
    const enKeys = getAllKeys(enUS)

    function getAllKeys(obj, prefix = '') {
      let keys = []
      for (const key in obj) {
        const fullKey = prefix ? `${prefix}.${key}` : key
        if (typeof obj[key] === 'object') {
          keys = keys.concat(getAllKeys(obj[key], fullKey))
        } else {
          keys.push(fullKey)
        }
      }
      return keys
    }

    it('should have matching keys between zh-CN and en-US', () => {
      const zhKeySet = new Set(zhKeys)
      const enKeySet = new Set(enKeys)

      const missingInEn = zhKeys.filter(k => !enKeySet.has(k))
      const missingInZh = enKeys.filter(k => !zhKeySet.has(k))

      expect(missingInEn).toEqual([])
      expect(missingInZh).toEqual([])
    })
  })
})
