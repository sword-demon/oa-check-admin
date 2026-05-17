import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import '@/styles/theme.scss'
import '@/styles/app.scss'
import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'

async function bootstrap() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)
  app.use(ElementPlus, { locale: zhCn })

  const userStore = useUserStore(pinia)

  if (userStore.token) {
    try {
      await userStore.hydrate()
    } catch {
      // Request interceptor handles invalid sessions.
    }
  }

  app.mount('#app')
}

void bootstrap()
