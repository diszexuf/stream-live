<script setup>
import {computed, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import AuthCard from '@/components/auth/AuthCard.vue'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const passwordError = ref('')
const privacyPolicyAccepted = ref(false)
const checkingUsername = ref(false)
const checkingEmail = ref(false)
const usernameAvailable = ref(true)
const emailAvailable = ref(true)
const usernameCheckInProgressFor = ref('')
const errorMessage = ref('')
const usernameError = ref('')

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

const validateEmail = (email) => {
  if (!email) return 'Введите email'
  if (!emailRegex.test(email)) return 'Некорректный формат email'
  if (!emailAvailable.value && emailRegex.test(email)) return 'Этот email занят'
  return true
}

const isLoading = ref(false)

const formValid = computed(() => {
  return (
      username.value.length >= 3 &&
      validateEmail(email.value) === true &&
      password.value.length >= 6 &&
      password.value === confirmPassword.value &&
      !passwordError.value &&
      usernameAvailable.value &&
      privacyPolicyAccepted.value
  )
})

const usernameSuccessMessage = computed(() => {
  if (!checkingUsername.value && username.value.length >= 3 && usernameAvailable.value) {
    return 'Это имя доступно!'
  }
  return ''
})

const emailSuccessMessage = computed(() => {
  return !checkingEmail.value && emailRegex.test(email.value) && emailAvailable.value
      ? 'Этот email доступен!'
      : ''
})

function debounce(fn, delay = 500) {
  let timeout
  return function (...args) {
    clearTimeout(timeout)
    timeout = setTimeout(() => fn(...args), delay)
  }
}

async function checkUsername(value) {
  if (value.length < 3) {
    usernameAvailable.value = true
    usernameError.value = ''
    checkingUsername.value = false
    return
  }

  usernameCheckInProgressFor.value = value
  checkingUsername.value = true

  try {
    const available = await userStore.checkFieldAvailability('username', value)

    if (usernameCheckInProgressFor.value === value) {
      usernameAvailable.value = available
      usernameError.value = available ? '' : 'Это имя занято'
    }
  } finally {
    if (usernameCheckInProgressFor.value === value) {
      checkingUsername.value = false
    }
  }
}

async function checkEmail(value) {
  if (!emailRegex.test(value)) {
    emailAvailable.value = true
    checkingEmail.value = false
    return
  }

  checkingEmail.value = true
  try {
    emailAvailable.value = await userStore.checkFieldAvailability('email', value)
  } finally {
    checkingEmail.value = false
  }
}

const debouncedCheckUsername = debounce(checkUsername)
const debouncedCheckEmail = debounce(checkEmail)

watch(username, (newVal) => {
  debouncedCheckUsername(newVal)
})

watch(email, (newVal) => {
  debouncedCheckEmail(newVal)
})

watch(privacyPolicyAccepted, (newVal) => {
  if (newVal) {
    if (username.value.length >= 3) {
      debouncedCheckUsername(username.value)
    }
    if (emailRegex.test(email.value)) {
      debouncedCheckEmail(email.value)
    }
  }
})

watch(password, (newVal) => {
  if (confirmPassword.value && newVal !== confirmPassword.value) {
    passwordError.value = 'Пароли не совпадают'
  } else {
    passwordError.value = ''
  }
})

watch(confirmPassword, (newVal) => {
  if (newVal !== password.value) {
    passwordError.value = 'Пароли не совпадают'
  } else {
    passwordError.value = ''
  }
})

const register = async () => {
  if (!formValid.value) {
    errorMessage.value = 'Пожалуйста, исправьте ошибки перед регистрацией'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const success = await userStore.register(username.value, email.value, password.value)
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
          :error-messages="usernameError"
          prepend-inner-icon="mdi-account"
          variant="outlined"
          density="comfortable"
      >
        <template v-slot:details>
          <div v-if="checkingUsername" class="d-flex align-center">
            <v-progress-circular size="20" indeterminate color="primary"/>
            <span class="ml-2">Проверяем доступность...</span>
          </div>
          <div v-else-if="usernameSuccessMessage" class="d-flex align-center">
            <v-icon color="success">mdi-check-circle</v-icon>
            <span class="ml-2 text-success">{{ usernameSuccessMessage }}</span>
          </div>
        </template>
      </v-text-field>

      <v-text-field
          v-model="email"
          label="Email"
          type="email"
          required
          :rules="[validateEmail]"
          prepend-inner-icon="mdi-email"
          variant="outlined"
          density="comfortable"
      >
        <template v-slot:details>
          <div v-if="checkingEmail" class="d-flex align-center">
            <v-progress-circular size="20" indeterminate color="primary"/>
            <span class="ml-2">Проверяем доступность...</span>
          </div>
          <div v-else-if="emailSuccessMessage" class="d-flex align-center">
            <v-icon color="success">mdi-check-circle</v-icon>
            <span class="ml-2 text-success">{{ emailSuccessMessage }}</span>
          </div>
        </template>
      </v-text-field>

      <v-text-field
          v-model="password"
          label="Пароль"
          type="password"
          required
          :rules="[
            v => !!v || 'Введите пароль',
            v => v.length >= 6 || 'Пароль должен содержать минимум 6 символов'
          ]"
          prepend-inner-icon="mdi-lock"
          variant="outlined"
          density="comfortable"
          @input="() => { if (confirmPassword) passwordError = password !== confirmPassword ? 'Пароли не совпадают' : '' }"
      />

      <v-text-field
          v-model="confirmPassword"
          label="Подтвердите пароль"
          type="password"
          required
          :rules="[
            v => !!v || 'Подтвердите пароль',
            v => v === password || 'Пароли не совпадают'
          ]"
          :error-messages="passwordError"
          prepend-inner-icon="mdi-lock-check"
          variant="outlined"
          density="comfortable"
          @input="passwordError = password !== confirmPassword ? 'Пароли не совпадают' : ''"
      />

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
          class="mt-2"
      >
        {{ errorMessage }}
      </v-alert>

      <v-btn
          color="primary"
          type="submit"
          block
          :loading="isLoading"
          :disabled="!formValid"
          class="mt-2"
      >
        Зарегистрироваться
      </v-btn>

      <div class="text-center mt-6">
        <p class="text-body-2 text-medium-emphasis mb-2">
          Уже есть аккаунт?
        </p>
        <v-btn variant="text" to="/login" class="text-none">
          Войти
        </v-btn>
      </div>
    </v-form>
  </auth-card>
</template>