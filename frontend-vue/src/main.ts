import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

/**
 * 应用入口：组装 Vue 应用并挂载到 index.html 的 #app。
 * - Pinia：全局状态管理（当前页面以本地 ref 为主，Pinia 为后续扩展预留）
 * - Router：hash 路由（见 src/router/index.ts）
 * - Element Plus：UI 组件库，注册后模板中可直接使用 el-* 组件
 */
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
