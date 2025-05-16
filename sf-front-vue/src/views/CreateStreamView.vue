<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import axios from 'axios'
import {useUserStore} from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const streamTitle = ref('')
const streamDescription = ref('')
const isLoading = ref(false)
const tags = ref([])
const apiUrl = 'http://localhost:8080/api'
const startStream = async () => {
  if (!streamTitle.value || !selectedCategoryId.value) {
    alert('Пожалуйста, заполните все обязательные поля')
    return
  }

  isLoading.value = true
  try {
    if (!userStore.user?.id) {
      await userStore.fetchUser()
      if (!userStore.user?.id) {
        throw new Error('Пользователь не авторизован')
      }
    }

    const response = await axios.post(`${apiUrl}/streams`, {
      title: streamTitle.value,
      description: streamDescription.value,
    }, {
      params: {
        userId: userStore.user.id,
      }
    })

    if (response.data && response.data.id) {
      await axios.post(`${apiUrl}/streams/${response.data.id}/start`)
      await router.push(`/stream/${response.data.id}`)
    } else {
      throw new Error('Неверный ответ от сервера')
    }
  } catch (error) {
    console.error('Ошибка при создании стрима:', error)
    alert('Не удалось создать стрим. Пожалуйста, попробуйте еще раз.')
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  if (!userStore.user) {
    await userStore.fetchUser()
  }
})

</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12" md="8" offset-md="2">
        <v-card>
          <v-card-title class="text-h4 mb-4">Начать стрим</v-card-title>

          <v-card-text v-if="!userStore.isAuthenticated">
            <v-alert type="warning">
              Для создания стрима необходимо авторизоваться
            </v-alert>
            <v-btn color="primary" block class="mt-4" to="/login">
              Войти в аккаунт
            </v-btn>
          </v-card-text>

          <v-card-text v-else>
            <v-form @submit.prevent="startStream">
              <v-text-field
                  v-model="streamTitle"
                  label="Название стрима"
                  required
                  :rules="[v => !!v || 'Введите название стрима']"
                  class="mb-4"
              ></v-text-field>

              <v-textarea
                  v-model="streamDescription"
                  label="Описание"
                  rows="3"
                  class="mb-4"
              ></v-textarea>

              <v-btn
                  color="primary"
                  type="submit"
                  block
                  :loading="isLoading"
              >
                Начать стрим
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
