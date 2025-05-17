<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()

const bioText = ref('')

const safeUserData = computed(() => {
  if (!userStore.user) return {};
  
  return {
    username: userStore.user.username || '',
    email: userStore.user.email || '',
    bio: typeof userStore.user.bio === 'object' ? '' : (userStore.user.bio || ''),
    avatarUrl: userStore.user.avatarUrl || 'https://picsum.photos/200/300',
    followerCount: userStore.user.followerCount || 0
  };
});

const activeTab = ref('info')
const isStreamKeyVisible = ref(false)
const streamKeyDisplay = computed(() => {
  if (!userStore.user || !userStore.user.streamKey) return '';
  return isStreamKeyVisible.value ? userStore.user.streamKey : '•'.repeat(userStore.user.streamKey.length);
})

const errorMessage = ref('')
const isLoading = ref(false)
const isUserLoading = ref(true)

const fileInput = ref(null)

const triggerFileUpload = () => {
  fileInput.value.click()
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    // Здесь можно добавить логику для загрузки файла на сервер
    // Пока просто обновляем URL аватара локально
    if (userStore.user) {
      userStore.user.avatarUrl = e.target.result
    }
  }
  reader.readAsDataURL(file)
}

// Сохранение данных профиля
const saveProfile = async () => {
  if (!userStore.user) return

  isLoading.value = true
  errorMessage.value = ''

  // Проверяем и обрабатываем поле bio
  const bioValue = typeof bioText.value === 'object' ? '' : (bioText.value || '')

  const payload = {
    email: userStore.user.email,
    bio: bioValue,
    avatarUrl: userStore.user.avatarUrl || 'https://picsum.photos/200/300',
  }

  console.log('Отправка данных профиля:', payload)

  try {
    const result = await userStore.updateCurrentUser(payload)
    if (result) {
      alert('Профиль успешно обновлен')
      
      // Обновляем локальное значение bio после успешного сохранения
      if (userStore.user) {
        userStore.user.bio = bioValue
      }
    } else {
      errorMessage.value = 'Не удалось обновить профиль'
    }
  } catch (error) {
    console.error('Ошибка при обновлении профиля:', error)
    errorMessage.value = 'Ошибка при сохранении профиля'
  } finally {
    isLoading.value = false
  }
}

// Переключение видимости ключа стрима
const toggleStreamKeyVisibility = () => {
  isStreamKeyVisible.value = !isStreamKeyVisible.value
}

// Сброс ключа стрима
const resetStreamKey = async () => {
  if (!confirm('Вы уверены, что хотите сбросить ключ стрима? Текущий стрим будет прерван.')) return

  try {
    const newKey = await userStore.regenerateStreamKey()
    if (newKey) {
      // Сначала обновляем ключ в пользовательских данных
      if (userStore.user) {
        userStore.user.streamKey = newKey
      }
      
      // Затем делаем ключ видимым
      isStreamKeyVisible.value = true
      
      // Используем nextTick для гарантии обновления DOM
      await nextTick()
    }
  } catch (error) {
    console.error('Ошибка при сбросе ключа:', error)
    errorMessage.value = 'Не удалось сбросить ключ стрима'
  }
}

// Копирование ключа стрима
const copyStreamKey = () => {
  if (!userStore.user || !userStore.user.streamKey) return
  
  navigator.clipboard.writeText(userStore.user.streamKey)
      .then(() => alert('Ключ стрима скопирован'))
      .catch(err => console.error('Ошибка копирования:', err))
}

// Загрузка данных пользователя
const loadUserData = async () => {
  isUserLoading.value = true
  
  try {
    if (!userStore.user) {
      await userStore.fetchCurrentUser()
      console.log('Загруженные данные пользователя:', userStore.user)
    }
    
    // Инициализация bioText
    if (userStore.user) {
      bioText.value = typeof userStore.user.bio === 'object' ? '' : (userStore.user.bio || '')
    }
  } catch (error) {
    console.error('Ошибка при загрузке данных пользователя:', error)
    if (!userStore.isAuthenticated) {
      router.push('/login')
    }
  } finally {
    isUserLoading.value = false
  }
}

onMounted(async () => {
  await loadUserData()
})
</script>

<template>
  <v-container v-if="!isUserLoading && userStore.user">
    <h1 class="text-h4 mb-4">Профиль пользователя</h1>

    <v-card>
      <v-card-item>
        <div class="position-relative d-inline-block mr-4">
          <v-avatar size="100" class="cursor-pointer" border @click="triggerFileUpload">
            <v-img :src="safeUserData.avatarUrl" cover></v-img>
            <div class="avatar-overlay d-flex align-center justify-center">
              <v-icon>mdi-camera</v-icon>
            </div>
          </v-avatar>
          <input 
            ref="fileInput" 
            type="file" 
            accept="image/*" 
            class="d-none" 
            @change="handleFileUpload"
          />
        </div>
        <v-card-title>{{ safeUserData.username }}</v-card-title>
        <v-card-subtitle>{{ safeUserData.followerCount }} подписчиков</v-card-subtitle>
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
                  v-model="bioText"
                  label="О себе"
                  rows="4"
                  auto-grow
                  class="mb-4"
              ></v-textarea>

              <v-btn type="submit" color="primary" :loading="isLoading">Сохранить изменения</v-btn>

              <v-alert v-if="errorMessage" type="error" variant="tonal" border="start" class="mt-4">
                {{ errorMessage }}
              </v-alert>
            </v-form>
          </v-window-item>

          <v-window-item value="stream">
            <v-card variant="outlined" class="mb-4">
              <v-card-title>Ключ стрима</v-card-title>
              <v-card-text>
                <v-row>
                  <v-col cols="12" sm="8">
                    <v-text-field
                        v-model="streamKeyDisplay"
                        readonly
                        variant="outlined"
                        density="compact"
                        hide-details
                        class="stream-key-field"
                        @click="toggleStreamKeyVisibility"
                        :append-icon="isStreamKeyVisible ? 'mdi-eye-off' : 'mdi-eye'"
                        @click:append="toggleStreamKeyVisibility"
                    >
                      <template v-slot:prepend>
                        <v-tooltip location="top">
                          <template v-slot:activator="{ props }">
                            <v-icon v-bind="props" color="primary">
                              {{ isStreamKeyVisible ? 'mdi-lock-open' : 'mdi-lock' }}
                            </v-icon>
                          </template>
                          <span>{{ isStreamKeyVisible ? 'Ключ виден' : 'Ключ скрыт' }}</span>
                        </v-tooltip>
                      </template>
                    </v-text-field>
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

  <v-container v-else-if="isUserLoading">
    <div class="d-flex justify-center align-center" style="height: 300px;">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>
  </v-container>

  <v-container v-else>
    <v-alert type="error" variant="tonal" border="start">
      Не удалось загрузить данные профиля. Пожалуйста, убедитесь, что вы авторизованы.
    </v-alert>
    <div class="text-center mt-4">
      <v-btn color="primary" to="/login">Войти</v-btn>
    </div>
  </v-container>
</template>

<style scoped>
.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
}

.cursor-pointer {
  cursor: pointer;
}

.cursor-pointer:hover .avatar-overlay {
  opacity: 1;
}

.stream-key-field {
  cursor: pointer;
  font-family: monospace;
}
</style>