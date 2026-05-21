import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useThemeStore } from '../theme.js'

describe('useThemeStore', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('should have default theme as minimal', () => {
    const store = useThemeStore()
    expect(store.currentTheme).toBe('minimal')
  })

  it('should have all four themes defined', () => {
    const store = useThemeStore()
    expect(store.themes.minimal).toBeDefined()
    expect(store.themes.tech).toBeDefined()
    expect(store.themes.vibrant).toBeDefined()
    expect(store.themes.dark).toBeDefined()
  })

  it('should return colors for current theme', () => {
    const store = useThemeStore()
    const colors = store.colors
    expect(colors.primary).toBe('#1a1a2e')
    expect(colors.accent).toBe('#4f46e5')
    expect(colors.bg).toBe('#ffffff')
  })

  it('should return theme name', () => {
    const store = useThemeStore()
    expect(store.themeName).toBe('Professional Minimal')
  })

  it('should switch theme and persist to localStorage', () => {
    const store = useThemeStore()
    store.setTheme('tech')
    expect(store.currentTheme).toBe('tech')
    expect(localStorage.getItem('theme')).toBe('tech')
  })

  it('should apply correct colors when switching to tech theme', () => {
    const store = useThemeStore()
    store.setTheme('tech')
    expect(store.colors.primary).toBe('#161b22')
    expect(store.colors.accent).toBe('#00d9ff')
    expect(store.colors.bg).toBe('#0d1117')
  })

  it('should apply correct colors when switching to vibrant theme', () => {
    const store = useThemeStore()
    store.setTheme('vibrant')
    expect(store.colors.primary).toBe('#1c1917')
    expect(store.colors.accent).toBe('#f97316')
    expect(store.colors.bg).toBe('#fefce8')
  })

  it('should apply correct colors when switching to dark theme', () => {
    const store = useThemeStore()
    store.setTheme('dark')
    expect(store.colors.primary).toBe('#141414')
    expect(store.colors.accent).toBe('#a855f7')
    expect(store.colors.bg).toBe('#0f0f0f')
  })

  it('should not change theme for invalid theme key', () => {
    const store = useThemeStore()
    const initialTheme = store.currentTheme
    store.setTheme('invalid-theme')
    expect(store.currentTheme).toBe(initialTheme)
    expect(localStorage.getItem('theme')).toBeNull()
  })

  it('should restore theme from localStorage', () => {
    localStorage.setItem('theme', 'dark')
    const store = useThemeStore()
    expect(store.currentTheme).toBe('dark')
    expect(store.themeName).toBe('Dark Luxe')
  })

  it('should apply theme to CSS variables', () => {
    const store = useThemeStore()
    store.setTheme('tech')
    store.applyTheme()
    const root = document.documentElement
    expect(root.style.getPropertyValue('--theme-primary')).toBe('#161b22')
    expect(root.style.getPropertyValue('--theme-accent')).toBe('#00d9ff')
    expect(root.style.getPropertyValue('--theme-bg')).toBe('#0d1117')
  })
})
