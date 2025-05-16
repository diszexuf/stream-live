<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AuthCard from '@/components/auth/AuthCard.vue'

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
</script>

<template>
  <auth-card title="Авторизация">
    <v-form @submit.prevent="login">
      <v-text-field
        v-model="username"
        label="Имя пользователя"
        required
        :rules="[v => !!v || 'Введите имя пользователя']"
        prepend-inner-icon="mdi-account"
        variant="outlined"
        density="comfortable"
      ></v-text-field>
      
      <v-text-field
        v-model="password"
        label="Пароль"
        type="password"
        required
        :rules="[v => !!v || 'Введите пароль']"
        prepend-inner-icon="mdi-lock"
        variant="outlined"
        density="comfortable"
      ></v-text-field>
      
      <v-alert
        v-if="errorMessage"
        type="error"
        variant="tonal"
        border="start"
      >
        {{ errorMessage }}
      </v-alert>
      
      <v-btn
        color="primary"
        type="submit"
        block
        :loading="isLoading"
        class="mt-2"
      >
        Войти
      </v-btn>

      <div class="text-center mt-6">
        <p class="text-body-2 text-medium-emphasis mb-2">
          Еще нет аккаунта?
        </p>
        <v-btn
          variant="text"
          to="/register"
          class="text-none"
        >
          Зарегистрироваться
        </v-btn>
      </div>
    </v-form>
  </auth-card>
</template>
