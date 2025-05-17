<script setup>
import { ref, onMounted, computed } from 'vue'
import { useStreamStore } from '@/stores/stream'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const streamStore = useStreamStore()
const userStore = useUserStore()

// Состояние формы
const formData = ref({
  title: '',
  description: '',
  thumbnailUrl: '',
  tags: []
})

const newTag = ref('')
const activeTab = ref('streams')
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Вычисляемые свойства
const isAuthenticated = computed(() => userStore.isAuthenticated)
const hasActiveStream = computed(() => streamStore.hasActiveStream)
const activeStream = computed(() => streamStore.activeStream)

// Добавление тега
const addTag = () => {
  if (newTag.value && !formData.value.tags.includes(newTag.value)) {
    formData.value.tags.push(newTag.value)
    newTag.value = ''
  }
}

// Удаление тега
const removeTag = (tag) => {
  formData.value.tags = formData.value.tags.filter(t => t !== tag)
}

// Создание нового стрима
const createStream = async () => {
  if (!formData.value.title) {
    errorMessage.value = 'Название стрима обязательно'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const result = await streamStore.createStream({
      title: formData.value.title,
      description: formData.value.description,
      thumbnailUrl: formData.value.thumbnailUrl,
      tags: formData.value.tags
    })

    if (result) {
      successMessage.value = 'Стрим успешно создан'
      resetForm()
      activeTab.value = 'streams'
    } else {
      errorMessage.value = streamStore.error || 'Не удалось создать стрим'
    }
  } catch (error) {
    console.error('Ошибка при создании стрима:', error)
    errorMessage.value = 'Произошла ошибка при создании стрима'
  } finally {
    isLoading.value = false
  }
}

// Обновление стрима
const updateStream = async () => {
  if (!activeStream.value || !formData.value.title) {
    errorMessage.value = 'Название стрима обязательно'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const result = await streamStore.updateStream({
      title: formData.value.title,
      description: formData.value.description,
      thumbnailUrl: formData.value.thumbnailUrl,
      tags: formData.value.tags
    })

    if (result) {
      successMessage.value = 'Стрим успешно обновлен'
    } else {
      errorMessage.value = streamStore.error || 'Не удалось обновить стрим'
    }
  } catch (error) {
    console.error('Ошибка при обновлении стрима:', error)
    errorMessage.value = 'Произошла ошибка при обновлении стрима'
  } finally {
    isLoading.value = false
  }
}

// Завершение стрима
const endCurrentStream = async () => {
  if (!confirm('Вы уверены, что хотите завершить стрим?')) return

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const result = await streamStore.endStream()

    if (result) {
      successMessage.value = 'Стрим успешно завершен'
    } else {
      errorMessage.value = streamStore.error || 'Не удалось завершить стрим'
    }
  } catch (error) {
    console.error('Ошибка при завершении стрима:', error)
    errorMessage.value = 'Произошла ошибка при завершении стрима'
  } finally {
    isLoading.value = false
  }
}

// Редактирование стрима
const editStream = (stream) => {
  formData.value = {
    title: stream.title,
    description: stream.description || '',
    thumbnailUrl: stream.thumbnailUrl || '',
    tags: [...stream.tags]
  }
  activeTab.value = 'edit'
}

// Сброс формы
const resetForm = () => {
  formData.value = {
    title: '',
    description: '',
    thumbnailUrl: '',
    tags: []
  }
}

// Загрузка стримов пользователя
const loadUserStreams = async () => {
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    await streamStore.fetchCurrentUserStreams()
  } catch (error) {
    console.error('Ошибка при загрузке стримов:', error)
    errorMessage.value = 'Не удалось загрузить стримы'
  } finally {
    isLoading.value = false
  }
}

// Инициализация
onMounted(async () => {
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }

  await loadUserStreams()
})
</script>

