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

// Импортируем HTTP-клиент
import { httpClient } from './api/manual'

const vuetify = createVuetify({
    components,
    directives,
    theme: {
        defaultTheme: 'myLightTheme',
        themes: {
            myLightTheme: {
                dark: false,
                colors: {
                    background: '#f5f5f5', // Мягкий серый-белый
                    surface: '#fafafa',   // Светло-серый
                    primary: '#2196F3',   // Более тёплый синий
                    secondary: '#757575',  // Средний серый
                    info: '#2196F3',
                    success: '#4CAF50',
                    warning: '#FB8C00',
                    error: '#FF5252',
                    onSurface: '#333333'   // Текст на поверхности
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

// Настройка обработки ошибок CORS
httpClient.handleResponse = async function(response) {
    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        
        // Логирование ошибок CORS
        if (response.status === 0 || response.type === 'opaque') {
            console.error('CORS Error:', response);
        }
        
        throw {
            status: response.status,
            statusText: response.statusText,
            ...error
        };
    }

    // Для ответов без тела (например, 204 No Content)
    if (response.status === 204) {
        return null;
    }

    return response.json();
};

const pinia = createPinia()
const app = createApp(App)

// Инициализация приложения
app.use(pinia)
app.use(router)
app.use(vuetify)
app.mount('#app')
