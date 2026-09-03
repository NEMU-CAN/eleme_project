import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/global.css'

// 应用入口：创建 Vue 实例并挂载路由与全局样式。
const app = createApp(App)

app.use(router)

app.mount('#app')
