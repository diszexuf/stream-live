<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { UsersApi } from '@/api/src/index.js'
import { onMounted } from 'vue'

const userStore = useUserStore()
const usersApi = new UsersApi()

const activeTab = ref('info')
const streamData = ref({
  streamKey: ''
})

const errorMessage = ref('')
const isLoading = ref(false)

// Сохранение данных профиля
const saveProfile = async () => {
  isLoading.value = true
  errorMessage.value = ''

  const payload = {
    email: userStore.user.email,
    bio: userStore.user.bio,
    avatarUrl: userStore.user.avatarUrl,
  }

  try {
    await usersApi.updateUser(userStore.user.id, payload)
    await userStore.fetchCurrentUser()
  } catch (error) {
    console.error('Ошибка при обновлении профиля:', error.response?.data || error.message)
    errorMessage.value = 'Ошибка при сохранении профиля'
  } finally {
    isLoading.value = false
  }
}

// Получение ключа стрима
const fetchStreamKey = async () => {
  try {
    const response = await usersApi.getStreamKey()
    streamData.value.streamKey = response.newStreamKey
  } catch (error) {
    console.error('Ошибка при получении ключа стрима:', error)
    streamData.value.streamKey = 'Не удалось загрузить ключ'
  }
}

// Сброс ключа стрима
const resetStreamKey = async () => {
  if (!confirm('Вы уверены, что хотите сбросить ключ стрима? Текущий стрим будет прерван.')) return

  try {
    const response = await usersApi.updateStreamKey(userStore.user.id)
    streamData.value.streamKey = response.newStreamKey
  } catch (error) {
    console.error('Ошибка при сбросе ключа:', error)
    errorMessage.value = 'Не удалось сбросить ключ стрима'
  }
}

// Копирование ключа стрима
const copyStreamKey = () => {
  navigator.clipboard.writeText(streamData.value.streamKey)
      .then(() => alert('Ключ стрима скопирован'))
      .catch(err => console.error('Ошибка копирования:', err))
}

onMounted(async () => {
  await fetchStreamKey()
})
</script>

<template>
  <v-container>
    <h1 class="text-h4 mb-4">Профиль пользователя</h1>

    <v-card>
      <v-card-item>
        <v-avatar size="100" class="mr-4">
          <v-img :src="userStore.user.avatarUrl || 'https://i.imgur.com/XzZDFqy.jpg '" cover></v-img>
        </v-avatar>
        <v-card-title>{{ userStore.user.username }}</v-card-title>
        <v-card-subtitle>{{ userStore.user.followerCount }} подписчиков</v-card-subtitle>
      </v-card-item>

      <v-tabs v-model="activeTab" align-tabs="start">
        <v-tab value="info">Информация профиля</v-tab>
        <v-tab value="stream">Настройки стрима</v-tab>
      </v-tabs>

      <v-card-text>
        <v-window v-model="activeTab">
          <!-- Информация -->
          <v-window-item value="info">
            <v-form @submit.prevent="saveProfile">
              <v-text-field
                  v-model="userStore.user.email"
                  label="Email"
                  type="email"
                  readonly
                  disabled
                  class="mb-4"
              ></v-text-field>

              <v-textarea
                  v-model="userStore.user.bio"
                  label="О себе"
                  rows="4"
                  auto-grow
                  class="mb-4"
              ></v-textarea>

              <v-text-field
                  v-model="userStore.user.avatarUrl"
                  label="Аватар (URL)"
                  class="mb-4"
              ></v-text-field>

              <v-btn type="submit" color="primary" :loading="isLoading">Сохранить изменения</v-btn>

              <v-alert v-if="errorMessage" type="error" variant="tonal" border="start" class="mt-4">
                {{ errorMessage }}
              </v-alert>
            </v-form>
          </v-window-item>

          <!-- Настройки стрима -->
          <v-window-item value="stream">
            <v-card variant="outlined" class="mb-4">
              <v-card-title>Ключ стрима</v-card-title>
              <v-card-text>
                <v-row>
                  <v-col cols="12" sm="8">
                    <v-text-field
                        v-model="streamData.streamKey"
                        readonly
                        variant="outlined"
                        density="compact"
                        hide-details
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="4" class="d-flex gap-2">
                    <v-btn @click="copyStreamKey">
                      <v-icon start>mdi-content-copy</v-icon>
                      Копировать
                    </v-btn>
                    <v-btn color="error" @click="resetStreamKey">
                      <v-icon start>mdi-refresh</v-icon>
                      Сбросить
                    </v-btn>
                  </v-col>
                </v-row>
                <v-alert type="warning" class="mt-2">
                  Никогда не передавайте свой ключ стрима третьим лицам!
                </v-alert>
              </v-card-text>
            </v-card>
          </v-window-item>
        </v-window>
      </v-card-text>
    </v-card>
  </v-container>
</template>