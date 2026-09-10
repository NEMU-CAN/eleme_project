import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { useHungryStore } from './composables/useHungryStore'
import './styles/global.css'

// 应用入口：创建 Vue 实例并挂载路由与全局样式。
const app = createApp(App)
const store = useHungryStore()

app.use(router)

await store.initialize().catch(() => undefined)

app.mount('#app')
