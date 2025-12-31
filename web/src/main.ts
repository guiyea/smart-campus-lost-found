import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Import Element Plus styles
import 'element-plus/theme-chalk/index.css'

// Import global styles
import './styles/index.scss'

const app = createApp(App)

// Use plugins
app.use(createPinia())
app.use(router)

app.mount('#app')