<template>
  <v-container v-if="isAuthenticated">
    <h1 class="text-h4 mb-4">Управление стримами</h1>

    <v-tabs v-model="activeTab" align-tabs="start" class="mb-4">
      <v-tab value="streams">Мои стримы</v-tab>
      <v-tab value="create" :disabled="hasActiveStream">Создать стрим</v-tab>
      <v-tab value="edit" v-if="activeStream" :disabled="!hasActiveStream">Редактировать стрим</v-tab>
    </v-tabs>

    <v-alert v-if="errorMessage" type="error" variant="tonal" border="start" class="mb-4">
      {{ errorMessage }}
    </v-alert>

    <v-alert v-if="successMessage" type="success" variant="tonal" border="start" class="mb-4">
      {{ successMessage }}
    </v-alert>

    <v-window v-model="activeTab">
      <!-- Список стримов -->
      <v-window-item value="streams">
        <v-card v-if="hasActiveStream" color="success" variant="outlined" class="mb-4">
          <v-card-title>Активный стрим</v-card-title>
          <v-card-text>
            <p><strong>Название:</strong> {{ activeStream.title }}</p>
            <p v-if="activeStream.description"><strong>Описание:</strong> {{ activeStream.description }}</p>
            <p><strong>Начало:</strong> {{ activeStream.getFormattedStartTime() }}</p>
            <p><strong>Зрителей:</strong> {{ activeStream.viewerCount }}</p>
            
            <div class="d-flex gap-2 mt-4">
              <v-btn color="primary" @click="editStream(activeStream)">
                <v-icon start>mdi-pencil</v-icon>
                Редактировать
              </v-btn>
              <v-btn color="error" @click="endCurrentStream" :loading="isLoading">
                <v-icon start>mdi-stop</v-icon>
                Завершить стрим
              </v-btn>
            </div>
          </v-card-text>
        </v-card>

        <v-card v-if="!hasActiveStream" variant="outlined" class="mb-4">
          <v-card-title>У вас нет активных стримов</v-card-title>
          <v-card-text>
            <p>Создайте новый стрим, чтобы начать трансляцию</p>
            <v-btn color="primary" @click="activeTab = 'create'" class="mt-2">
              <v-icon start>mdi-plus</v-icon>
              Создать стрим
            </v-btn>
          </v-card-text>
        </v-card>

        <h2 class="text-h5 mb-3 mt-4">История стримов</h2>
        
        <v-table v-if="streamStore.currentUserStreams.length > 0">
          <thead>
            <tr>
              <th>Название</th>
              <th>Дата</th>
              <th>Длительность</th>
              <th>Зрители</th>
              <th>Статус</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="stream in streamStore.currentUserStreams" :key="stream.id">
              <td>{{ stream.title }}</td>
              <td>{{ stream.getFormattedStartTime() }}</td>
              <td>{{ stream.getDuration() }}</td>
              <td>{{ stream.viewerCount }}</td>
              <td>
                <v-chip :color="stream.isLive ? 'success' : 'default'" size="small">
                  {{ stream.isLive ? 'В эфире' : 'Завершен' }}
                </v-chip>
              </td>
            </tr>
          </tbody>
        </v-table>

        <v-card v-else variant="outlined" class="text-center py-4">
          <v-card-text>
            <p>У вас пока нет стримов</p>
          </v-card-text>
        </v-card>
      </v-window-item>

      <!-- Создание стрима -->
      <v-window-item value="create">
        <v-card variant="outlined">
          <v-card-title>Создание нового стрима</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="createStream">
              <v-text-field
                v-model="formData.title"
                label="Название стрима"
                required
                :rules="[v => !!v || 'Название обязательно']"
                class="mb-4"
              ></v-text-field>

              <v-textarea
                v-model="formData.description"
                label="Описание стрима"
                rows="4"
                auto-grow
                class="mb-4"
              ></v-textarea>

              <v-text-field
                v-model="formData.thumbnailUrl"
                label="URL миниатюры"
                class="mb-4"
              ></v-text-field>

              <div class="mb-4">
                <label class="text-subtitle-1 mb-2 d-block">Теги</label>
                <div class="d-flex flex-wrap gap-2 mb-2">
                  <v-chip
                    v-for="tag in formData.tags"
                    :key="tag"
                    closable
                    @click:close="removeTag(tag)"
                  >
                    {{ tag }}
                  </v-chip>
                </div>
                <div class="d-flex gap-2">
                  <v-text-field
                    v-model="newTag"
                    label="Новый тег"
                    hide-details
                    density="compact"
                    @keyup.enter="addTag"
                  ></v-text-field>
                  <v-btn @click="addTag" icon="mdi-plus"></v-btn>
                </div>
              </div>

              <v-btn type="submit" color="primary" :loading="isLoading" class="mr-2">
                <v-icon start>mdi-check</v-icon>
                Создать стрим
              </v-btn>
              <v-btn @click="resetForm" variant="outlined">
                <v-icon start>mdi-refresh</v-icon>
                Сбросить
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-window-item>

      <!-- Редактирование стрима -->
      <v-window-item value="edit">
        <v-card variant="outlined">
          <v-card-title>Редактирование стрима</v-card-title>
          <v-card-text>
            <v-form @submit.prevent="updateStream">
              <v-text-field
                v-model="formData.title"
                label="Название стрима"
                required
                :rules="[v => !!v || 'Название обязательно']"
                class="mb-4"
              ></v-text-field>

              <v-textarea
                v-model="formData.description"
                label="Описание стрима"
                rows="4"
                auto-grow
                class="mb-4"
              ></v-textarea>

              <v-text-field
                v-model="formData.thumbnailUrl"
                label="URL миниатюры"
                class="mb-4"
              ></v-text-field>

              <div class="mb-4">
                <label class="text-subtitle-1 mb-2 d-block">Теги</label>
                <div class="d-flex flex-wrap gap-2 mb-2">
                  <v-chip
                    v-for="tag in formData.tags"
                    :key="tag"
                    closable
                    @click:close="removeTag(tag)"
                  >
                    {{ tag }}
                  </v-chip>
                </div>
                <div class="d-flex gap-2">
                  <v-text-field
                    v-model="newTag"
                    label="Новый тег"
                    hide-details
                    density="compact"
                    @keyup.enter="addTag"
                  ></v-text-field>
                  <v-btn @click="addTag" icon="mdi-plus"></v-btn>
                </div>
              </div>

              <v-btn type="submit" color="primary" :loading="isLoading" class="mr-2">
                <v-icon start>mdi-check</v-icon>
                Сохранить изменения
              </v-btn>
              <v-btn @click="activeTab = 'streams'" variant="outlined">
                <v-icon start>mdi-cancel</v-icon>
                Отмена
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-window-item>
    </v-window>
  </v-container>

  <v-container v-else>
    <v-alert type="warning" variant="tonal" border="start">
      Необходимо авторизоваться для управления стримами
    </v-alert>
    <div class="text-center mt-4">
      <v-btn color="primary" to="/login">Войти</v-btn>
    </div>
  </v-container>
</template> 