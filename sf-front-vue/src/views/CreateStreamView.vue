<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {useStreamStore} from '@/stores/stream'

const router = useRouter()
const userStore = useUserStore()
const streamStore = useStreamStore()
const streamTitle = ref('')
const streamDescription = ref('')
const isLoading = ref(false)

const startStream = async () => {
  if (!streamTitle.value) {
    alert('Пожалуйста, введите название стрима')
    return
  }

  isLoading.value = true
  try {
    // Создаем объект с данными стрима
    const streamData = {
      title: streamTitle.value,
      description: streamDescription.value
    }

    // Используем метод createStream из хранилища стримов
    const result = await streamStore.createStream(streamData)
    
    if (result) {
      // Если стрим создан успешно, переходим на страницу стрима
      const newStreamId = streamStore.currentUserStreams[streamStore.currentUserStreams.length - 1].id
      await router.push(`/stream/${newStreamId}`)
    } else {
      throw new Error(streamStore.error || 'Не удалось создать стрим')
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
    await userStore.fetchCurrentUser()
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
