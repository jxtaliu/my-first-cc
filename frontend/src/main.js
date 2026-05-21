import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import i18n from './locales'
import App from './App.vue'
import { useThemeStore } from '@/stores/theme'

const app = createApp(App)

// Register all icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)

// Apply theme before mounting
const themeStore = useThemeStore()
themeStore.applyTheme()

app.use(router)
app.use(ElementPlus)
app.use(i18n)

app.mount('#app')
