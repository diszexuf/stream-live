import './assets/main.css'

import {createApp} from 'vue'
import {createPinia} from 'pinia'
import App from './App.vue'
import router from './router'
import 'vuetify/styles'
import {createVuetify} from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'

const vuetify = createVuetify({
    components,
    directives,
    theme: {
        defaultTheme: 'myLightTheme',
        themes: {
            myLightTheme: {
                dark: false,
                colors: {
                    background: '#f5f5f5',
                    surface: '#fafafa',
                    primary: '#2196F3',
                    secondary: '#757575',
                    info: '#2196F3',
                    success: '#4CAF50',
                    warning: '#FB8C00',
                    error: '#FF5252',
                    onSurface: '#333333'
                },
                variables: {
                    'border-color': '#ccc',
                    'border-opacity': 0.8,
                    'card-border-radius': '12px',
                    'button-height': '40px',
                    'font-size': '16px',
                    'input-height': '56px',
                    'card-elevation': '4',
                }
            }
        }
    }
})

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(vuetify)
app.mount('#app')
