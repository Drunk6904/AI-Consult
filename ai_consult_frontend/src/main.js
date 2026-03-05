import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import permissionDirective from './directives/permission'
import './utils/axios'

const app = createApp(App)
app.directive('permission', permissionDirective)
app.mount('#app')
