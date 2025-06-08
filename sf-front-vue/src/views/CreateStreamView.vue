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
    if (!userStore.isAuthenticated || !userStore.token) {
      console.error('Пользователь не авторизован')
      throw new Error('Необходимо авторизоваться')
    }

    const streamData = {
      title: streamTitle.value,
      description: streamDescription.value
    }

    console.log('Создание стрима с данными:', streamData)

    const result = await streamStore.createStream(streamData)
    
    if (result) {
      await streamStore.fetchCurrentUserStreams()
      
      const userStreams = streamStore.currentUserStreams
      if (userStreams && userStreams.length > 0) {
        const newStream = userStreams[userStreams.length - 1]

        if (newStream && newStream.id) {

          setTimeout(async () => {
            try {
              await router.push(`/stream/${newStream.id}`)
            } catch (routerError) {
              console.error('Ошибка при переходе на страницу стрима:', routerError)
            }
          }, 1000)
        } else {
          throw new Error('Не удалось получить ID нового стрима')
        }
      } else {
        throw new Error('Список стримов пуст после создания')
      }
    } else {
      throw new Error(streamStore.error || 'Не удалось создать стрим')
    }
  } catch (error) {
    console.error('Ошибка при создании стрима:', error)
    alert(`Не удалось создать стрим: ${error.message || 'Неизвестная ошибка'}`)
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
