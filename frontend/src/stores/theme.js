import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    currentTheme: localStorage.getItem('theme') || 'minimal', // minimal | tech | vibrant | dark
    themes: {
      minimal: {
        name: 'Professional Minimal',
        colors: {
          primary: '#1a1a2e',
          accent: '#4f46e5',
          bg: '#ffffff',
          text: '#1f2937',
          textLight: '#6b7280',
          border: '#e5e7eb',
          cardBg: '#f9fafb'
        }
      },
      tech: {
        name: 'Modern Tech',
        colors: {
          primary: '#161b22',
          accent: '#00d9ff',
          bg: '#0d1117',
          text: '#e6edf3',
          textLight: '#8b949e',
          border: '#30363d',
          cardBg: '#161b22'
        }
      },
      vibrant: {
        name: 'Vibrant & Playful',
        colors: {
          primary: '#1c1917',
          accent: '#f97316',
          bg: '#fefce8',
          text: '#1c1917',
          textLight: '#78716c',
          border: '#f5f5f4',
          cardBg: '#ffffff'
        }
      },
      dark: {
        name: 'Dark Luxe',
        colors: {
          primary: '#141414',
          accent: '#a855f7',
          bg: '#0f0f0f',
          text: '#fafafa',
          textLight: '#737373',
          border: '#262626',
          cardBg: '#1f1f1f'
        }
      }
    }
  }),

  getters: {
    colors: (state) => state.themes[state.currentTheme].colors,
    themeName: (state) => state.themes[state.currentTheme].name
  },

  actions: {
    setTheme(theme) {
      if (this.themes[theme]) {
        this.currentTheme = theme
        localStorage.setItem('theme', theme)
        this.applyTheme()
      }
    },

    applyTheme() {
      const colors = this.colors
      const root = document.documentElement
      root.style.setProperty('--theme-primary', colors.primary)
      root.style.setProperty('--theme-accent', colors.accent)
      root.style.setProperty('--theme-bg', colors.bg)
      root.style.setProperty('--theme-text', colors.text)
      root.style.setProperty('--theme-text-light', colors.textLight)
      root.style.setProperty('--theme-border', colors.border)
      root.style.setProperty('--theme-card-bg', colors.cardBg)
    }
  }
})
