import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import { router } from './router'
import { setupPermissionDirective } from './directives/permission'
import './styles/main.css'

const app = createApp(App)
setupPermissionDirective(app)
app.use(createPinia()).use(router).mount('#app')
