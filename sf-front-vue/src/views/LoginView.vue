<template>
  <v-container>
    <v-row>
      <v-col cols="12" md="6" offset-md="3">
        <v-card>
          <v-card-title class="text-h4 mb-4">Авторизация</v-card-title>
          
          <v-card-text>
            <v-form @submit.prevent="login">
              <v-text-field
                v-model="username"
                label="Имя пользователя"
                required
                :rules="[v => !!v || 'Введите имя пользователя']"
                class="mb-4"
              ></v-text-field>
              
              <v-text-field
                v-model="password"
                label="Пароль"
                type="password"
                required
                :rules="[v => !!v || 'Введите пароль']"
                class="mb-4"
              ></v-text-field>
              
              <v-alert
                v-if="errorMessage"
                type="error"
                class="mb-4"
              >
                {{ errorMessage }}
              </v-alert>
              
              <v-btn
                color="primary"
                type="submit"
                block
                :loading="isLoading"
              >
                Войти
              </v-btn>
            </v-form>
          </v-card-text>
          
          <v-card-text class="text-center">
            <p>
              Для демонстрации вы можете использовать тестовые данные:
            </p>
            <p>
              <strong>Имя пользователя:</strong> streamMaster<br>
              <strong>Пароль:</strong> password
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const username = ref('')
    const password = ref('')
    const errorMessage = ref('')
    const isLoading = ref(false)
    
    const login = async () => {
      if (!username.value || !password.value) {
        errorMessage.value = 'Пожалуйста, заполните все поля'
        return
      }
      
      isLoading.value = true
      try {
        const success = await userStore.login(username.value, password.value)
        
        if (success) {
          // Перенаправляем на главную страницу
          router.push('/')
        } else {
          errorMessage.value = 'Неверное имя пользователя или пароль'
        }
      } catch (error) {
        console.error('Ошибка при авторизации:', error)
        errorMessage.value = 'Произошла ошибка при авторизации, попробуйте еще раз'
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      username,
      password,
      errorMessage,
      isLoading,
      login
    }
  }
}
</script> 