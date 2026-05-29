import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Bootstrap CSS + Icons
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Estilos globales + tema Metro UI
import './assets/css/main.css'
import './assets/css/metro.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
