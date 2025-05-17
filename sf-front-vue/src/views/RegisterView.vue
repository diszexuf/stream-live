<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AuthCard from '@/components/auth/AuthCard.vue'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const errorMessage = ref('')
const isLoading = ref(false)
const privacyPolicyAccepted = ref(false)

const register = async () => {
  if (!username.value || !email.value || !password.value || !confirmPassword.value) {
    errorMessage.value = 'Пожалуйста, заполните все поля'
    return
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'Пароли не совпадают'
    return
  }

  if (!privacyPolicyAccepted.value) {
    errorMessage.value = 'Необходимо принять политику конфиденциальности'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const success = await userStore.register(
        username.value,
        email.value,
        password.value
    )

    if (success) {
      await router.push('/')
    } else {
      errorMessage.value = 'Ошибка при регистрации'
    }
  } catch (error) {
    console.error('Ошибка при регистрации:', error)
    errorMessage.value = 'Произошла ошибка при регистрации'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <auth-card title="Регистрация">
    <v-form @submit.prevent="register">
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
          v-model="email"
          label="Email"
          type="email"
          required
          :rules="[v => !!v || 'Введите email']"
          prepend-inner-icon="mdi-email"
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

      <v-text-field
          v-model="confirmPassword"
          label="Подтвердите пароль"
          type="password"
          required
          :rules="[v => v === password || 'Пароли не совпадают']"
          prepend-inner-icon="mdi-lock-check"
          variant="outlined"
          density="comfortable"
      ></v-text-field>

      <v-checkbox
          v-model="privacyPolicyAccepted"
          color="primary"
          hide-details
          class="mb-4"
      >
        <template v-slot:label>
          <div>
            Я согласен с 
            <router-link to="/privacy-policy" target="_blank" class="text-primary">
              политикой конфиденциальности
            </router-link>
          </div>
        </template>
      </v-checkbox>

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
          :disabled="!privacyPolicyAccepted"
          class="mt-2"
      >
        Зарегистрироваться
      </v-btn>

      <div class="text-center mt-6">
        <p class="text-body-2 text-medium-emphasis mb-2">
          Уже есть аккаунт?
        </p>
        <v-btn
            variant="text"
            to="/login"
            class="text-none"
        >
          Войти
        </v-btn>
      </div>
    </v-form>
  </auth-card>
</template>