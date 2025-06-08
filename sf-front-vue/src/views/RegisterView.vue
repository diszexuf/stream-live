<script setup>
import {ref, watch, computed} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import AuthCard from '@/components/auth/AuthCard.vue'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const privacyPolicyAccepted = ref(false)

const checkingUsername = ref(false)
const checkingEmail = ref(false)
const usernameAvailable = ref(true)
const emailAvailable = ref(true)

const errorMessage = ref('')
const usernameError = ref('')
const emailError = ref('')

const isLoading = ref(false)

const formValid = computed(() => {
  const isValid = (
      username.value.length >= 3 &&
      email.value.includes('@') &&
      password.value.length >= 6 &&
      password.value === confirmPassword.value &&
      usernameAvailable.value &&
      emailAvailable.value &&
      privacyPolicyAccepted.value
  )
  console.log('Form Valid:', {
    usernameLength: username.value.length,
    emailValid: email.value.includes('@'),
    passwordLength: password.value.length,
    passwordsMatch: password.value === confirmPassword.value,
    usernameAvailable: usernameAvailable.value,
    emailAvailable: emailAvailable.value,
    privacyPolicyAccepted: privacyPolicyAccepted.value,
    isValid
  })
  return isValid
})

const usernameSuccessMessage = computed(() => {
  if (!checkingUsername.value && username.value.length >= 3 && usernameAvailable.value) {
    return 'Это имя доступно!'
  }
  return ''
})

const emailSuccessMessage = computed(() => {
  if (!checkingEmail.value && email.value.includes('@') && emailAvailable.value) {
    return 'Этот email доступен!'
  }
  return ''
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

  checkingUsername.value = true
  try {
    const available = await userStore.checkFieldAvailability('username', value)
    usernameAvailable.value = available
    usernameError.value = available ? '' : 'Это имя занято'
  } finally {
    checkingUsername.value = false
  }
}

async function checkEmail(value) {
  if (!value.includes('@')) {
    emailAvailable.value = true
    emailError.value = ''
    checkingEmail.value = false
    return
  }

  checkingEmail.value = true
  try {
    const available = await userStore.checkFieldAvailability('email', value)
    emailAvailable.value = available
    emailError.value = available ? '' : 'Этот email занят'
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
    debouncedCheckUsername(username.value)
    debouncedCheckEmail(email.value)
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
          :rules="[v => !!v || 'Введите email']"
          :error-messages="emailError"
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
          :rules="[v => !!v || 'Введите пароль']"
          prepend-inner-icon="mdi-lock"
          variant="outlined"
          density="comfortable"
      />

      <v-text-field
          v-model="confirmPassword"
          label="Подтвердите пароль"
          type="password"
          required
          :rules="[v => v === password || 'Пароли не совпадают']"
          prepend-inner-icon="mdi-lock-check"
          variant="outlined"
          density="comfortable"
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